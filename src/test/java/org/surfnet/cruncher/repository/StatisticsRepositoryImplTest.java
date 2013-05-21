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

package org.surfnet.cruncher.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.surfnet.cruncher.config.SpringConfiguration;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.LoginEntry;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfiguration.class)
public class StatisticsRepositoryImplTest {

  private static final Logger LOG = LoggerFactory.getLogger(StatisticsRepositoryImplTest.class);

  @Inject
  private StatisticsRepositoryImpl statisticsRepository;

  @Inject
  private JdbcTemplate jdbcTemplate;

  private String sqlRowCountAggregated = "select count(*) from aggregated_log_logins";

  @Test(expected = IllegalArgumentException.class)
  public void aggregateLoginNull() throws Exception {
    statisticsRepository.aggregateLogin(null);
  }

  @Test
  public void aggregateEmptyList() {
    statisticsRepository.aggregateLogin(Collections.<LoginEntry> emptyList());
  }

  @Test
  public void aggregateList() {
    int rowCountBefore = jdbcTemplate.queryForInt(sqlRowCountAggregated);
    LoginEntry loginEntry = new LoginEntry("someIdp", "marker0", new Date(), "someSp", "", "", "", "");
    LoginEntry loginEntry2 = new LoginEntry("someIdp", "marker0", new Date(), "someSp", "", "", "", "");

    statisticsRepository.aggregateLogin(Arrays.asList(loginEntry, loginEntry2));

    int rowCountAfter = jdbcTemplate.queryForInt(sqlRowCountAggregated);
    assertEquals("Aggregation of 2 records should result in 1 added rows", rowCountBefore + 1, rowCountAfter);
    int aggregatedCount = jdbcTemplate
        .queryForInt("select entrycount from aggregated_log_logins where idpentityname like 'marker0'");
    assertEquals("Aggegrated records should count 2", 2, aggregatedCount);
  }

  @Test
  public void aggregateDifferentSpIdp() {
    int rowCountBefore = jdbcTemplate.queryForInt(sqlRowCountAggregated);
    LoginEntry loginEntry1 = new LoginEntry("someIdp1", "marker1", new Date(), "someSp1", "", "", "", "");
    LoginEntry loginEntry2 = new LoginEntry("someIdp2", "marker1", new Date(), "someSp1", "", "", "", "");
    LoginEntry loginEntry3 = new LoginEntry("someIdp1", "marker1", new Date(), "someSp2", "", "", "", "");
    LoginEntry loginEntry4 = new LoginEntry("someIdp2", "marker1", new Date(), "someSp2", "", "", "", "");

    statisticsRepository.aggregateLogin(Arrays.asList(loginEntry1, loginEntry2, loginEntry3, loginEntry4));

    int rowCountAfter = jdbcTemplate.queryForInt(sqlRowCountAggregated);
    assertEquals("Aggregation of 4 records of different sp/idps should result in 4 added rows", rowCountBefore + 4,
        rowCountAfter);
    LOG.debug("Contents of aggregated_log_logins: {}", jdbcTemplate.queryForList("select * from aggregated_log_logins"));
    List<Integer> aggregatedCount = jdbcTemplate.queryForList(
        "select entrycount from aggregated_log_logins where idpentityname like 'marker1'", Integer.class);
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(0));
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(1));
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(2));
    assertEquals("Aggegrated records should all count 1", new Integer(1), aggregatedCount.get(3));

  }

  @Test
  public void getUniqueLogins() throws ParseException {
    LocalDate start = new LocalDate(0L);
    LocalDate end = new LocalDate(System.currentTimeMillis());
    List<LoginData> result = statisticsRepository.getUniqueLogins(start, end, "sp1", "idp1");
    assertNotNull(result);
    assertEquals(12, result.size());

    result = statisticsRepository.getUniqueLogins(start, end, "unknown", "idp1");
    assertNotNull(result);
    assertEquals(0, result.size());

    LocalDate startDate = new LocalDate(2013, 1, 1);
    LocalDate endDate = new LocalDate(2013, 1, 4);
    result = statisticsRepository.getUniqueLogins(startDate, endDate, null, "idp1");
    assertNotNull(result);
    assertEquals(8, result.size());
    LoginData first = result.get(0);
    assertEquals(20, first.getTotal());
    assertEquals("idp1", first.getIdpEntityId());
  }

  @Test
  public void getLogins() throws ParseException {
    LocalDate start = new LocalDate(2013, 1, 1);
    LocalDate end = new LocalDate(2013, 1, 12);
    List<LoginData> result = statisticsRepository.getLogins(start, end, "sp1", "idp1", null);
    assertNotNull(result);
    assertEquals(1, result.size());
    LoginData data = result.get(0);
    checkSp1Entry(data);
  }

  @Test
  public void getMultipleLogins() {
    LocalDate start = new LocalDate(2013, 1, 1);
    LocalDate end = new LocalDate(2013, 1, 12);
    List<LoginData> result = statisticsRepository.getLogins(start, end, null, "idp1", null);
    assertNotNull(result);
    assertEquals(2, result.size());
    LoginData first = result.get(0);
    LoginData second = result.get(1);
    if (first.getSpEntityId().equals("sp1")) {
      checkSp1Entry(first);
    } else {
      checkSp1Entry(second);
    }
  }

  private void checkSp1Entry(LoginData data) {
    assertEquals(240, data.getTotal());
    assertEquals(12, data.getData().size());
    assertEquals(20, (int) data.getData().get(0));
    assertEquals(20, (int) data.getData().get(6));
    assertEquals(20, (int) data.getData().get(11));
  }

  @Test
  public void testIllegalArguments() {
    LocalDate start = new LocalDate(2013, 3, 1);
    LocalDate end = new LocalDate(2013, 4, 2);
    
    try {
      statisticsRepository.getLogins(start, end, null, null, null);
      fail("Should have received an illegal argument exception");
    } catch (IllegalArgumentException iae) {
      // expected
    }
    
    try {
      statisticsRepository.getLogins(start, end, null, null, 0L);
      fail("Should have received an illegal argument exception");
    } catch (IllegalArgumentException iae) {
      // expected
    }
    
    try {
      statisticsRepository.getLogins(start, end, null, null, (1000L*60L*60L*24L));
      fail("Should have received an illegal argument exception");
    } catch (IllegalArgumentException iae) {
      // expected
    }
  }
  
  @Test
  public void testResponseWithZeros() {
    LocalDate start = new LocalDate(2013, 1, 10);
    LocalDate end = new LocalDate(2013, 1, 20);
    List<LoginData> result = statisticsRepository.getLogins(start, end, "sp1", "idp1", null);
    
    assertNotNull(result);
    assertEquals(1, result.size());
    LoginData loginData = result.get(0);
    assertEquals(11, loginData.getData().size());
    assertEquals(20, (int)loginData.getData().get(2));
    assertEquals(0, (int)loginData.getData().get(3));
    assertEquals(0, (int)loginData.getData().get(4));
    assertEquals(0, (int)loginData.getData().get(10));
  }
}
