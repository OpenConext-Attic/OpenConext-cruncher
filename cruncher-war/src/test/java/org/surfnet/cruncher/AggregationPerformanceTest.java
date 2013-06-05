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

package org.surfnet.cruncher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.surfnet.cruncher.config.SpringConfiguration;
import org.surfnet.cruncher.message.Aggregator;
import org.surfnet.cruncher.model.LoginEntry;
import org.surfnet.cruncher.unittest.config.SpringConfigurationForTest;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfigurationForTest.class)
public class AggregationPerformanceTest {


  private static final Logger LOG = LoggerFactory.getLogger(AggregationPerformanceTest.class);

  @Inject
  private Aggregator aggregator;

  @Inject
  private JdbcTemplate jdbcTemplate;

  public List<String> randomStrings(int nr) {
    List<String> ret = new ArrayList<String>(nr);
    for (int i = 0; i < nr; i++) {
       ret.add(UUID.randomUUID().toString());
    }
    return ret;
  }

  /*
   * This is a starting point for a 'real' integration test that measures performance
   */
  @Ignore
  @Test
  public void test() {
    List<String> sps = randomStrings(100);
    List<String> idps = randomStrings(100);
    List<String> users = randomStrings(10);

    Random randomGenerator = new Random();
    int nrOfLoginsPerDay = 10000;

    long dateOffset = System.currentTimeMillis();
    int nrOfDays = 10;
    for (int i = 0; i < nrOfDays; i++) {

      for (int j = 0; j < nrOfLoginsPerDay; j++) {
        Date date = new Date(dateOffset + (i * 86400 * 1000) + randomGenerator.nextInt(1000*86400));


        String idp = idps.get(randomGenerator.nextInt(idps.size()));
        String sp = sps.get(randomGenerator.nextInt(sps.size()));
        String user = users.get(randomGenerator.nextInt(users.size()));
        LoginEntry login = new LoginEntry(idp, idp, date, sp, sp, "", user, "");
        aggregator.aggregateLogin(Arrays.asList(login));
      }
    }
  }
}
