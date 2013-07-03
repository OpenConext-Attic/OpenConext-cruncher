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
package org.surfnet.cruncher.model;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class LoginDataTest {

  private ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testJsonConversion() throws IOException {
    LoginData loginData = mapper.readValue(new ClassPathResource("stats.json").getInputStream(), LoginData.class);
    List<Integer> data = loginData.getData();
    int totalFromData = 0;
    for (Integer entryCount : data) {
      totalFromData += entryCount;
    }
    assertEquals(loginData.getTotal(), totalFromData);
  }

}
