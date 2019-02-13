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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.drill.exec.physical.base.AbstractMetadataGroupScan;
import org.apache.drill.exec.store.dfs.DrillFileSystem;
import org.apache.drill.metastore.FileMetadata;
import org.apache.drill.metastore.RowGroupMetadata;
import org.apache.drill.common.exceptions.ExecutionSetupException;
import org.apache.drill.common.expression.LogicalExpression;
import org.apache.drill.common.expression.SchemaPath;
import org.apache.drill.common.expression.ValueExpressions;
import org.apache.drill.common.logical.FormatPluginConfig;
import org.apache.drill.common.logical.StoragePluginConfig;
import org.apache.drill.exec.physical.base.GroupScan;
import org.apache.drill.exec.physical.base.PhysicalOperator;
import org.apache.drill.exec.proto.CoordinationProtos.DrillbitEndpoint;
import org.apache.drill.exec.store.ColumnExplorer;
import org.apache.drill.exec.store.StoragePluginRegistry;
import org.apache.drill.exec.store.dfs.FileSelection;
import org.apache.drill.exec.store.dfs.ReadEntryWithPath;
import org.apache.drill.exec.util.ImpersonationUtil;
import org.apache.hadoop.fs.Path;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.apache.drill.shaded.guava.com.google.common.base.Preconditions;

@JsonTypeName("parquet-scan")
public class ParquetGroupScan extends AbstractParquetGroupScan {

  private final ParquetFormatPlugin formatPlugin;
  private final ParquetFormatConfig formatConfig;

  private boolean usedMetadataCache; // false by default
  // may change when filter push down / partition pruning is applied
  private String selectionRoot;
  private String cacheFileRoot;

  @JsonCreator
  public ParquetGroupScan(@JacksonInject StoragePluginRegistry engineRegistry,
                          @JsonProperty("userName") String userName,
                          @JsonProperty("entries") List<ReadEntryWithPath> entries,
                          @JsonProperty("storage") StoragePluginConfig storageConfig,
                          @JsonProperty("format") FormatPluginConfig formatConfig,
                          @JsonProperty("columns") List<SchemaPath> columns,
                          @JsonProperty("selectionRoot") String selectionRoot,
                          @JsonProperty("cacheFileRoot") String cacheFileRoot,
                          @JsonProperty("readerConfig") ParquetReaderConfig readerConfig,
                          @JsonProperty("filter") LogicalExpression filter) throws IOException, ExecutionSetupException {
    super(ImpersonationUtil.resolveUserName(userName), columns, entries, readerConfig, filter);
    Preconditions.checkNotNull(storageConfig);
    Preconditions.checkNotNull(formatConfig);

    this.cacheFileRoot = cacheFileRoot;

//    ParquetTableMetadataCreator metadataCreator = new ParquetTableMetadataCreator(engineRegistry, userName, entries, storageConfig,
//        formatConfig, selectionRoot, cacheFileRoot, readerConfig, null);


    this.formatPlugin = Preconditions.checkNotNull(
        (ParquetFormatPlugin) engineRegistry.getFormatPlugin(storageConfig, formatConfig));
    this.formatConfig = this.formatPlugin.getConfig();
    DrillFileSystem fs = ImpersonationUtil.createFileSystem(ImpersonationUtil.resolveUserName(userName), formatPlugin.getFsConf());
    boolean corruptDatesAutoCorrected = this.formatConfig.areCorruptDatesAutoCorrected();
//    metadataProvider = new ParquetTableMetadataProvider(
//        new ParquetTableMetadataCreator(entries, selectionRoot, cacheFileRoot, readerConfig,
//            null, fs, corruptDatesAutoCorrected));

    metadataProvider = new ParquetTableMetadataProvider(entries, selectionRoot, cacheFileRoot, null,
        readerConfig, fs, corruptDatesAutoCorrected);
    ParquetTableMetadataProvider parquetMetadataProvider = (ParquetTableMetadataProvider) this.metadataProvider;

    this.selectionRoot = parquetMetadataProvider.getSelectionRoot();
//    String tableLocation = selectionRoot; // the same now in ParquetTableMetadataCreator constructors
//    String tableName = selectionRoot; // the same now in ParquetTableMetadataCreator constructors
    this.tableMetadata = parquetMetadataProvider.getTableMetadata(tableLocation, tableName);
    this.partitions = parquetMetadataProvider.getPartitionsMetadata(tableLocation, tableName);

    this.rowGroups = parquetMetadataProvider.getRowGroupsMeta();
    this.files = parquetMetadataProvider.getFilesMetadata(tableLocation, tableName);
    this.usedMetadataCache = parquetMetadataProvider.isUsedMetadataCache();
    this.entries = parquetMetadataProvider.getEntries();
    this.partitionColumns = parquetMetadataProvider.getPartitionColumns();
    this.fileSet = parquetMetadataProvider.fileSet;

    init();
  }

  // TODO: replace constructors by ParquetTableMetadataCreator usage and
  //  add method for creating ParquetGroupScan
  public ParquetGroupScan(String userName,
                          FileSelection selection,
                          ParquetFormatPlugin formatPlugin,
                          List<SchemaPath> columns,
                          ParquetReaderConfig readerConfig) throws IOException {
    this(userName, selection, formatPlugin, columns, readerConfig, ValueExpressions.BooleanExpression.TRUE);
  }

  public ParquetGroupScan(String userName,
                          FileSelection selection,
                          ParquetFormatPlugin formatPlugin,
                          List<SchemaPath> columns,
                          ParquetReaderConfig readerConfig,
                          LogicalExpression filter) throws IOException {
    super(userName, columns, new ArrayList<>(), readerConfig, filter);

    this.formatPlugin = formatPlugin;
    this.formatConfig = formatPlugin.getConfig();
    this.cacheFileRoot = selection.getCacheFileRoot();

    DrillFileSystem fs = ImpersonationUtil.createFileSystem(ImpersonationUtil.resolveUserName(userName), formatPlugin.getFsConf());
    metadataProvider = new ParquetTableMetadataProvider(selection, readerConfig, fs,
        formatConfig.areCorruptDatesAutoCorrected());
    ParquetTableMetadataProvider parquetMetadataProvider = (ParquetTableMetadataProvider) this.metadataProvider;

    this.selectionRoot = parquetMetadataProvider.getSelectionRoot();
    String tableLocation = selectionRoot; // the same now in ParquetTableMetadataCreator constructors
    String tableName = selectionRoot; // the same now in ParquetTableMetadataCreator constructors
    this.tableMetadata = parquetMetadataProvider.getTableMetadata(tableLocation, tableName);

    this.rowGroups = parquetMetadataProvider.getRowGroupsMeta();
    this.files = parquetMetadataProvider.getFilesMetadata(tableLocation, tableName);
    this.partitions = parquetMetadataProvider.getPartitionsMetadata(tableLocation, tableName);
    this.usedMetadataCache = parquetMetadataProvider.isUsedMetadataCache();
    this.entries = parquetMetadataProvider.getEntries();
    this.partitionColumns = parquetMetadataProvider.getPartitionColumns();
    this.fileSet = parquetMetadataProvider.fileSet;

    // TODO: initialize TableMetadata, FileMetadata and RowGroupMetadata from
    //  parquetTableMetadata if it wasn't fetched from the metastore using ParquetTableMetadataCreator

    init();
  }

  private ParquetGroupScan(ParquetGroupScan that) {
    this(that, null);
  }

  private ParquetGroupScan(ParquetGroupScan that, FileSelection selection) {
    super(that);
    this.formatConfig = that.formatConfig;
    this.formatPlugin = that.formatPlugin;
    this.selectionRoot = that.selectionRoot;
    if (selection != null) {
      this.cacheFileRoot = selection.getCacheFileRoot();
    } else {
      this.cacheFileRoot = that.cacheFileRoot;
    }
    this.usedMetadataCache = that.usedMetadataCache;
  }

  // getters for serialization / deserialization start
  @JsonProperty("format")
  public ParquetFormatConfig getFormatConfig() {
    return formatConfig;
  }

  @JsonProperty("storage")
  public StoragePluginConfig getEngineConfig() {
    return formatPlugin.getStorageConfig();
  }

  @JsonProperty
  public String getSelectionRoot() {
    return selectionRoot;
  }

  @JsonProperty
  public String getCacheFileRoot() {
    return cacheFileRoot;
  }
  // getters for serialization / deserialization end

  @Override
  public ParquetRowGroupScan getSpecificScan(int minorFragmentId) {
    return new ParquetRowGroupScan(getUserName(), formatPlugin, getReadEntries(minorFragmentId), columns, readerConfig, selectionRoot, filter);
  }

  @Override
  public PhysicalOperator getNewWithChildren(List<PhysicalOperator> children) {
    Preconditions.checkArgument(children.isEmpty());
    return new ParquetGroupScan(this);
  }

  @Override
  public GroupScan clone(List<SchemaPath> columns) {
    ParquetGroupScan newScan = new ParquetGroupScan(this);
    newScan.columns = columns;
    return newScan;
  }

  @Override
  public ParquetGroupScan clone(FileSelection selection) throws IOException {
    // TODO: rewrite in accordance to the new logic
    ParquetGroupScan newScan = new ParquetGroupScan(this, selection);
    newScan.modifyFileSelection(selection);
    newScan.init();
    return newScan;
  }

  private List<ReadEntryWithPath> entries() {
    return entries;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ParquetGroupScan [");
    builder.append("entries=").append(entries());
    builder.append(", selectionRoot=").append(selectionRoot);
    // TODO: solve whether print entries when no pruning is done or list of files
    //  and the actual number instead of root and 1 file...
    builder.append(", numFiles=").append(getEntries().size());
    builder.append(", numRowGroups=").append(rowGroups.size());
    builder.append(", usedMetadataFile=").append(usedMetadataCache);

    String filterString = getFilterString();
    if (!filterString.isEmpty()) {
      builder.append(", filter=").append(filterString);
    }

    if (usedMetadataCache) {
      // For EXPLAIN, remove the URI prefix from cacheFileRoot.  If cacheFileRoot is null, we
      // would have read the cache file from selectionRoot
      String cacheFileRootString = (cacheFileRoot == null) ?
          Path.getPathWithoutSchemeAndAuthority(new Path(selectionRoot)).toString() :
          Path.getPathWithoutSchemeAndAuthority(new Path(cacheFileRoot)).toString();
      builder.append(", cacheFileRoot=").append(cacheFileRootString);
    }

    builder.append(", columns=").append(columns);
    builder.append("]");

    return builder.toString();
  }

  // overridden protected methods block start
  @Override
  protected AbstractParquetGroupScan cloneWithFileSelection(Collection<String> filePaths) throws IOException {
    FileSelection newSelection = new FileSelection(null, new ArrayList<>(filePaths), getSelectionRoot(), cacheFileRoot, false);
    return clone(newSelection);
  }

  @Override
  protected RowGroupScanBuilder getBuilder() {
    return new ParquetGroupScanBuilder(this);
  }

  @Override
  protected Collection<DrillbitEndpoint> getDrillbits() {
    return formatPlugin.getContext().getBits();
  }

  @Override
  protected boolean supportsFileImplicitColumns() {
    return selectionRoot != null;
  }

  @Override
  protected List<String> getPartitionValues(RowGroupInfo rowGroupInfo) {
    return ColumnExplorer.listPartitionValues(rowGroupInfo.getPath(), selectionRoot);
  }
  // overridden protected methods block end

  private static class ParquetGroupScanBuilder extends RowGroupScanBuilder {
    private final ParquetGroupScan source;

    public ParquetGroupScanBuilder(ParquetGroupScan source) {
      this.source = source;
    }

    @Override
    public AbstractMetadataGroupScan build() {
      ParquetGroupScan groupScan = new ParquetGroupScan(source);
      groupScan.tableMetadata = tableMetadata != null && !tableMetadata.isEmpty() ? tableMetadata.get(0) : null;
      groupScan.partitions = partitions != null ? partitions : Collections.emptyList();
      groupScan.files = files != null ? files : Collections.emptyList();
      groupScan.rowGroups = rowGroups != null ? rowGroups : Collections.emptyList();
      groupScan.partitionColumns = source.partitionColumns;
      // since builder is used when pruning happens, entries and fileSet should be expanded
      if (!groupScan.files.isEmpty()) {
        groupScan.entries = groupScan.files.stream()
            .map(file -> new ReadEntryWithPath(file.getLocation()))
            .collect(Collectors.toList());
      } else if (!groupScan.rowGroups.isEmpty()) {
        groupScan.entries = groupScan.rowGroups.stream()
            .map(RowGroupMetadata::getLocation)
            .distinct()
            .map(ReadEntryWithPath::new)
            .collect(Collectors.toList());
      }

      groupScan.fileSet = groupScan.files.stream()
        .map(FileMetadata::getLocation)
        .collect(Collectors.toSet());

      return groupScan;
    }
  }
}
