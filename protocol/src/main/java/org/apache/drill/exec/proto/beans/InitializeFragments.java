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
import java.util.ArrayList;
import java.util.List;

import com.dyuproject.protostuff.GraphIOUtil;
import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Message;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.Schema;

public final class InitializeFragments implements Externalizable, Message<InitializeFragments>, Schema<InitializeFragments>
{

    public static Schema<InitializeFragments> getSchema()
    {
        return DEFAULT_INSTANCE;
    }

    public static InitializeFragments getDefaultInstance()
    {
        return DEFAULT_INSTANCE;
    }

    static final InitializeFragments DEFAULT_INSTANCE = new InitializeFragments();

    
    private List<PlanFragment> fragment;

    public InitializeFragments()
    {
        
    }

    // getters and setters

    // fragment

    public List<PlanFragment> getFragmentList()
    {
        return fragment;
    }

    public InitializeFragments setFragmentList(List<PlanFragment> fragment)
    {
        this.fragment = fragment;
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

    public Schema<InitializeFragments> cachedSchema()
    {
        return DEFAULT_INSTANCE;
    }

    // schema methods

    public InitializeFragments newMessage()
    {
        return new InitializeFragments();
    }

    public Class<InitializeFragments> typeClass()
    {
        return InitializeFragments.class;
    }

    public String messageName()
    {
        return InitializeFragments.class.getSimpleName();
    }

    public String messageFullName()
    {
        return InitializeFragments.class.getName();
    }

    public boolean isInitialized(InitializeFragments message)
    {
        return true;
    }

    public void mergeFrom(Input input, InitializeFragments message) throws IOException
    {
        for(int number = input.readFieldNumber(this);; number = input.readFieldNumber(this))
        {
            switch(number)
            {
                case 0:
                    return;
                case 1:
                    if(message.fragment == null)
                        message.fragment = new ArrayList<PlanFragment>();
                    message.fragment.add(input.mergeObject(null, PlanFragment.getSchema()));
                    break;

                default:
                    input.handleUnknownField(number, this);
            }   
        }
    }


    public void writeTo(Output output, InitializeFragments message) throws IOException
    {
        if(message.fragment != null)
        {
            for(PlanFragment fragment : message.fragment)
            {
                if(fragment != null)
                    output.writeObject(1, fragment, PlanFragment.getSchema(), true);
            }
        }

    }

    public String getFieldName(int number)
    {
        switch(number)
        {
            case 1: return "fragment";
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
        __fieldMap.put("fragment", 1);
    }
    
}
