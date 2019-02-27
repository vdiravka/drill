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

import java.util.Map;

/**
 * Metadata which corresponds to the file level of table.
 */
public class FileMetadata implements BaseMetadata, LocationProvider {

  private final String location;
  private final TupleSchema schema;
  private final Map<SchemaPath, ColumnStatistics> columnsStatistics;
  private final Map<String, Object> fileStatistics;
  private final String tableName;
  private final long lastModifiedTime;

  public FileMetadata(String location, TupleSchema schema, Map<SchemaPath, ColumnStatistics> columnsStatistics,
                      Map<String, Object> fileStatistics, String tableName, long lastModifiedTime) {
    this.schema = schema;
    this.columnsStatistics = columnsStatistics;
    this.fileStatistics = fileStatistics;
    this.location = location;
    this.tableName = tableName;
    this.lastModifiedTime = lastModifiedTime;
  }

  @Override
  public Object getStatisticsForColumn(SchemaPath columnName, StatisticsKind statisticsKind) {
    return columnsStatistics.get(columnName).getStatistic(statisticsKind);
  }

  @Override
  public Object getStatistic(StatisticsKind statisticsKind) {
    return fileStatistics.get(statisticsKind.getName());
  }

  @Override
  public ColumnStatistics getColumnStatistics(SchemaPath columnName) {
    return columnsStatistics.get(columnName);
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public ColumnMetadata getColumn(SchemaPath name) {
    return SchemaPathUtils.getColumnMetadata(name, schema);
  }

  @Override
  public TupleSchema getSchema() {
    return schema;
  }

  @Override
  public Map<SchemaPath, ColumnStatistics> getColumnsStatistics() {
    return columnsStatistics;
  }

  /**
   * Returns name of the table which contain this file.
   *
   * @return name of the table for this file
   */
  public String getTableName() {
    return tableName;
  }

  /**
   * Returns last modified time of the file.
   *
   * @return last modified time of the file.
   */
  public long getLastModifiedTime() {
    return lastModifiedTime;
  }
}
