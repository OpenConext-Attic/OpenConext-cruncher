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

import java.text.ParseException;
import java.util.List;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.surfnet.cruncher.config.SpringConfiguration;
import org.surfnet.cruncher.model.LoginData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfiguration.class)
public class StatisticsRepositoryImplTest {

  @Inject
  private StatisticsRepositoryImpl statisticsRepository;

  @Inject
  private JdbcTemplate jdbcTemplate;


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
