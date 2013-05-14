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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static junit.framework.Assert.*;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.surfnet.cruncher.config.SpringConfiguration;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.LoginEntry;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfiguration.class)
public class StatisticsRepositoryImplTest  {

  @Inject
  private StatisticsRepositoryImpl statisticsRepository;


  @Test(expected=IllegalArgumentException.class)
  public void aggregateLoginNull() throws Exception {
    statisticsRepository.aggregateLogin(null);
  }

  @Test
  public void aggregateEmptyList() {
    statisticsRepository.aggregateLogin(Collections.<LoginEntry>emptyList());
  }

  @Test
  public void aggregateList() {
    statisticsRepository.aggregateLogin(Arrays.asList(new LoginEntry()));
  }
  
  @Test
  public void getUniqueLogins() {
    Timestamp start = new Timestamp(0L);
    Timestamp end = new Timestamp(System.currentTimeMillis());
    List<LoginData> result = statisticsRepository.getUniqueLogins(start, end, "sp1", "idp1");
    assertNotNull(result);
    assertEquals(1, result.size());
  }
}
