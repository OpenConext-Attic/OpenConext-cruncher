/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.surfnet.cruncher.resource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.surfnet.coin.janus.Janus;
import nl.surfnet.coin.janus.domain.EntityMetadata;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.SpStatistic;
import org.surfnet.cruncher.model.VersStatistic;
import org.surfnet.cruncher.repository.StatisticsRepository;

@Named
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
public class CruncherResource {

  private static final Logger LOG = LoggerFactory.getLogger(CruncherResource.class);

  @Inject
  private StatisticsRepository statisticsRepository;
  
  @Inject
  private Janus janusRestClient;

  @GET
  @Path("/lastlogin")
  public Response getRecentLoginsForUser(@QueryParam("userId") String userId,
      @QueryParam("idpEntityId") String idpEntityId) {
    invariant(userId, idpEntityId);
    
    final List<SpStatistic> recentLogins = statisticsRepository.getActiveServices(userId, idpEntityId);
    LOG.info("returning recent logins for " + userId + " on " + idpEntityId);
    return Response.ok(recentLogins).build();
  }

  @GET
  @Path("/logins")
  public Response getLoginsPerInterval(@QueryParam("startDate") Long startDate,
      @QueryParam("endDate") Long endDate, @QueryParam("idpEntityId") String idpEntityId,
      @QueryParam("spEntityId") String spEntityId) {
    invariant(startDate, endDate);

    List<LoginData> result = statisticsRepository.getLogins(new LocalDate(startDate), new LocalDate(endDate),
        idpEntityId, spEntityId);
    LOG.info("returning logins for sp " + spEntityId + " and idp " + idpEntityId);
    return Response.ok(result).build();
  }
  
  @GET
  @Path("/versstats")
  public Response getVersStatistics(@QueryParam("month") Integer month,
      @QueryParam("year") Integer year, @QueryParam("spEntityId") String spEntityId) {
    invariant(month, year, spEntityId);
    VersStatistic result = new VersStatistic();
    
    LocalDate startDate = new LocalDate(year, month, 1);
    LocalDate endDate = startDate.plusMonths(1);
    VersStatistic queryResult = statisticsRepository.getVersStats(startDate, endDate, spEntityId);
    Map<String, String> idpInstitutions = getInstitutionIdsFromJanus();
    Iterator<String> keyIterator = queryResult.getInstitutionLogins().keySet().iterator();
    while (keyIterator.hasNext()) {
      String key = keyIterator.next();
      Long institutionCount = queryResult.getInstitutionLogins().get(key);
      String institutionId = idpInstitutions.get(key);
      result.addInstitutionLoginCount(institutionId, institutionCount);
      result.setTotalLogins(result.getTotalLogins() + institutionCount);
    }
    return Response.ok(result).build();
  }
  
  private Map<String, String> getInstitutionIdsFromJanus() {
    Map<String, String> result = new HashMap<String, String>();
    List<EntityMetadata> idpList = janusRestClient.getIdpList();
    for (EntityMetadata current : idpList) {
      result.put(current.getAppEntityId(), current.getInstutionId());
    }
    return result;
  }

  private void invariant(Long startDate, Long endDate) {
    Assert.notNull(startDate, "startDate is a required query parameter");
    Assert.notNull(endDate, "endDate is a required query parameter");
  }
  
  private void invariant(String userId, String idpEntityId) {
    Assert.notNull(userId, "userId is a required query parameter");
    Assert.notNull(idpEntityId, "idpEntityId is a required query parameter");
  }

  private void invariant(Integer month, Integer year, String spEntityId) {
    Assert.notNull(month, "month is a required query parameter");
    Assert.notNull(year, "year is a required query parameter");
    Assert.notNull(spEntityId, "spEntityID is a required query paramter");
  }
}
