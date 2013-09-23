/*
 * Copyright 2013 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.surfnet.cruncher.message;


import static java.util.Calendar.*;
import static java.util.Calendar.MONDAY;
import static java.util.Calendar.YEAR;
import static org.junit.Assert.assertEquals;
import static org.surfnet.cruncher.message.Aggregator.aggregationRecordHash;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.surfnet.cruncher.model.LoginEntry;
import org.surfnet.cruncher.unittest.config.SpringConfigurationForTest;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfigurationForTest.class)
@Transactional
@TransactionConfiguration(defaultRollback=true)
public class AggregatorTest {
  private static final Logger LOG = LoggerFactory.getLogger(AggregatorTest.class);

  @Inject
  private Aggregator aggregator;

  @Inject
  private JdbcTemplate cruncherJdbcTemplate;

  private String sqlRowCountAggregated = "select count(*) from aggregated_log_logins";

  @Test(expected=IllegalArgumentException.class)
  public void aggregateLoginNull() throws Exception {
    aggregator.aggregateLogin(null);
  }

  @Test
  public void testRun() {
    aggregator.run();
    Calendar instance = Calendar.getInstance();
    instance.set(2012, 3, 21);
    String hash = aggregationRecordHash("idp2", "sp1", instance.getTime());
 
    long entryCount = cruncherJdbcTemplate.queryForLong("select entrycount from aggregated_log_logins where datespidphash = ?", hash);
    assertEquals(2, entryCount);
    long timestamp = cruncherJdbcTemplate.queryForLong("select aggregatepoint from aggregate_meta_data");
    assertEquals(20009L, timestamp);
    
    String userHash = aggregationRecordHash("idp2:user_1","sp1");
    long lastlogin = cruncherJdbcTemplate.queryForObject("select loginstamp from user_log_logins where usersphash = '" + userHash+"'", new RowMapper<Long>() {

      @Override
      public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getTimestamp(1).getTime();
      }
      
    });
    instance.setTimeInMillis(lastlogin);
    
    //2012-02-20 11:48:42
    assertEquals(20, instance.get(DAY_OF_MONTH));
    assertEquals(1, instance.get(MONDAY));
    assertEquals(2012, instance.get(YEAR));
  }

  @Test
  public void aggregateEmptyList() {
    aggregator.aggregateLogin(Collections.<LoginEntry>emptyList());
  }

  @Test
  public void aggregateList() {
    int rowCountBefore = cruncherJdbcTemplate.queryForInt(sqlRowCountAggregated);
    LoginEntry loginEntry = new LoginEntry(0L, "someIdp", "marker0", new Date(), "someSp", "", "");
    LoginEntry loginEntry2 = new LoginEntry(1L, "someIdp", "marker0", new Date(), "someSp", "", "");

    aggregator.aggregateLogin(Arrays.asList(loginEntry, loginEntry2));

    int rowCountAfter = cruncherJdbcTemplate.queryForInt(sqlRowCountAggregated);
    assertEquals("Aggregation of 2 records should result in 1 added rows", rowCountBefore + 1, rowCountAfter);
    int aggregatedCount = cruncherJdbcTemplate.queryForInt("select entrycount from aggregated_log_logins where idpentityname like 'marker0'");
    assertEquals("Aggegrated records should count 2", 2, aggregatedCount);
  }

  @Test
  public void aggregateDifferentSpIdp() {
    int rowCountBefore = cruncherJdbcTemplate.queryForInt(sqlRowCountAggregated);
    LoginEntry loginEntry1 = new LoginEntry(0L, "someIdp1", "marker1", new Date(), "someSp1", "", "");
    LoginEntry loginEntry2 = new LoginEntry(1L, "someIdp2", "marker1", new Date(), "someSp1", "", "");
    LoginEntry loginEntry3 = new LoginEntry(2L, "someIdp1", "marker1", new Date(), "someSp2", "", "");
    LoginEntry loginEntry4 = new LoginEntry(3L, "someIdp2", "marker1", new Date(), "someSp2", "", "");

    aggregator.aggregateLogin(Arrays.asList(loginEntry1, loginEntry2, loginEntry3, loginEntry4));

    int rowCountAfter = cruncherJdbcTemplate.queryForInt(sqlRowCountAggregated);
    assertEquals("Aggregation of 4 records of different sp/idps should result in 4 added rows", rowCountBefore + 4, rowCountAfter);
    LOG.debug("Contents of aggregated_log_logins: {}", cruncherJdbcTemplate.queryForList("select * from aggregated_log_logins"));
    List<Integer> aggregatedCount = cruncherJdbcTemplate.queryForList("select entrycount from aggregated_log_logins where idpentityname like 'marker1'", Integer.class);
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(0));
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(1));
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(2));
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(3));
  }
  
  @Test
  public void testUniqueLoginInsert() {
    LoginEntry loginEntry = new LoginEntry(0L, "someIdp", "marker0", new Date(), "someSp", "", "user-1");
    LoginEntry loginEntry2 = new LoginEntry(1L, "someIdp", "marker0", new Date(), "someSp", "", "user-1");
    LoginEntry loginEntry3 = new LoginEntry(2L, "someIdp", "marker0", new Date(), "someSp", "", "user-2");

    aggregator.aggregateLogin(Arrays.asList(loginEntry, loginEntry2, loginEntry3));

    int rowCount = cruncherJdbcTemplate.queryForInt("select count(*) from user_unique_logins_cache;");
    assertEquals("Aggregation of 3 records should result in 2 added rows for the unique user table", 2, rowCount);
  }
  
  @Test
  public void testUniqueLoginUpdateFromCache() {
    Calendar now = new GregorianCalendar();
    now.add(MONTH, -2);
    
    LoginEntry loginEntry = new LoginEntry(0L, "someIdp", "marker0", now.getTime(), "someSp", "", "user-1");
    LoginEntry loginEntry2 = new LoginEntry(1L, "someIdp", "marker0", now.getTime(), "someSp", "", "user-1");
    LoginEntry loginEntry3 = new LoginEntry(2L, "someIdp", "marker0", now.getTime(), "someSp", "", "user-2");
    
    aggregator.aggregateLogin(Arrays.asList(loginEntry, loginEntry2, loginEntry3));
    
    /* at this point the copy should not have been performed */
    int rowCount = cruncherJdbcTemplate.queryForInt("select count(*) from user_unique_logins;");
    assertEquals("unique user cache should have been updated to unique users table", 0, rowCount);
    
    now.add(MONTH, 3);
    LoginEntry loginEntry4 = new LoginEntry(3L, "someIdp", "marker0", now.getTime(), "someSp", "", "user-2");
    
    aggregator.aggregateLogin(Arrays.asList(loginEntry4));
    
    /* now the original set of logins should be aggregated in the unique users login table */
    rowCount = cruncherJdbcTemplate.queryForInt("select count(*) from user_unique_logins;");
    assertEquals("unique user cache should have been updated to unique users table", 1, rowCount);
    int entrycount  = cruncherJdbcTemplate.queryForInt("select entrycount from user_unique_logins where spentityid='someSp' and idpentityid='someIdp'");
    assertEquals("entry count for unique users should be 2", 2, entrycount);
  }
}
