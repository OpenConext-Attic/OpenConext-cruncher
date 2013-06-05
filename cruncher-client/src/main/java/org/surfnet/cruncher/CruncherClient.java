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

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.surfnet.cruncher.model.SpStatistic;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CruncherClient implements Cruncher {

  private static final Logger LOG = LoggerFactory.getLogger(CruncherClient.class);

  /**
   * OAuth2 Client Key (from the JS oauth2 client when this client was registered
   */
  @Value("${cruncher.client.key}")
  private String cruncherClientKey;

  /**
   * OAuth2 Client Secret (from the JS oauth2 client when this client was registered
   */
  @Value("${cruncher.client.secret}")
  private String cruncherClientSecret;

  /**
   * Location of the Authorization Server for getting a client credential
   */
  @Value("${apis.oauth2.authorization.url}")
  private String apisOAuth2AuthorizationUrl;

  /**
   * Location of the cruncher Resource Server
   */
  @Value("${cruncher.base.url}")
  private String cruncherBaseLocation;

  private String accessToken;

  private RestTemplate restTemplate = new RestTemplate();

  public CruncherClient() {
  }

  public CruncherClient(String cruncherClientKey, String cruncherClientSecret, String cruncherBaseLocation, String apisOAuth2AuthorizationUrl) {
    this.cruncherClientKey = cruncherClientKey;
    this.cruncherClientSecret = cruncherClientSecret;
    this.cruncherBaseLocation = cruncherBaseLocation;
    this.apisOAuth2AuthorizationUrl = apisOAuth2AuthorizationUrl;
    this.accessToken = getAccessToken();
    // we handle invalid access_token ourselves
    restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
      protected boolean hasError(HttpStatus statusCode) {
        return super.hasError(statusCode) && statusCode != HttpStatus.FORBIDDEN;
      }
    });
  }

  @Override
  public String getLogins() {
    return doJsonGetFromCruncher("/logins?startDate={startDate}&endDate={endDate}", getLoginsVariables(null, null));
  }

  @Override
  public String getLoginsByIdpAndSp(String idpEntityId, String spEntityId) {
    return doJsonGetFromCruncher("/logins?startDate={startDate}&endDate={endDate}&idpEntityId={idpEntityId}&spEntityId={spEntityId}", getLoginsVariables(spEntityId, idpEntityId));
  }

  @Override
  public String getLoginsByIdp(String idpEntityId) {
    return doJsonGetFromCruncher("/logins?startDate={startDate}&endDate={endDate}&idpEntityId={idpEntityId}", getLoginsVariables(null, idpEntityId));
  }

  @Override
  public String getLoginsBySp(String spEntityId) {
    return doJsonGetFromCruncher("/logins?startDate={startDate}&endDate={endDate}&spEntityId={spEntityId}", getLoginsVariables(spEntityId, null));
  }

  @Override
  public List<SpStatistic> getRecentLoginsForUser(String userId, String idpEntityId) {
    Map variables = new HashMap<String, Object>();
    variables.put("idpEntityId", idpEntityId);
    variables.put("userId", userId);
    return (List<SpStatistic>) doGetFromCruncher("/lastlogin?idpEntityId={idpEntityId}&userId={userId}", variables, SpStatistic[].class, true);
  }

  private String doJsonGetFromCruncher(String url, Map<String, ?> variables) {
    return doGetFromCruncher(url, variables, String.class, true);
  }

  private <T> T doGetFromCruncher(String url, Map<String, ?> variables, Class clazz, boolean retry) {
    HttpHeaders headers = new HttpHeaders();
    if (accessToken == null) {
      accessToken = getAccessToken();
    }
    headers.add("Authorization", "bearer " + accessToken);

    HttpEntity requestEntity = new HttpEntity(headers);
    HttpMethod method = HttpMethod.GET;
    ResponseEntity<T> response;

    if (CollectionUtils.isEmpty(variables)) {
      response = restTemplate.exchange(URI.create(cruncherBaseLocation + url), method, requestEntity, clazz);
    } else {
      response = restTemplate.exchange(cruncherBaseLocation + url, method, requestEntity, clazz, variables);
    }
    if (retry && response.getStatusCode() != HttpStatus.OK) {
      //let's try again with a new AccessToken
      accessToken = null;
      doGetFromCruncher(url, variables, clazz, false);
    }
    T body = response.getBody();
    if (clazz.isArray()) {
      return getListResult((T[]) body);
    }
    return body;
  }

  /*
   * This could be achieved using the methods we use for Csa REST calls, but it would make that implementation needless generic (e.g. complex)
   */
  private String getAccessToken() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + new String(Base64.encodeBase64((cruncherClientKey + ":" + cruncherClientSecret).getBytes())));
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<String> requestEntity = new HttpEntity<String>("grant_type=client_credentials", headers);
    try {
      ResponseEntity<Map> response = restTemplate.exchange(URI.create(apisOAuth2AuthorizationUrl),
              HttpMethod.POST,
              requestEntity,
              Map.class);
      if (response.getStatusCode() != HttpStatus.OK) {
        LOG.error("Received HttpStatus {} when trying to obtain AccessToken", response.getStatusCode());
        return null;
      } else {
        Map map = response.getBody();
        return (String) map.get("access_token");
      }
    } catch (RestClientException e) {
      LOG.error("Error trying to obtain AccessToken", e);
      //this will ensure we will try again for the next call. no sensible action can be undertaken now
      return null;
    }
  }

  private Map<String, Object> getLoginsVariables(String spEntityId, String idpEntityId) {
    Map variables = new HashMap<String, Object>();
    if (StringUtils.hasText(idpEntityId)) {
      variables.put("idpEntityId", idpEntityId);
    }
    if (StringUtils.hasText(spEntityId)) {
      variables.put("spEntityId", spEntityId);
    }
    variables.put("startDate", 0L);
    variables.put("endDate", System.currentTimeMillis());
    return variables;
  }

  /*
   *  (T) Arrays.<T>asList(body) won't work as the type is not inferred and we end up with a list containing one entry: the array
   */
  private <T> T getListResult(T[] body) {
    List<T> result = new ArrayList<T>();
    T[] arr = body;
    for (T t : arr) {
      result.add(t);
    }
    return (T) result;
  }
}
