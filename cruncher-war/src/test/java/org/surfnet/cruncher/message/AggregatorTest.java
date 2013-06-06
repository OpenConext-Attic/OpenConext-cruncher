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

import static java.util.Calendar.DAY_OF_MONTH;
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
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.surfnet.cruncher.model.LoginEntry;
import org.surfnet.cruncher.unittest.config.SpringConfigurationForTest;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfigurationForTest.class)
public class AggregatorTest {
  private static final Logger LOG = LoggerFactory.getLogger(AggregatorTest.class);

  @Inject
  private Aggregator aggregator;

  @Inject
  private JdbcTemplate jdbcTemplate;

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
 
    long entryCount = jdbcTemplate.queryForLong("select entrycount from aggregated_log_logins where datespidphash = ?", hash);
    assertEquals(2, entryCount);
    long timestamp = jdbcTemplate.queryForLong("select aggregatepoint from aggregate_meta_data");
    assertEquals(1335088121000L, timestamp);
    
    String userHash = aggregationRecordHash("user_1","sp1");
    long lastlogin = jdbcTemplate.queryForObject("select loginstamp from user_log_logins where usersphash = '" + userHash+"'", new RowMapper<Long>() {

      @Override
      public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getTimestamp(1).getTime();
      }
      
    });
    instance.setTimeInMillis(lastlogin);
    
    //2012-04-20 11:48:41
    assertEquals(20, instance.get(DAY_OF_MONTH));
    assertEquals(3, instance.get(MONDAY));
    assertEquals(2012, instance.get(YEAR));
  }

  @Test
  public void aggregateEmptyList() {
    aggregator.aggregateLogin(Collections.<LoginEntry>emptyList());
  }

  @Test
  public void aggregateList() {
    int rowCountBefore = jdbcTemplate.queryForInt(sqlRowCountAggregated);
    LoginEntry loginEntry = new LoginEntry("someIdp", "marker0", new Date(), "someSp", "", "");
    LoginEntry loginEntry2 = new LoginEntry("someIdp", "marker0", new Date(), "someSp", "", "");

    aggregator.aggregateLogin(Arrays.asList(loginEntry, loginEntry2));

    int rowCountAfter = jdbcTemplate.queryForInt(sqlRowCountAggregated);
    assertEquals("Aggregation of 2 records should result in 1 added rows", rowCountBefore + 1, rowCountAfter);
    int aggregatedCount = jdbcTemplate.queryForInt("select entrycount from aggregated_log_logins where idpentityname like 'marker0'");
    assertEquals("Aggegrated records should count 2", 2, aggregatedCount);
  }

  @Test
  public void aggregateDifferentSpIdp() {
    int rowCountBefore = jdbcTemplate.queryForInt(sqlRowCountAggregated);
    LoginEntry loginEntry1 = new LoginEntry("someIdp1", "marker1", new Date(), "someSp1", "", "");
    LoginEntry loginEntry2 = new LoginEntry("someIdp2", "marker1", new Date(), "someSp1", "", "");
    LoginEntry loginEntry3 = new LoginEntry("someIdp1", "marker1", new Date(), "someSp2", "", "");
    LoginEntry loginEntry4 = new LoginEntry("someIdp2", "marker1", new Date(), "someSp2", "", "");

    aggregator.aggregateLogin(Arrays.asList(loginEntry1, loginEntry2, loginEntry3, loginEntry4));

    int rowCountAfter = jdbcTemplate.queryForInt(sqlRowCountAggregated);
    assertEquals("Aggregation of 4 records of different sp/idps should result in 4 added rows", rowCountBefore + 4, rowCountAfter);
    LOG.debug("Contents of aggregated_log_logins: {}", jdbcTemplate.queryForList("select * from aggregated_log_logins"));
    List<Integer> aggregatedCount = jdbcTemplate.queryForList("select entrycount from aggregated_log_logins where idpentityname like 'marker1'", Integer.class);
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(0));
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(1));
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(2));
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(3));
  }

  /*
   * This test is more useful with a debugger attached. But at least if no
   * exception occur this is a good sign.
   */
  @Test
  public void testConcurrency() throws InterruptedException {
    for (int j = 0; j < 5; j++) {
      List<Thread> threads = new ArrayList<Thread>();

      for (int i = 0; i < 20; i++) {
        Thread t = new Thread(new AggregatorTread(aggregator));
        threads.add(t);
        t.start();
      }

      for (Thread t : threads) {
        t.join();
      }
    }
  }
}

class AggregatorTread implements Runnable {
  private final Aggregator aggregator;
  
  public AggregatorTread(final Aggregator aggregator) {
    this.aggregator = aggregator;
  }
  
  @Override
  public void run() {
    aggregator.run();
  }
}
