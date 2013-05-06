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

  public String spName;
  public String idpname;
  public String spEntityId;
  public String idpEntityId;
  public long pointStart;
  public long pointInterval = 24L * 3600L * 1000L; // one day
  public int total;
  public List<Integer> data = new ArrayList<Integer>();

  public LoginData(String spName, String idpname, String spEntityId, String idpEntityId, long pointStart, long pointInterval) {
    this.spName = spName;
    this.idpname = idpname;
    this.spEntityId = spEntityId;
    this.idpEntityId = idpEntityId;
    this.pointStart = pointStart;
    this.pointInterval = pointInterval == 0 ? this.pointInterval : pointInterval;
  }
}
