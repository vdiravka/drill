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

public enum GroupBySupport implements com.dyuproject.protostuff.EnumLite<GroupBySupport>
{
    GB_NONE(1),
    GB_SELECT_ONLY(2),
    GB_BEYOND_SELECT(3),
    GB_UNRELATED(4);
    
    public final int number;
    
    private GroupBySupport (int number)
    {
        this.number = number;
    }
    
    public int getNumber()
    {
        return number;
    }
    
    public static GroupBySupport valueOf(int number)
    {
        switch(number) 
        {
            case 1: return GB_NONE;
            case 2: return GB_SELECT_ONLY;
            case 3: return GB_BEYOND_SELECT;
            case 4: return GB_UNRELATED;
            default: return null;
        }
    }
}
