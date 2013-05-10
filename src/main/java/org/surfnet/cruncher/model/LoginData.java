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

import java.util.ArrayList;
import java.util.List;

public class LoginData {

  private String spName;
  private String idpname;
  private String spEntityId;
  private String idpEntityId;
  private long loginTime;
  private List<Integer> data;
  
  public String getSpName() {
    return spName;
  }
  
  public void setSpName(String spName) {
    this.spName = spName;
  }
  
  public String getIdpname() {
    return idpname;
  }
  
  public void setIdpname(String idpname) {
    this.idpname = idpname;
  }
  
  public String getSpEntityId() {
    return spEntityId;
  }
  
  public void setSpEntityId(String spEntityId) {
    this.spEntityId = spEntityId;
  }
  
  public String getIdpEntityId() {
    return idpEntityId;
  }
  
  public void setIdpEntityId(String idpEntityId) {
    this.idpEntityId = idpEntityId;
  }
  
  public long getLoginTime() {
    return loginTime;
  }
  
  public void setLoginTime(long loginTime) {
    this.loginTime = loginTime;
  }
  
  public List<Integer> getData() {
    if (null == data) {
      data = new ArrayList<Integer>();
    }
    return data;
  }
}
