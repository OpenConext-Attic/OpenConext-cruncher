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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.SpStatistic;
import org.surfnet.cruncher.repository.StatisticsRepository;

@Named
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
public class CruncherResource {

  private static final Logger LOG = LoggerFactory.getLogger(CruncherResource.class);

  @Inject
  private StatisticsRepository statisticsRepository;

  @GET
  @Path("/lastlogin/{idpEntityId}/{userId}")
  public Response getRecentLoginsForUser(@Context HttpServletRequest request, @PathParam("userId") String userId,
      @PathParam("idpEntityId") String idpEntityId) {

    final List<SpStatistic> recentLogins = statisticsRepository.getActiveServices(userId, idpEntityId);
    LOG.info("returning recent logins for " + userId + " on " + idpEntityId);
    return Response.ok(recentLogins).build();
  }

  @GET
  @Path("/uniqueLogins/{startDate}/{endDate}")
  public Response getUniqueLogins(@Context HttpServletRequest request, @PathParam("startDate") long startDate,
      @PathParam("endDate") long endDate, @QueryParam("idpEntityId") String idpEntityId,
      @QueryParam("spEntityId") String spEntityId) {
    // start and end date are required
    // idp en sp entity id are optional, if neither is given -> error
    if (StringUtils.isBlank(idpEntityId) && StringUtils.isBlank(spEntityId)) {
      throw new IllegalArgumentException("Either idp or sp entity ID is required for this call");
    }
    
    LOG.debug("returning mocked response for unique logins. startDate " + startDate + " endData " + endDate
        + " idpEntityId " + idpEntityId + " spEntityId " + spEntityId);
    // TODO determine what a unique login is and return it
    List<LoginData> result = statisticsRepository.getUniqueLogins(new LocalDate(startDate), new LocalDate(endDate),
        idpEntityId, spEntityId);
    return Response.ok(result).build();
  }

  @GET
  @Path("logins/{startDate}/{endDate}")
  public Response getLoginsPerInterval(@Context HttpServletRequest request, @PathParam("startDate") long startDate,
      @PathParam("endDate") long endDate, @QueryParam("idpEntityId") String idpEntityId,
      @QueryParam("spEntityId") String spEntityId) {

    // start and end date are required
    // idp en sp entity id are optional, if neither is given -> error
    if (StringUtils.isBlank(idpEntityId) && StringUtils.isBlank(spEntityId)) {
      throw new IllegalArgumentException("Either idp or sp entity ID is required for this call");
    }

    List<LoginData> result = statisticsRepository.getLogins(new LocalDate(startDate), new LocalDate(endDate),
        idpEntityId, spEntityId);
    LOG.info("returning logins for sp " + spEntityId + " and idp " + idpEntityId);
    return Response.ok(result).build();
  }
}