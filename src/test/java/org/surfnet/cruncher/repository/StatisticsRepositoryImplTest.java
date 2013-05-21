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

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.surfnet.cruncher.config.SpringConfiguration;
import org.surfnet.cruncher.model.LoginData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfiguration.class)
public class StatisticsRepositoryImplTest  {

  @Inject
  private StatisticsRepositoryImpl statisticsRepository;

  @Inject
  private JdbcTemplate jdbcTemplate;

  @Test
  public void getUniqueLogins() throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Timestamp start = new Timestamp(0L);
    Timestamp end = new Timestamp(System.currentTimeMillis());
    List<LoginData> result = statisticsRepository.getUniqueLogins(start, end, "sp1", "idp1");
    assertNotNull(result);
    assertEquals(12, result.size());
    
    result = statisticsRepository.getUniqueLogins(start, end, "unknown", "idp1");
    assertNotNull(result);
    assertEquals(0, result.size());
    
    Date startDate = sdf.parse("2013-01-01");
    Date endDate =sdf.parse("2013-01-04");
    result = statisticsRepository.getUniqueLogins(new Timestamp(startDate.getTime()), new Timestamp(endDate.getTime()), null, "idp1");
    assertNotNull(result);
    assertEquals(4, result.size());
    LoginData first = result.get(0);
    assertEquals(20, first.getTotal());
    assertEquals("idp1", first.getIdpEntityId());
  }
}
