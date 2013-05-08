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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.surfnet.cruncher.repository.StatisticsRepository;
import org.surfnet.oaaas.auth.AuthorizationServerFilter;
import org.surfnet.oaaas.model.VerifyTokenResponse;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Named
@Path("/v1")
@Produces(MediaType.APPLICATION_JSON)
public class CruncherResource {

  private static final Logger LOG = LoggerFactory.getLogger(CruncherResource.class);

  @Inject
  private StatisticsRepository statisticsRepository;

  @GET
  @Path("/statistics/{idpEntityId}")
  public Response getAll(@Context
                         HttpServletRequest request, @PathParam("idpEntityId")
                         String idpEntityId) {

//    String owner = getClientId(request);
    final List<Object> stats = new ArrayList<Object>();

    LOG.debug("About to return all stats for client {}"); //, owner);

    return Response.ok(stats).build();
  }
  
  @GET
  @Path("statistics/{idpEntityId}/{userId}")
  public Response getActiveSpsForUser(@Context HttpServletRequest request, 
      @PathParam("idpEntityId") String idpEntityId,
      @PathParam("userId") String userId) {
    return null;
  }

  protected String getClientId(HttpServletRequest request) {
    VerifyTokenResponse verifyTokenResponse = (VerifyTokenResponse) request.getAttribute(AuthorizationServerFilter.VERIFY_TOKEN_RESPONSE);
    return verifyTokenResponse.getPrincipal().getName();
  }


}
