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
package org.surfnet.cruncher.repository;

import java.util.List;

import org.joda.time.LocalDate;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.LoginEntry;

public interface StatisticsRepository {

  /**
   * Use the given LoginEntries to aggregate data in the database.
   * This will
   * @param loginEntries
   */
  void aggregateLogin(List<LoginEntry> loginEntries);

  /**
   * Makes a List of login data per Service Provider got the Identity Provider
   *
   * @param idpEntityId unique identifier of the Identity provider
   * @return List of {@link LoginData}
   */
  List<LoginData> getLoginsPerSpPerDay(String idpEntityId);

  /**
   * Makes a List of login data per Service Provider for all IdP's
   *
   * @return List of {@link LoginData}
   */
  List<LoginData> getLoginsPerSpPerDay();
  
  /**
   * return unique logins
   * @param start
   * @param end
   * @param spEntityId
   * @param idpEntityId
   * @return
   */
  List<LoginData> getUniqueLogins(final LocalDate start, final LocalDate end, final String spEntityId, final String idpEntityId);
  
  /**
   * Return aggregated logins based on IDP or SP.
   * <p>
   * <strong>NOTE</strong> Either sp entity ID or idp entity ID is required!
   * </p>
   * @param start start date
   * @param end end date
   * @param spEntityId (optional) sp entity ID
   * @param idpEntityId (optional) idpEntity ID
   * @param interval currently always null and defaults to a day
   * @return a list of LoginData
   */
  List<LoginData> getLogins(final LocalDate start, final LocalDate end, final String spEntityId, final String idpEntityId, final Long interval);
}
