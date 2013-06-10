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

import org.surfnet.cruncher.model.SpStatistic;

import java.util.Date;
import java.util.List;

/**
 * Note that some of the methods return JSON (e.g. String) as it is directly consumed by JavaScript and other are
 * consumed from within Java (e.g. Controllers) and return Cruncher domain objects.
 */
public interface Cruncher {

  String getLogins(Date startDate, Date endDate);

  String getLoginsByIdpAndSp(Date startDate, Date endDate, String idpEntityId,  String spEntityId);

  String getLoginsByIdp(Date startDate, Date endDate, String idpEntityId);

  String getLoginsBySp(Date startDate, Date endDate, String spEntityId);

  List<SpStatistic> getRecentLoginsForUser(String userId, String idpEntityId);

}
