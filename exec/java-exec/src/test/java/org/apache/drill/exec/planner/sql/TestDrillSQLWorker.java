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
package org.apache.drill.exec.planner.sql;

import static org.junit.Assert.assertEquals;

import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.drill.BaseTestQuery;
import org.apache.drill.exec.planner.physical.PlannerSettings;
import org.junit.Test;

public class TestDrillSQLWorker extends BaseTestQuery {

  private void validateFormattedIs(String sql, SqlParserPos pos, String expected) {
    String formatted = SqlConverter.formatSQLParsingError(sql, pos);
    assertEquals(expected, formatted);
  }

  @Test
  public void testErrorFormating() {
    String sql = "Select * from Foo\nwhere tadadidada;\n";
    validateFormattedIs(sql, new SqlParserPos(1, 2),
        "Select * from Foo\n"
      + " ^\n"
      + "where tadadidada;\n");
    validateFormattedIs(sql, new SqlParserPos(2, 2),
        "Select * from Foo\n"
      + "where tadadidada;\n"
      + " ^\n" );
    validateFormattedIs(sql, new SqlParserPos(1, 10),
        "Select * from Foo\n"
      + "         ^\n"
      + "where tadadidada;\n");
    validateFormattedIs(sql, new SqlParserPos(-11, -10), sql);
    validateFormattedIs(sql, new SqlParserPos(0, 10), sql);
    validateFormattedIs(sql, new SqlParserPos(100, 10), sql);
  }

  @Test
  public void testDoubleQuotesForQuotingIdentifiers() throws Exception {
    try {
      test("ALTER SESSION SET `%s` = '%s'", PlannerSettings.QUOTING_IDENTIFIERS_CHARACTER_KEY,
          Quoting.DOUBLE_QUOTE.string);
      testBuilder()
          .sqlQuery("select \"employee_id\", \"full_name\" from cp.\"employee.json\" limit 1")
          .ordered()
          .baselineColumns("employee_id", "full_name")
          .baselineValues(1L, "Sheri Nowmer")
          .go();

      // Back-tick default quotes should work even when other quoting character is chosen
      testBuilder()
          .sqlQuery("select `employee_id`, `full_name` from cp.`employee.json` limit 1")
          .ordered()
          .baselineColumns("employee_id", "full_name")
          .baselineValues(1L, "Sheri Nowmer")
          .go();

      // Mix of different quotes in the one SQL statement is not acceptable
      errorMsgTestHelper("select \"employee_id\", \"full_name\" from cp.`employee.json` limit 1", "Encountered: \"`\"");
    } finally {
      test("ALTER SESSION RESET `%s`", PlannerSettings.QUOTING_IDENTIFIERS_CHARACTER_KEY);
    }
  }

  @Test
  public void testBracketsForQuotingIdentifiers() throws Exception {
    try {
      test("ALTER SESSION SET `%s` = '%s'", PlannerSettings.QUOTING_IDENTIFIERS_CHARACTER_KEY,
          Quoting.BRACKET.string);
      testBuilder()
          .sqlQuery("select [employee_id], [full_name] from cp.[employee.json] limit 1")
          .ordered()
          .baselineColumns("employee_id", "full_name")
          .baselineValues(1L, "Sheri Nowmer")
          .go();
      // Other quoting characters (except default back-tick) is not acceptable while particular one is chosen
      errorMsgTestHelper("select \"employee_id\", \"full_name\" from cp.\"employee.json\" limit 1",
          "Encountered \"\\\"\"");
    } finally {
      test("ALTER SESSION RESET `%s`", PlannerSettings.QUOTING_IDENTIFIERS_CHARACTER_KEY);
    }
  }
}
