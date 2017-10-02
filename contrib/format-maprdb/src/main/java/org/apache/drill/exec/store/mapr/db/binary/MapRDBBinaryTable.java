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
package org.apache.drill.exec.store.mapr.db.binary;

import java.io.IOException;

import org.apache.drill.common.exceptions.UserException;
import org.apache.drill.exec.store.dfs.FileSystemPlugin;
import org.apache.drill.exec.store.dfs.FormatSelection;
import org.apache.drill.exec.store.hbase.AbstractHBaseDrillTable;
import org.apache.drill.exec.store.mapr.TableFormatPlugin;
import org.apache.drill.exec.store.mapr.db.MapRDBFormatPlugin;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;

public class MapRDBBinaryTable extends AbstractHBaseDrillTable {
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MapRDBBinaryTable.class);

  public MapRDBBinaryTable(String storageEngineName, FileSystemPlugin storagePlugin, TableFormatPlugin formatPlugin,
      FormatSelection selection) {
    super(storageEngineName, storagePlugin, selection);
    String tableName = selection.getSelection().getFiles().get(0);
    try(Admin admin = ((MapRDBFormatPlugin) formatPlugin).getConnection().getAdmin()) {
      tableDesc = admin.getTableDescriptor(TableName.valueOf(tableName));
    } catch (IOException e) {
      throw UserException.dataReadError()
          .message("Failure while loading table %s in database %s.", tableName, storageEngineName)
          .addContext("Message: ", e.getMessage())
          .build(logger);
    }
  }

}
