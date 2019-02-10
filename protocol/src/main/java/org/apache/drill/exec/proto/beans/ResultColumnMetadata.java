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
// Generated by http://code.google.com/p/protostuff/ ... DO NOT EDIT!
// Generated from protobuf

package org.apache.drill.exec.proto.beans;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.dyuproject.protostuff.GraphIOUtil;
import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Message;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;

public final class ResultColumnMetadata implements Externalizable, Message<ResultColumnMetadata>, Schema<ResultColumnMetadata>
{

    public static Schema<ResultColumnMetadata> getSchema()
    {
        return DEFAULT_INSTANCE;
    }

    public static ResultColumnMetadata getDefaultInstance()
    {
        return DEFAULT_INSTANCE;
    }

    static final ResultColumnMetadata DEFAULT_INSTANCE = new ResultColumnMetadata();

    
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String label;
    private String dataType;
    private Boolean isNullable;
    private int precision;
    private int scale;
    private Boolean signed;
    private int displaySize;
    private Boolean isAliased;
    private ColumnSearchability searchability;
    private ColumnUpdatability updatability;
    private Boolean autoIncrement;
    private Boolean caseSensitivity;
    private Boolean sortable;
    private String className;
    private Boolean isCurrency;

    public ResultColumnMetadata()
    {
        
    }

    // getters and setters

    // catalogName

    public String getCatalogName()
    {
        return catalogName;
    }

    public ResultColumnMetadata setCatalogName(String catalogName)
    {
        this.catalogName = catalogName;
        return this;
    }

    // schemaName

    public String getSchemaName()
    {
        return schemaName;
    }

    public ResultColumnMetadata setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
        return this;
    }

    // tableName

    public String getTableName()
    {
        return tableName;
    }

    public ResultColumnMetadata setTableName(String tableName)
    {
        this.tableName = tableName;
        return this;
    }

    // columnName

    public String getColumnName()
    {
        return columnName;
    }

    public ResultColumnMetadata setColumnName(String columnName)
    {
        this.columnName = columnName;
        return this;
    }

    // label

    public String getLabel()
    {
        return label;
    }

    public ResultColumnMetadata setLabel(String label)
    {
        this.label = label;
        return this;
    }

    // dataType

    public String getDataType()
    {
        return dataType;
    }

    public ResultColumnMetadata setDataType(String dataType)
    {
        this.dataType = dataType;
        return this;
    }

    // isNullable

    public Boolean getIsNullable()
    {
        return isNullable;
    }

    public ResultColumnMetadata setIsNullable(Boolean isNullable)
    {
        this.isNullable = isNullable;
        return this;
    }

    // precision

    public int getPrecision()
    {
        return precision;
    }

    public ResultColumnMetadata setPrecision(int precision)
    {
        this.precision = precision;
        return this;
    }

    // scale

    public int getScale()
    {
        return scale;
    }

    public ResultColumnMetadata setScale(int scale)
    {
        this.scale = scale;
        return this;
    }

    // signed

    public Boolean getSigned()
    {
        return signed;
    }

    public ResultColumnMetadata setSigned(Boolean signed)
    {
        this.signed = signed;
        return this;
    }

    // displaySize

    public int getDisplaySize()
    {
        return displaySize;
    }

    public ResultColumnMetadata setDisplaySize(int displaySize)
    {
        this.displaySize = displaySize;
        return this;
    }

    // isAliased

    public Boolean getIsAliased()
    {
        return isAliased;
    }

    public ResultColumnMetadata setIsAliased(Boolean isAliased)
    {
        this.isAliased = isAliased;
        return this;
    }

    // searchability

    public ColumnSearchability getSearchability()
    {
        return searchability == null ? ColumnSearchability.UNKNOWN_SEARCHABILITY : searchability;
    }

    public ResultColumnMetadata setSearchability(ColumnSearchability searchability)
    {
        this.searchability = searchability;
        return this;
    }

    // updatability

    public ColumnUpdatability getUpdatability()
    {
        return updatability == null ? ColumnUpdatability.UNKNOWN_UPDATABILITY : updatability;
    }

    public ResultColumnMetadata setUpdatability(ColumnUpdatability updatability)
    {
        this.updatability = updatability;
        return this;
    }

    // autoIncrement

    public Boolean getAutoIncrement()
    {
        return autoIncrement;
    }

    public ResultColumnMetadata setAutoIncrement(Boolean autoIncrement)
    {
        this.autoIncrement = autoIncrement;
        return this;
    }

    // caseSensitivity

    public Boolean getCaseSensitivity()
    {
        return caseSensitivity;
    }

    public ResultColumnMetadata setCaseSensitivity(Boolean caseSensitivity)
    {
        this.caseSensitivity = caseSensitivity;
        return this;
    }

    // sortable

    public Boolean getSortable()
    {
        return sortable;
    }

    public ResultColumnMetadata setSortable(Boolean sortable)
    {
        this.sortable = sortable;
        return this;
    }

    // className

    public String getClassName()
    {
        return className;
    }

    public ResultColumnMetadata setClassName(String className)
    {
        this.className = className;
        return this;
    }

    // isCurrency

    public Boolean getIsCurrency()
    {
        return isCurrency;
    }

    public ResultColumnMetadata setIsCurrency(Boolean isCurrency)
    {
        this.isCurrency = isCurrency;
        return this;
    }

    // java serialization

    public void readExternal(ObjectInput in) throws IOException
    {
        GraphIOUtil.mergeDelimitedFrom(in, this, this);
    }

    public void writeExternal(ObjectOutput out) throws IOException
    {
        GraphIOUtil.writeDelimitedTo(out, this, this);
    }

    // message method

    public Schema<ResultColumnMetadata> cachedSchema()
    {
        return DEFAULT_INSTANCE;
    }

    // schema methods

    public ResultColumnMetadata newMessage()
    {
        return new ResultColumnMetadata();
    }

    public Class<ResultColumnMetadata> typeClass()
    {
        return ResultColumnMetadata.class;
    }

    public String messageName()
    {
        return ResultColumnMetadata.class.getSimpleName();
    }

    public String messageFullName()
    {
        return ResultColumnMetadata.class.getName();
    }

    public boolean isInitialized(ResultColumnMetadata message)
    {
        return true;
    }

    public void mergeFrom(Input input, ResultColumnMetadata message) throws IOException
    {
        for(int number = input.readFieldNumber(this);; number = input.readFieldNumber(this))
        {
            switch(number)
            {
                case 0:
                    return;
                case 1:
                    message.catalogName = input.readString();
                    break;
                case 2:
                    message.schemaName = input.readString();
                    break;
                case 3:
                    message.tableName = input.readString();
                    break;
                case 4:
                    message.columnName = input.readString();
                    break;
                case 5:
                    message.label = input.readString();
                    break;
                case 6:
                    message.dataType = input.readString();
                    break;
                case 7:
                    message.isNullable = input.readBool();
                    break;
                case 8:
                    message.precision = input.readInt32();
                    break;
                case 9:
                    message.scale = input.readInt32();
                    break;
                case 10:
                    message.signed = input.readBool();
                    break;
                case 11:
                    message.displaySize = input.readInt32();
                    break;
                case 12:
                    message.isAliased = input.readBool();
                    break;
                case 13:
                    message.searchability = ColumnSearchability.valueOf(input.readEnum());
                    break;
                case 14:
                    message.updatability = ColumnUpdatability.valueOf(input.readEnum());
                    break;
                case 15:
                    message.autoIncrement = input.readBool();
                    break;
                case 16:
                    message.caseSensitivity = input.readBool();
                    break;
                case 17:
                    message.sortable = input.readBool();
                    break;
                case 18:
                    message.className = input.readString();
                    break;
                case 20:
                    message.isCurrency = input.readBool();
                    break;
                default:
                    input.handleUnknownField(number, this);
            }   
        }
    }


    public void writeTo(Output output, ResultColumnMetadata message) throws IOException
    {
        if(message.catalogName != null)
            output.writeString(1, message.catalogName, false);

        if(message.schemaName != null)
            output.writeString(2, message.schemaName, false);

        if(message.tableName != null)
            output.writeString(3, message.tableName, false);

        if(message.columnName != null)
            output.writeString(4, message.columnName, false);

        if(message.label != null)
            output.writeString(5, message.label, false);

        if(message.dataType != null)
            output.writeString(6, message.dataType, false);

        if(message.isNullable != null)
            output.writeBool(7, message.isNullable, false);

        if(message.precision != 0)
            output.writeInt32(8, message.precision, false);

        if(message.scale != 0)
            output.writeInt32(9, message.scale, false);

        if(message.signed != null)
            output.writeBool(10, message.signed, false);

        if(message.displaySize != 0)
            output.writeInt32(11, message.displaySize, false);

        if(message.isAliased != null)
            output.writeBool(12, message.isAliased, false);

        if(message.searchability != null)
             output.writeEnum(13, message.searchability.number, false);

        if(message.updatability != null)
             output.writeEnum(14, message.updatability.number, false);

        if(message.autoIncrement != null)
            output.writeBool(15, message.autoIncrement, false);

        if(message.caseSensitivity != null)
            output.writeBool(16, message.caseSensitivity, false);

        if(message.sortable != null)
            output.writeBool(17, message.sortable, false);

        if(message.className != null)
            output.writeString(18, message.className, false);

        if(message.isCurrency != null)
            output.writeBool(20, message.isCurrency, false);
    }

    public String getFieldName(int number)
    {
        switch(number)
        {
            case 1: return "catalogName";
            case 2: return "schemaName";
            case 3: return "tableName";
            case 4: return "columnName";
            case 5: return "label";
            case 6: return "dataType";
            case 7: return "isNullable";
            case 8: return "precision";
            case 9: return "scale";
            case 10: return "signed";
            case 11: return "displaySize";
            case 12: return "isAliased";
            case 13: return "searchability";
            case 14: return "updatability";
            case 15: return "autoIncrement";
            case 16: return "caseSensitivity";
            case 17: return "sortable";
            case 18: return "className";
            case 20: return "isCurrency";
            default: return null;
        }
    }

    public int getFieldNumber(String name)
    {
        final Integer number = __fieldMap.get(name);
        return number == null ? 0 : number.intValue();
    }

    private static final java.util.HashMap<String,Integer> __fieldMap = new java.util.HashMap<String,Integer>();
    static
    {
        __fieldMap.put("catalogName", 1);
        __fieldMap.put("schemaName", 2);
        __fieldMap.put("tableName", 3);
        __fieldMap.put("columnName", 4);
        __fieldMap.put("label", 5);
        __fieldMap.put("dataType", 6);
        __fieldMap.put("isNullable", 7);
        __fieldMap.put("precision", 8);
        __fieldMap.put("scale", 9);
        __fieldMap.put("signed", 10);
        __fieldMap.put("displaySize", 11);
        __fieldMap.put("isAliased", 12);
        __fieldMap.put("searchability", 13);
        __fieldMap.put("updatability", 14);
        __fieldMap.put("autoIncrement", 15);
        __fieldMap.put("caseSensitivity", 16);
        __fieldMap.put("sortable", 17);
        __fieldMap.put("className", 18);
        __fieldMap.put("isCurrency", 20);
    }
    
}
