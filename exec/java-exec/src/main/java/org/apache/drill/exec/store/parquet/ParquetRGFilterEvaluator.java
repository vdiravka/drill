/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.store.parquet;

import org.apache.drill.exec.record.metadata.ColumnMetadata;
import org.apache.drill.exec.record.metadata.SchemaPathUtils;
import org.apache.drill.exec.record.metadata.TupleSchema;
import org.apache.drill.exec.store.parquet.metadata.MetadataBase;
import org.apache.drill.shaded.guava.com.google.common.collect.Sets;
import org.apache.drill.common.expression.ErrorCollector;
import org.apache.drill.common.expression.ErrorCollectorImpl;
import org.apache.drill.common.expression.LogicalExpression;
import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.common.expression.visitors.AbstractExprVisitor;
import org.apache.drill.common.types.TypeProtos;
import org.apache.drill.exec.compile.sig.ConstantExpressionIdentifier;
import org.apache.drill.exec.expr.ExpressionTreeMaterializer;
import org.apache.drill.exec.expr.fn.FunctionLookupContext;
import org.apache.drill.exec.expr.stat.ParquetFilterPredicate;
import org.apache.drill.exec.expr.stat.ParquetFilterPredicate.RowsMatch;
import org.apache.drill.exec.expr.stat.RangeExprEvaluator;
import org.apache.drill.exec.ops.FragmentContext;
import org.apache.drill.exec.ops.UdfUtilities;
import org.apache.drill.exec.server.options.OptionManager;
import org.apache.drill.exec.store.parquet.stat.ColumnStatCollector;
import org.apache.drill.exec.store.parquet.stat.ColumnStatistics;
import org.apache.drill.exec.store.parquet.stat.ParquetFooterStatCollector;
import org.apache.drill.metastore.ColumnStatistic;
import org.apache.drill.metastore.expr.FilterPredicate;
import org.apache.drill.metastore.expr.StatisticsProvider;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.drill.exec.store.parquet.metadata.MetadataBase.ParquetTableMetadataBase;

public class ParquetRGFilterEvaluator {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ParquetRGFilterEvaluator.class);

  public static RowsMatch evalFilter(LogicalExpression expr, ParquetMetadata footer, int rowGroupIndex,
      OptionManager options, FragmentContext fragmentContext) {
    final HashMap<String, String> emptyMap = new HashMap<>();
    return evalFilter(expr, footer, rowGroupIndex, options, fragmentContext, emptyMap);
  }

  public static RowsMatch evalFilter(LogicalExpression expr, ParquetMetadata footer, int rowGroupIndex,
      OptionManager options, FragmentContext fragmentContext, Map<String, String> implicitColValues) {
    // figure out the set of columns referenced in expression.
    final Set<SchemaPath> schemaPathsInExpr = expr.accept(new FieldReferenceFinder(), null);
    final ColumnStatCollector columnStatCollector = new ParquetFooterStatCollector(footer, rowGroupIndex, implicitColValues,true, options);

    Map<SchemaPath, ColumnStatistics> columnStatisticsMap = columnStatCollector.collectColStat(schemaPathsInExpr);

    return matches(expr, columnStatisticsMap, footer.getBlocks().get(rowGroupIndex).getRowCount(), fragmentContext, fragmentContext.getFunctionRegistry());
  }

  public static RowsMatch matches(ParquetFilterPredicate parquetPredicate,
      Map<SchemaPath, ColumnStatistics> columnStatisticsMap, long rowCount) {
    if (parquetPredicate != null) {
      RangeExprEvaluator rangeExprEvaluator = new RangeExprEvaluator(columnStatisticsMap, rowCount);
      return parquetPredicate.matches(rangeExprEvaluator);
    }
    return RowsMatch.SOME;
  }

  public static RowsMatch matches(LogicalExpression expr, Map<SchemaPath, ColumnStatistics> columnStatisticsMap,
      long rowCount, UdfUtilities udfUtilities, FunctionLookupContext functionImplementationRegistry) {
    ErrorCollector errorCollector = new ErrorCollectorImpl();
    TupleSchema schema = new TupleSchema();
    for (Map.Entry<SchemaPath, ColumnStatistics> pathStat : columnStatisticsMap.entrySet()) {
      SchemaPathUtils.addColumnMetadata(pathStat.getKey(), schema, pathStat.getValue().getMajorType());
    }

    LogicalExpression materializedFilter = ExpressionTreeMaterializer.materializeFilterExpr(
        expr,
        schema,
        errorCollector, functionImplementationRegistry);

    if (errorCollector.hasErrors()) {
      logger.error("{} error(s) encountered when materialize filter expression : {}",
          errorCollector.getErrorCount(), errorCollector.toErrorString());
      return RowsMatch.SOME;
    }

    Set<LogicalExpression> constantBoundaries = ConstantExpressionIdentifier.getConstantExpressionSet(materializedFilter);
    ParquetFilterPredicate parquetPredicate = ParquetFilterBuilder.buildParquetFilterPredicate(
        materializedFilter, constantBoundaries, udfUtilities, true);

    return matches(parquetPredicate, columnStatisticsMap, rowCount);
  }

  public static RowsMatch matches(ParquetFilterPredicate parquetPredicate, Map<SchemaPath, ColumnStatistics> columnStatisticsMap, long rowCount, ParquetTableMetadataBase parquetTableMetadata, List<? extends MetadataBase.ColumnMetadata> columnMetadataList, Set<SchemaPath> schemaPathsInExpr) {
    RowsMatch temp = matches(parquetPredicate, columnStatisticsMap, rowCount);
    return temp == RowsMatch.ALL && isRepeated(schemaPathsInExpr, parquetTableMetadata, columnMetadataList) ? RowsMatch.SOME : temp;
  }

  public static RowsMatch matches(FilterPredicate parquetPredicate,
                                  Map<SchemaPath, ColumnStatistic> columnStatisticsMap,
                                  long rowCount, TupleSchema fileMetadata, Set<SchemaPath> schemaPathsInExpr) {
    RowsMatch temp = matches(parquetPredicate, columnStatisticsMap, rowCount);
    return temp == RowsMatch.ALL && isRepeated(schemaPathsInExpr, fileMetadata) ? RowsMatch.SOME : temp;
  }

  public static RowsMatch matches(FilterPredicate predicate, Map<SchemaPath,
    ColumnStatistic> columnStatisticsMap, long rowCount) {
    if (predicate != null) {
      StatisticsProvider rangeExprEvaluator = new StatisticsProvider(columnStatisticsMap, rowCount);
      return predicate.matches(rangeExprEvaluator);
    }
    return RowsMatch.SOME;
  }

  private static boolean isRepeated(Set<SchemaPath> fields, TupleSchema fileMetadata) {
    for (SchemaPath field : fields) {
      ColumnMetadata columnMetadata = SchemaPathUtils.getColumnMetadata(field, fileMetadata);
      TypeProtos.MajorType fieldType = columnMetadata != null ? columnMetadata.majorType() : null;
      if (fieldType != null && fieldType.getMode() == TypeProtos.DataMode.REPEATED) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if one of the fields involved in the filter is an array (used in DRILL_6259_test_data).
   *
   * @return true if one at least is an array, false otherwise.
   */
  private static boolean isRepeated(Set<SchemaPath> fields, ParquetTableMetadataBase parquetTableMetadata, List<? extends MetadataBase.ColumnMetadata> columnMetadataList) {
    final Map<SchemaPath, MetadataBase.ColumnMetadata> columnMetadataMap = new HashMap<>();
    for (final MetadataBase.ColumnMetadata columnMetadata : columnMetadataList) {
      SchemaPath schemaPath = SchemaPath.getCompoundPath(columnMetadata.getName());
      columnMetadataMap.put(schemaPath, columnMetadata);
    }
    for (final SchemaPath field : fields) {
      MetadataBase.ColumnMetadata columnMetadata = columnMetadataMap.get(field.getUnIndexed());
      if (columnMetadata != null && parquetTableMetadata.getRepetitionLevel(columnMetadata.getName()) >= 1) {
        return true;
      }
    }
    return false;
  }

  /**
   * Search through a LogicalExpression, finding all internal schema path references and returning them in a set.
   */
  public static class FieldReferenceFinder extends AbstractExprVisitor<Set<SchemaPath>, Void, RuntimeException> {
    @Override
    public Set<SchemaPath> visitSchemaPath(SchemaPath path, Void value) {
      Set<SchemaPath> set = Sets.newHashSet();
      set.add(path);
      return set;
    }

    @Override
    public Set<SchemaPath> visitUnknown(LogicalExpression e, Void value) {
      Set<SchemaPath> paths = Sets.newHashSet();
      for (LogicalExpression ex : e) {
        paths.addAll(ex.accept(this, null));
      }
      return paths;
    }
  }
}
