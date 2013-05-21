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

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.LoginEntry;

public interface StatisticsRepository {

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
   * Makes a list of unique logins per SP and/or Idp. The login should fall
   * between start en end time stamp. Start en end timestamps are required
   * and one of idp and sp is required (or both).
   * 
   * @param start start time
   * @param end end time
   * @param spEntityId sp entity id
   * @param idpEntityId idp entity id
   * @return List of {@link LoginData}
   */
  List<LoginData> getUniqueLogins(final Timestamp start, final Timestamp end, final String spEntityId, final String idpEntityId);
  
  List<LoginData> getLogins(final Timestamp start, final Timestamp end, final String spEntityId, final String idpEntityId, final long interval);

  /**
   * Get a list of records that have to be aggregated yet.
   * @param nrOfRecords the number to get
   */
  List<LoginEntry> getUnprocessedLoginEntries(int nrOfRecords);

  /**
   * Mark login-log entries as processed by aggregation
   * @param entries the entries to mark
   */
  void setLoginEntriesProcessed(List<LoginEntry> entries);

  void updateAggregated(String idpEntityId, String spEntityId, Date loginDate);

  void insertAggregated(LoginEntry le);

  boolean aggregatedExists(String idpEntityId, String spEntityId, Date loginDate);
}
