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
package org.apache.drill.exec.physical.impl.join;

import org.apache.drill.categories.OperatorTest;
import org.apache.drill.exec.planner.physical.PlannerSettings;
import org.apache.drill.test.BaseTestQuery;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.nio.file.Paths;

import static org.apache.drill.PlanTestBase.testPlanMatchingPatterns;
import static org.junit.Assert.assertEquals;

@Category(OperatorTest.class)
public class TestJoinEmptyTable extends JoinTestBase {

  private static final String HJ_PATTERN = "HashJoin";
  private static final String MJ_PATTERN = "MergeJoin";
  private static final String NLJ_PATTERN = "NestedLoopJoin";

  private static final String EMPTY_DIRECTORY = "empty_directory";

  @BeforeClass
  public static void setupTestFiles() {
    dirTestWatcher.makeTestTmpSubDir(Paths.get(EMPTY_DIRECTORY));
  }

  /** HashJoin with left empty dir table */
  @Test
  public void testHashJoinWithLeftEmptyDirTable() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from dfs.tmp.`%s` t1 inner join cp.`employee.json` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 0;

      enableJoin(true, false, false);
      testPlanMatchingPatterns(query, new String[]{HJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** HashJoin with left empty dir table */
  @Test
  public void testHashJoinWithRightEmptyDirTable() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from cp.`employee.json` t1 inner join dfs.tmp.`%s` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 0;

      enableJoin(true, false, false);
      testPlanMatchingPatterns(query, new String[]{HJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** HashJoin with left empty dir table */
  @Test
  public void testHashJoinWithBothEmptyDirTables() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from dfs.tmp.`%1$s` t1 inner join dfs.tmp.`%1$s` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 0;

      enableJoin(true, false, false);
      testPlanMatchingPatterns(query, new String[]{HJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** MergeJoin with left empty dir table, MergeJoin */
  @Test
  public void testMergeJoinWithLeftEmptyDirTable() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from dfs.tmp.`%s` t1 inner join cp.`employee.json` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 0;

      enableJoin(false, true, false);
      testPlanMatchingPatterns(query, new String[]{MJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** MergeJoin with left empty dir table */
  @Test
  public void testMergeJoinWithRightEmptyDirTable() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from cp.`employee.json` t1 inner join dfs.tmp.`%s` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 0;

      enableJoin(false, true, false);
      testPlanMatchingPatterns(query, new String[]{MJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** MergeJoin with left empty dir table */
  @Test
  public void testMergeJoinWithBothEmptyDirTables() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from dfs.tmp.`%1$s` t1 inner join dfs.tmp.`%1$s` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 0;

      enableJoin(false, true, false);
      testPlanMatchingPatterns(query, new String[]{MJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** NestedLoop with left empty dir table */
  @Test
  public void testNestedLoopJoinWithLeftEmptyDirTable() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from dfs.tmp.`%s` t1 inner join cp.`employee.json` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 0;

      enableJoin(false, false, true);
      testPlanMatchingPatterns(query, new String[]{NLJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** NestedLoop with left empty dir table */
  @Test
  public void testNestedLoopJoinWithRightEmptyDirTable() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from cp.`employee.json` t1 inner join dfs.tmp.`%s` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 0;

      enableJoin(false, false, true);
      testPlanMatchingPatterns(query, new String[]{NLJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** NestedLoop with left empty dir table */
  @Test
  public void testNestedLoopJoinWithBothEmptyDirTables() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from dfs.tmp.`%1$s` t1 inner join dfs.tmp.`%1$s` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 0;

      enableJoin(false, false, true);
      testPlanMatchingPatterns(query, new String[]{NLJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** LeftJoin empty dir table, HashJoin */
  @Test
  public void testLeftHashJoinEmptyDirTable() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from cp.`employee.json` t1 left join dfs.tmp.`%s` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 1155;

      enableJoin(true, false, false);
      testPlanMatchingPatterns(query, new String[]{HJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** LeftJoin empty dir table, MergeJoin */
  @Test
  public void testLeftMergeJoinEmptyDirTable() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from cp.`employee.json` t1 left join dfs.tmp.`%s` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 1155;

      enableJoin(false, true, false);
      testPlanMatchingPatterns(query, new String[]{MJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }

  /** LeftJoin empty dir table, NestedLoopJoin */
  @Test
  public void testLeftNestedLoopJoinEmptyDirTable() throws Exception {
    try {
      String query = String.format("select t1.`employee_id`, t1.`full_name`, t2.`employee_id`, t2.`full_name` " +
          "from cp.`employee.json` t1 left join dfs.tmp.`%s` t2 on t1.`full_name` = t2.`full_name`", EMPTY_DIRECTORY);
      final int expectedRecordCount = 1155;

      enableJoin(false, false, true);
      testPlanMatchingPatterns(query, new String[]{NLJ_PATTERN}, new String[]{});
      final int actualRecordCount = testSql(query);
      assertEquals("Number of output rows", expectedRecordCount, actualRecordCount);
    } finally {
      resetJoinOptions();
    }
  }
}
