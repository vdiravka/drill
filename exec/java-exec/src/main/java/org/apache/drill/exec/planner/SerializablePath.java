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
package org.apache.drill.exec.planner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.hadoop.fs.Path;

import java.io.IOException;

public class SerializablePath extends SimpleModule {
  private static final String NAME = "CustomIntervalModule";

  public SerializablePath() {
//    super(NAME, VersionUtil.versionFor(SerializablePath.class));
    super(NAME);
    addSerializer(Path.class, new PathSerializer());
  }

  public static class PathSerializer extends JsonSerializer<Path> {

//    protected PathSerializer(Class<Path> t) {
//      super(t);
//    }

    @Override
    public void serialize(Path value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//      gen.writeStartObject();
      gen.writeString(value.toUri().getPath());
//      gen.writeEndObject();
    }
  }

  public static class PathDeserializer extends StdDeserializer<Path> {

    public PathDeserializer() {
      super(Path.class);
    }

    @Override
    public Path deserialize(com.fasterxml.jackson.core.JsonParser p, com.fasterxml.jackson.databind.DeserializationContext ctxt) throws IOException {
      System.out.println(p.getText());
      return new Path(p.getText());
    }
  }

}
