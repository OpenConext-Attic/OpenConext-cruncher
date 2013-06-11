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
package org.surfnet.cruncher.integration;

import nl.surfnet.coin.oauth.ClientCredentialsClient;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalTestServer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.junit.BeforeClass;
import org.junit.Test;
import org.surfnet.cruncher.CruncherClient;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.SpStatistic;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class  CruncherClientTestIntegration {

  private static String cruncherBaseLocation = "http://localhost:8080/cruncher/stats/v1";

  private static String answer = "{\"scope\":\"something\",\"access_token\":\"3fc6a956-a414-4f4b-a280-65cfbeb9ba2a\",\"token_type\":\"bearer\",\"expires_in\":0}";

  private static ObjectMapper mapper = new ObjectMapper().enable(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
          .setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL).setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);

  /*
   * We need to mock the authorization server response for an client credentials access token
   */
  private static LocalTestServer oauth2AuthServer;

  private static CruncherClient cruncherClient;

  @BeforeClass
  public static void beforeClass() throws Exception {
    oauth2AuthServer = new LocalTestServer(null, null);
    oauth2AuthServer.start();
    oauth2AuthServer.register("/oauth2/token", new HttpRequestHandler() {
      @Override
      public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        response.setEntity(new StringEntity(answer, ContentType.APPLICATION_JSON));
        response.setStatusCode(200);
      }

    });
    String apisOAuth2AuthorizationUrl = String.format("http://%s:%d/oauth2/token", oauth2AuthServer.getServiceAddress().getHostName(),
            oauth2AuthServer.getServiceAddress().getPort());
    cruncherClient = new CruncherClient(cruncherBaseLocation);
    ClientCredentialsClient oauthClient = new ClientCredentialsClient();
    oauthClient.setClientKey("key");
    oauthClient.setClientSecret("secret");
    oauthClient.setOauthAuthorizationUrl(apisOAuth2AuthorizationUrl);
    cruncherClient.setOauthClient(oauthClient);
  }

  @Test
  public void recentLoginsForUser() throws IOException {
    List<SpStatistic> logins = cruncherClient.getRecentLoginsForUser("user_4", "idp3");
    assertEquals(2, logins.size());
    for (SpStatistic login : logins) {
      assertNotNull(login.getSpEntityId());
    }
  }

  @Test
  public void getLogins() {
    String json = cruncherClient.getLogins(new Date(0L), new Date());
    List<LoginData> logins = getLoginDatas(json);
    assertEquals(7, logins.size());
  }

  @Test
  public void getLoginsByIdpAndSp() {
    String json = cruncherClient.getLoginsByIdpAndSp(new Date(0L), new Date(), "idp1","sp1");
    List<LoginData> logins = getLoginDatas(json);
    assertEquals(1, logins.size());
  }
  @Test
  public void getLoginsByIdp() {
    String json = cruncherClient.getLoginsByIdp(new Date(0L), new Date(), "idp1");
    List<LoginData> logins = getLoginDatas(json);
    assertEquals(2, logins.size());
  }
  @Test
  public void getLoginsBySp() {
    String json = cruncherClient.getLoginsBySp(new Date(0L), new Date(), "sp1");
    List<LoginData> logins = getLoginDatas(json);
    assertEquals(2, logins.size());
  }

  private List<LoginData> getLoginDatas(String json) {
    try {
      return mapper.readValue(json, new TypeReference<List<LoginData>>() {
      });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}