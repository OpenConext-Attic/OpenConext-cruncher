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

package org.surfnet.cruncher.resource;

import junit.framework.Assert;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.surfnet.cruncher.message.Aggregator;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.SpStatistic;
import org.surfnet.cruncher.unittest.config.SpringConfigurationForTest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfigurationForTest.class)
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class CruncherResourceTest {

  @Inject
  private CruncherResource cruncherResource;

  @Inject
  private Aggregator aggregator;

  @Inject
  private JdbcTemplate cruncherJdbcTemplate;

  private HttpServletRequest request = null; //currently never really used

  @Test
  public void getLogins() {
    LocalDate start = new LocalDate(2013, 1, 1);
    LocalDate end = new LocalDate(2013, 1, 12);
    Response response = cruncherResource.getLoginsPerInterval(request, start.toDate().getTime(), end.toDate().getTime(), "idp1", "sp1");
    List<LoginData> result = (List<LoginData>) response.getEntity();
    assertNotNull(result);
    assertEquals(1, result.size());
    LoginData data = result.get(0);
    checkSp1Entry(data);
  }

  @Test
  public void getMultipleLogins() {
    LocalDate start = new LocalDate(2013, 1, 1);
    LocalDate end = new LocalDate(2013, 1, 12);
    Response response = cruncherResource.getLoginsPerInterval(request, start.toDate().getTime(), end.toDate().getTime(), "idp1", null);
    List<LoginData> result = (List<LoginData>) response.getEntity();
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
    assertEquals("idp1", data.getIdpEntityId());
    assertEquals("idp1_name", data.getIdpname());
    assertEquals("sp1", data.getSpEntityId());
    assertEquals("sp1_name", data.getSpName());
    assertEquals(240, data.getTotal());
    assertEquals(12, data.getData().size());
    assertEquals(20, (int) data.getData().get(0));
    assertEquals(20, (int) data.getData().get(6));
    assertEquals(20, (int) data.getData().get(11));
  }

  @Test
  public void testIllegalArguments() {
    cruncherResource.getLoginsPerInterval(request, 0L, 0L, null, null).getEntity();
    try {
      cruncherResource.getLoginsPerInterval(request, null, null, null, null).getEntity();
      fail("illegal start and end date may not be null");
    } catch (IllegalArgumentException e) {
      //expected
    }
  }

  @Test
  public void testResponseWithZeros() {
    LocalDate start = new LocalDate(2013, 1, 10);
    LocalDate end = new LocalDate(2013, 1, 20);
    Response response = cruncherResource.getLoginsPerInterval(request, start.toDate().getTime(), end.toDate().getTime(), "idp1", "sp1");
    List<LoginData> result = (List<LoginData>) response.getEntity();

    assertNotNull(result);
    assertEquals(1, result.size());
    LoginData loginData = result.get(0);
    assertEquals(11, loginData.getData().size());
    assertEquals(20, (int) loginData.getData().get(2));
    assertEquals(0, (int) loginData.getData().get(3));
    assertEquals(0, (int) loginData.getData().get(4));
    assertEquals(0, (int) loginData.getData().get(10));
  }

  @Test
  public void getActiveServices() {
    aggregator.run();
    Response response = cruncherResource.getRecentLoginsForUser(request, "idp2:user_1", "idp2");
    List<SpStatistic> result = (List<SpStatistic>) response.getEntity();
    assertNotNull(result);
    assertEquals(2, result.size());
    SpStatistic currentStat = result.get(0);
    if (currentStat.getSpEntityId().equals("sp2")) {
      checkStatistics(result.get(0));
    } else {
      checkStatistics(result.get(1));
    }
  }

  @Test
  public void testDifferentResultsForSameSpWhenRetrievedWithExplcitSpParameterAndNot() throws IOException {
    LocalDate start = new LocalDate(1999, 1, 10);
    LocalDate end = new LocalDate(2999, 1, 20);

    Response response = cruncherResource.getLoginsPerInterval(request, start.toDate().getTime(),
            end.toDate().getTime(), null, null);

    List<LoginData> loginData = (List<LoginData>) response.getEntity();

    Set<String> idps = uniqueIdps(loginData);
    for (String idp : idps) {
      List<LoginData> sPsPerIdp = sPsPerIdp(idp, loginData);
      for (LoginData data : sPsPerIdp) {
        assertLoginStats(data, idp, start, end);
      }
    }
  }

  private List<LoginData> sPsPerIdp(String iDP, List<LoginData> loginData) {
    List<LoginData> result = new ArrayList<LoginData>();
    for (LoginData data : loginData) {
      if (data.getIdpEntityId().equals(iDP)) {
        result.add(data);
      }
    }
    return result;
  }

  private Set<String> uniqueIdps(List<LoginData> loginData) {
    Set<String> idps = new HashSet<String>();
    for (LoginData data : loginData) {
      idps.add(data.getIdpEntityId());
    }
    return idps;
  }

  private void assertLoginStats(LoginData loginData, String idp, LocalDate start, LocalDate end) throws IOException {
    String spId = loginData.getSpEntityId();

    Response response = cruncherResource.getLoginsPerInterval(request, start.toDate().getTime(),
            end.toDate().getTime(), idp, spId);

    List<LoginData> result = (List<LoginData>) response.getEntity();
    Assert.assertEquals(1, result.size());
    LoginData oneSp = result.get(0);

    assertEquals(oneSp.getData().size(), loginData.getData().size());
    assertEquals(oneSp.getTotal(), loginData.getTotal());
  }

  private void checkStatistics(SpStatistic spStatistic) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    java.util.Date entryDate = null;
    try {
      entryDate = sdf.parse("2012-03-19 11:48:42");
    } catch (ParseException e) {
      e.printStackTrace();
    }
    assertEquals(entryDate.getTime(), spStatistic.getEntryTime());
  }


}
