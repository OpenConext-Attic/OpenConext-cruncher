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
package org.surfnet.cruncher;

import nl.surfnet.coin.oauth.OauthClient;
import org.springframework.util.StringUtils;
import org.surfnet.cruncher.model.SpStatistic;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CruncherClient implements Cruncher {

  private OauthClient oauthClient;

  /**
   * Location of the cruncher Resource Server
   */
  private String cruncherBaseLocation;

  public CruncherClient(String cruncherBaseLocation) {
    this.cruncherBaseLocation = cruncherBaseLocation;
  }

  @Override
  public String getLogins(final Date startDate, final Date endDate) {
    return doJsonGetFromCruncher("/logins?startDate={startDate}&endDate={endDate}", getLoginsVariables(startDate, endDate, null, null));
  }

  @Override
  public String getLoginsByIdpAndSp(final Date startDate, final Date endDate, String idpEntityId, String spEntityId) {
    return doJsonGetFromCruncher("/logins?startDate={startDate}&endDate={endDate}&idpEntityId={idpEntityId}&spEntityId={spEntityId}", getLoginsVariables(startDate, endDate, spEntityId, idpEntityId));
  }

  @Override
  public String getLoginsByIdp(final Date startDate, final Date endDate, String idpEntityId) {
    return doJsonGetFromCruncher("/logins?startDate={startDate}&endDate={endDate}&idpEntityId={idpEntityId}", getLoginsVariables(startDate, endDate, null, idpEntityId));
  }

  @Override
  public String getLoginsBySp(final Date startDate, final Date endDate, String spEntityId) {
    return doJsonGetFromCruncher("/logins?startDate={startDate}&endDate={endDate}&spEntityId={spEntityId}", getLoginsVariables(startDate, endDate, spEntityId, null));
  }

  @Override
  public List<SpStatistic> getRecentLoginsForUser(String userId, String idpEntityId) {
    Map<String, String> variables = new HashMap<String, String>();
    variables.put("idpEntityId", idpEntityId);
    variables.put("userId", userId);
    return (List<SpStatistic>) oauthClient.exchange(cruncherBaseLocation + "/lastlogin?idpEntityId={idpEntityId}&userId={userId}", variables, SpStatistic[].class);
  }

  @Override
  public void setOauthClient(OauthClient oc) {
    this.oauthClient = oc;
  }

  private String doJsonGetFromCruncher(String subPath, Map<String, ?> variables) {
    return oauthClient.exchange(cruncherBaseLocation + subPath, variables, String.class);
  }


  private Map<String, Object> getLoginsVariables(final Date startDate, final Date endDate, String spEntityId, String idpEntityId) {
    Map variables = new HashMap<String, Object>();
    if (StringUtils.hasText(idpEntityId)) {
      variables.put("idpEntityId", idpEntityId);
    }
    if (StringUtils.hasText(spEntityId)) {
      variables.put("spEntityId", spEntityId);
    }
    variables.put("startDate", startDate.getTime());
    variables.put("endDate", endDate.getTime());
    return variables;
  }

  public void setCruncherBaseLocation(String cruncherBaseLocation) {
    this.cruncherBaseLocation = cruncherBaseLocation;
  }

}
