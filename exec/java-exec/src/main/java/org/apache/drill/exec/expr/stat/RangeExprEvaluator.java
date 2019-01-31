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
package org.apache.drill.exec.expr.stat;

import org.apache.drill.common.exceptions.DrillRuntimeException;
import org.apache.drill.common.expression.FunctionHolderExpression;
import org.apache.drill.common.expression.LogicalExpression;
import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.common.expression.TypedFieldExpr;
import org.apache.drill.common.expression.ValueExpressions;
import org.apache.drill.common.expression.fn.FunctionReplacementUtils;
import org.apache.drill.common.expression.fn.FuncHolder;
import org.apache.drill.common.expression.visitors.AbstractExprVisitor;
import org.apache.drill.common.types.TypeProtos;
import org.apache.drill.common.types.Types;
import org.apache.drill.exec.expr.DrillSimpleFunc;
import org.apache.drill.exec.expr.fn.DrillSimpleFuncHolder;
import org.apache.drill.exec.expr.fn.interpreter.InterpreterEvaluator;
import org.apache.drill.exec.expr.holders.BigIntHolder;
import org.apache.drill.exec.expr.holders.Float4Holder;
import org.apache.drill.exec.expr.holders.Float8Holder;
import org.apache.drill.exec.expr.holders.IntHolder;
import org.apache.drill.exec.expr.holders.TimeStampHolder;
import org.apache.drill.exec.expr.holders.ValueHolder;
import org.apache.drill.exec.store.parquet.stat.ColumnStatistics;
import org.apache.drill.exec.vector.ValueHolderHelper;
import org.apache.parquet.column.statistics.BooleanStatistics;
import org.apache.parquet.column.statistics.DoubleStatistics;
import org.apache.parquet.column.statistics.FloatStatistics;
import org.apache.parquet.column.statistics.IntStatistics;
import org.apache.parquet.column.statistics.LongStatistics;
import org.apache.parquet.column.statistics.Statistics;
import org.apache.parquet.schema.DecimalMetadata;
import org.apache.parquet.schema.OriginalType;
import org.apache.parquet.schema.PrimitiveType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RangeExprEvaluator<T extends Comparable<T>> extends AbstractExprVisitor<Statistics<T>, Void, RuntimeException> {

  private final Map<SchemaPath, ColumnStatistics<T>> columnStatMap;
  private final long rowCount;

  public RangeExprEvaluator(final Map<SchemaPath, ColumnStatistics<T>> columnStatMap, long rowCount) {
    this.columnStatMap = columnStatMap;
    this.rowCount = rowCount;
  }

  public long getRowCount() {
    return this.rowCount;
  }

  @Override
  public Statistics<T> visitUnknown(LogicalExpression e, Void value) throws RuntimeException {
    // do nothing for the unknown expression
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Statistics<T> visitTypedFieldExpr(TypedFieldExpr typedFieldExpr, Void value) throws RuntimeException {
    final ColumnStatistics<T> columnStatistics = columnStatMap.get(typedFieldExpr.getPath());
    if (columnStatistics != null) {
      return columnStatistics.getStatistics();
    } else if (typedFieldExpr.getMajorType().equals(Types.OPTIONAL_INT)) {
      // field does not exist.
      PrimitiveType columnPrimitiveType = org.apache.parquet.schema.Types.optional(PrimitiveType.PrimitiveTypeName.INT32)
          .as(OriginalType.INT_32)
          .named(typedFieldExpr.getPath().toString());

      return (Statistics<T>) Statistics.getBuilderForReading(columnPrimitiveType)
          .withNumNulls(rowCount) // all values are nulls
          .build();
    }
    return null;
  }

  @Override
  public Statistics<T> visitIntConstant(ValueExpressions.IntExpression expr, Void value) throws RuntimeException {
    return getStatistics(expr.getInt());
  }

  @Override
  public Statistics<T> visitBooleanConstant(ValueExpressions.BooleanExpression expr, Void value) throws RuntimeException {
    return getStatistics(expr.getBoolean());
  }

  @Override
  public Statistics<T> visitLongConstant(ValueExpressions.LongExpression expr, Void value) throws RuntimeException {
    return getStatistics(expr.getLong());
  }

  @Override
  public Statistics<T> visitFloatConstant(ValueExpressions.FloatExpression expr, Void value) throws RuntimeException {
    return getStatistics(expr.getFloat());
  }

  @Override
  public Statistics<T> visitDoubleConstant(ValueExpressions.DoubleExpression expr, Void value) throws RuntimeException {
    return getStatistics(expr.getDouble());
  }

  @Override
  public Statistics<T> visitDateConstant(ValueExpressions.DateExpression expr, Void value) throws RuntimeException {
    long dateInMillis = expr.getDate();
    return getStatistics(dateInMillis);
  }

  @Override
  public Statistics<T> visitTimeStampConstant(ValueExpressions.TimeStampExpression tsExpr, Void value) throws RuntimeException {
    long tsInMillis = tsExpr.getTimeStamp();
    return getStatistics(tsInMillis);
  }

  @Override
  public Statistics<T> visitTimeConstant(ValueExpressions.TimeExpression timeExpr, Void value) throws RuntimeException {
    int milliSeconds = timeExpr.getTime();
    return getStatistics(milliSeconds);
  }

  @Override
  public Statistics<T> visitQuotedStringConstant(ValueExpressions.QuotedString quotedString, Void value) throws RuntimeException {
    String stringValue = quotedString.getString();
    return getStatistics(stringValue);
  }

  @Override
  public Statistics<T> visitVarDecimalConstant(ValueExpressions.VarDecimalExpression decExpr, Void value) throws RuntimeException {
    DecimalMetadata decimalMeta = new DecimalMetadata(decExpr.getMajorType().getPrecision(), decExpr.getMajorType().getScale());
    return getStatistics(decExpr.getBigDecimal(), decimalMeta);
  }

  @Override
  public Statistics<T> visitFunctionHolderExpression(FunctionHolderExpression holderExpr, Void value) throws RuntimeException {
    FuncHolder funcHolder = holderExpr.getHolder();

    if (! (funcHolder instanceof DrillSimpleFuncHolder)) {
      // Only Drill function is allowed.
      return null;
    }

    final String funcName = ((DrillSimpleFuncHolder) funcHolder).getRegisteredNames()[0];

    if (FunctionReplacementUtils.isCastFunction(funcName)) {
      Statistics stat = holderExpr.args.get(0).accept(this, null);
      if (stat != null && ! stat.isEmpty()) {
        return evalCastFunc(holderExpr, stat);
      }
    }
    return null;
  }

  private Statistics<T> getStatistics(int value) {
    return getStatistics(value, value);
  }

  @SuppressWarnings("unchecked")
  private Statistics<T> getStatistics(int min, int max) {
    PrimitiveType int32Type = org.apache.parquet.schema.Types.optional(PrimitiveType.PrimitiveTypeName.INT32)
        .as(OriginalType.INT_32)
        .named("int32_type");
    Statistics<T> statistics = (Statistics<T>) Statistics.createStats(int32Type);
    Statistics.createStats(int32Type);
    ((IntStatistics) statistics).setMinMax(min, max);
    return statistics;
  }

  private Statistics<T> getStatistics(boolean value) {
    return getStatistics(value, value);
  }

  @SuppressWarnings("unchecked")
  private Statistics<T> getStatistics(boolean min, boolean max) {
    PrimitiveType booleanType = org.apache.parquet.schema.Types.optional(PrimitiveType.PrimitiveTypeName.BOOLEAN)
        .named("boolean_type");
    Statistics<T> statistics = (Statistics<T>) Statistics.createStats(booleanType);
    ((BooleanStatistics) statistics).setMinMax(min, max);
    return statistics;
  }

  private Statistics<T> getStatistics(long value) {
    return getStatistics(value, value);
  }

  @SuppressWarnings("unchecked")
  private Statistics<T> getStatistics(long min, long max) {
    PrimitiveType int64Type = org.apache.parquet.schema.Types.optional(PrimitiveType.PrimitiveTypeName.INT64)
        .named("int64_type");
    Statistics<T> statistics = (Statistics<T>) Statistics.createStats(int64Type);
    ((LongStatistics) statistics).setMinMax(min, max);
    return statistics;
  }

  private Statistics<T> getStatistics(double value) {
    return getStatistics(value, value);
  }

  @SuppressWarnings("unchecked")
  private Statistics<T> getStatistics(double min, double max) {
    PrimitiveType doubleType = org.apache.parquet.schema.Types.optional(PrimitiveType.PrimitiveTypeName.DOUBLE)
        .named("double_type");
    Statistics<T> statistics = (Statistics<T>) Statistics.createStats(doubleType);
    ((DoubleStatistics) statistics).setMinMax(min, max);
    return statistics;
  }

  private Statistics<T> getStatistics(float value) {
    return getStatistics(value, value);
  }

  @SuppressWarnings("unchecked")
  private Statistics<T> getStatistics(float min, float max) {
    PrimitiveType floatType = org.apache.parquet.schema.Types.optional(PrimitiveType.PrimitiveTypeName.FLOAT)
        .named("float_type");
    Statistics<T> statistics = (Statistics<T>) Statistics.createStats(floatType);
    ((FloatStatistics) statistics).setMinMax(min, max);
    return statistics;
  }

  private Statistics<T> getStatistics(String value) {
    return getStatistics(value, value);
  }

  @SuppressWarnings("unchecked")
  private Statistics<T> getStatistics(String min, String max) {
    PrimitiveType binaryType = org.apache.parquet.schema.Types.optional(PrimitiveType.PrimitiveTypeName.BINARY)
        .named("binary_type");
    return (Statistics<T>) Statistics.getBuilderForReading(binaryType)
        .withMin(min.getBytes())
        .withMax(max.getBytes())
        .withNumNulls(0)
        .build();
  }

  private Statistics<T> getStatistics(BigDecimal value, DecimalMetadata decimalMetadata) {
    return getStatistics(value, value, decimalMetadata);
  }

  @SuppressWarnings("unchecked")
  private Statistics<T> getStatistics(BigDecimal min, BigDecimal max, DecimalMetadata decimalMetadata) {
    PrimitiveType decimalType = org.apache.parquet.schema.Types.optional(PrimitiveType.PrimitiveTypeName.BINARY)
      .as(OriginalType.DECIMAL)
      .precision(decimalMetadata.getPrecision())
      .scale(decimalMetadata.getScale())
      .named("decimal_type");

    return (Statistics<T>) Statistics.getBuilderForReading(decimalType)
        .withMin(min.unscaledValue().toByteArray())
        .withMax(max.unscaledValue().toByteArray())
        .withNumNulls(0)
        .build();
  }

  private Statistics<T> evalCastFunc(FunctionHolderExpression holderExpr, Statistics<T> input) {
    try {
      DrillSimpleFuncHolder funcHolder = (DrillSimpleFuncHolder) holderExpr.getHolder();

      DrillSimpleFunc interpreter = funcHolder.createInterpreter();

      final ValueHolder minHolder, maxHolder;

      TypeProtos.MinorType srcType = holderExpr.args.get(0).getMajorType().getMinorType();
      TypeProtos.MinorType destType = holderExpr.getMajorType().getMinorType();

      if (srcType.equals(destType)) {
        // same type cast ==> NoOp.
        return input;
      } else if (!CAST_FUNC.containsKey(srcType) || !CAST_FUNC.get(srcType).contains(destType)) {
        return null; // cast func between srcType and destType is NOT allowed.
      }

      switch (srcType) {
      case INT :
        minHolder = ValueHolderHelper.getIntHolder(((IntStatistics)input).getMin());
        maxHolder = ValueHolderHelper.getIntHolder(((IntStatistics)input).getMax());
        break;
      case BIGINT:
        minHolder = ValueHolderHelper.getBigIntHolder(((LongStatistics)input).getMin());
        maxHolder = ValueHolderHelper.getBigIntHolder(((LongStatistics)input).getMax());
        break;
      case FLOAT4:
        minHolder = ValueHolderHelper.getFloat4Holder(((FloatStatistics)input).getMin());
        maxHolder = ValueHolderHelper.getFloat4Holder(((FloatStatistics)input).getMax());
        break;
      case FLOAT8:
        minHolder = ValueHolderHelper.getFloat8Holder(((DoubleStatistics)input).getMin());
        maxHolder = ValueHolderHelper.getFloat8Holder(((DoubleStatistics)input).getMax());
        break;
      case DATE:
        minHolder = ValueHolderHelper.getDateHolder(((LongStatistics)input).getMin());
        maxHolder = ValueHolderHelper.getDateHolder(((LongStatistics)input).getMax());
        break;
      default:
        return null;
      }

      final ValueHolder[] args1 = {minHolder};
      final ValueHolder[] args2 = {maxHolder};

      final ValueHolder minFuncHolder = InterpreterEvaluator.evaluateFunction(interpreter, args1, holderExpr.getName());
      final ValueHolder maxFuncHolder = InterpreterEvaluator.evaluateFunction(interpreter, args2, holderExpr.getName());

      Statistics<T> statistics;
      switch (destType) {
        case INT:
          statistics = getStatistics(((IntHolder) minFuncHolder).value, ((IntHolder) maxFuncHolder).value);
          break;
        case BIGINT:
          statistics = getStatistics(((BigIntHolder) minFuncHolder).value, ((BigIntHolder) maxFuncHolder).value);
          break;
        case FLOAT4:
          statistics = getStatistics(((Float4Holder) minFuncHolder).value, ((Float4Holder) maxFuncHolder).value);
          break;
        case FLOAT8:
          statistics = getStatistics(((Float8Holder) minFuncHolder).value, ((Float8Holder) maxFuncHolder).value);
          break;
        case TIMESTAMP:
          statistics = getStatistics(((TimeStampHolder) minFuncHolder).value, ((TimeStampHolder) maxFuncHolder).value);
          break;
        default:
          return null;
      }
      statistics.setNumNulls(input.getNumNulls());
      return statistics;
    } catch (Exception e) {
      throw new DrillRuntimeException("Error in evaluating function of " + holderExpr.getName() );
    }
  }

  public static final Map<TypeProtos.MinorType, Set<TypeProtos.MinorType>> CAST_FUNC = new HashMap<>();
  static {
    // float -> double , int, bigint
    HashSet<TypeProtos.MinorType> float4Types = new HashSet<>();
    CAST_FUNC.put(TypeProtos.MinorType.FLOAT4, float4Types);
    float4Types.add(TypeProtos.MinorType.FLOAT8);
    float4Types.add(TypeProtos.MinorType.INT);
    float4Types.add(TypeProtos.MinorType.BIGINT);

    // double -> float, int, bigint
    HashSet<TypeProtos.MinorType> float8Types = new HashSet<>();
    CAST_FUNC.put(TypeProtos.MinorType.FLOAT8, float8Types);
    float8Types.add(TypeProtos.MinorType.FLOAT4);
    float8Types.add(TypeProtos.MinorType.INT);
    float8Types.add(TypeProtos.MinorType.BIGINT);

    // int -> float, double, bigint
    HashSet<TypeProtos.MinorType> intTypes = new HashSet<>();
    CAST_FUNC.put(TypeProtos.MinorType.INT, intTypes);
    intTypes.add(TypeProtos.MinorType.FLOAT4);
    intTypes.add(TypeProtos.MinorType.FLOAT8);
    intTypes.add(TypeProtos.MinorType.BIGINT);

    // bigint -> int, float, double
    HashSet<TypeProtos.MinorType> bigIntTypes = new HashSet<>();
    CAST_FUNC.put(TypeProtos.MinorType.BIGINT, bigIntTypes);
    bigIntTypes.add(TypeProtos.MinorType.INT);
    bigIntTypes.add(TypeProtos.MinorType.FLOAT4);
    bigIntTypes.add(TypeProtos.MinorType.FLOAT8);

    // date -> timestamp
    HashSet<TypeProtos.MinorType> dateTypes = new HashSet<>();
    CAST_FUNC.put(TypeProtos.MinorType.DATE, dateTypes);
    dateTypes.add(TypeProtos.MinorType.TIMESTAMP);
  }

}
