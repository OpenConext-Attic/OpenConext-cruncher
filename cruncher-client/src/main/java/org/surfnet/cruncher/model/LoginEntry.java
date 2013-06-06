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

package org.surfnet.cruncher.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Class that represents a user login on OpenConext.
 */
public class LoginEntry implements Serializable {
  private static final long serialVersionUID = -3406146073511813593L;

  public LoginEntry(String idpEntityId, String idpEntityName, Date loginDate, String spEntityId, String spEntityName, String userId) {
    this.idpEntityId = idpEntityId;
    this.idpEntityName = idpEntityName;
    this.loginDate = loginDate;
    this.spEntityId = spEntityId;
    this.spEntityName = spEntityName;
    this.userId = userId;
  }

  private Date loginDate;
  private String userId;
  private String spEntityId;
  private String idpEntityId;
  private String spEntityName;
  private String idpEntityName;

  public Date getLoginDate() {
    return loginDate;
  }

  public String getSpEntityId() {
    return spEntityId;
  }

  public String getIdpEntityId() {
    return idpEntityId;
  }

  public String getSpEntityName() {
    return spEntityName;
  }

  public String getIdpEntityName() {
    return idpEntityName;
  }

  public String getUserId() {
    return userId;
  }

  @Override
  public String toString() {
    return "Login at "+loginDate+" of "+userId+" on "+ idpEntityId +" sp " + spEntityId;
  }
  
  
}
