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
package org.apache.drill.metastore;

import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.exec.record.metadata.ColumnMetadata;
import org.apache.drill.exec.record.metadata.SchemaPathUtils;
import org.apache.drill.exec.record.metadata.TupleSchema;
import org.apache.hadoop.fs.Path;

import java.util.Map;

public class RowGroupMetadata implements BaseMetadata, LocationProvider {
  private final TupleSchema schema;
  private final Map<SchemaPath, ColumnStatistic> columnStatistics;
  private final Map<String, Object> rowGroupStatistics;
  private Map<String, Float> hostAffinity;
  private int rowGroupIndex;
  private Path location;

  public RowGroupMetadata(TupleSchema schema,
                          Map<SchemaPath, ColumnStatistic> columnStatistics,
                          Map<String, Object> rowGroupStatistics,
                          Map<String, Float> hostAffinity,
                          int rowGroupIndex,
                          Path location) {
    this.schema = schema;
    this.columnStatistics = columnStatistics;
    this.rowGroupStatistics = rowGroupStatistics;
    this.hostAffinity = hostAffinity;
    this.rowGroupIndex = rowGroupIndex;
    this.location = location;
  }

  @Override
  public TupleSchema getSchema() {
    return schema;
  }

  @Override
  public Object getStatistic(StatisticsKind statisticsKind) {
    return rowGroupStatistics.get(statisticsKind.getName());
  }

  @Override
  public Map<SchemaPath, ColumnStatistic> getColumnStatistics() {
    return columnStatistics;
  }

  @Override
  public Path getLocation() {
    return location;
  }

  //TODO: why do not add these methods to interface?

  public ColumnMetadata getColumn(SchemaPath name) {
    return SchemaPathUtils.getColumnMetadata(name, schema);
  }

  public int getRowGroupIndex() {
    return rowGroupIndex;
  }

  public Map<String, Float> getHostAffinity() {
    return hostAffinity;
  }

}
