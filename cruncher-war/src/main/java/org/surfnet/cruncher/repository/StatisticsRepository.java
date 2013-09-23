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

import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.LoginEntry;
import org.surfnet.cruncher.model.SpStatistic;
import org.surfnet.cruncher.model.VersStatistic;

public interface StatisticsRepository {
  
  /**
   * Return aggregated logins based on IDP or SP per day.
   * <p>
   * <strong>NOTE</strong> Either sp entity ID or idp entity ID is required!
   * </p>
   * @param start start date
   * @param end end date
   * @param spEntityId (optional) sp entity ID
   * @param idpEntityId (optional) idpEntity ID
   * @return a list of LoginData
   */
  List<LoginData> getLogins(final LocalDate start, final LocalDate end, final String idpEntityId, final String spEntityId);

  /**
   * retrieve a list of service for which the user has been active (a login
   * record must exist for this user)
   * 
   * @param userid the user for the user
   * @param idpEntityId the idp for which the user is currently logged in
   * @return a list of SPs for which the user has been logged in
   */
  List<SpStatistic> getActiveServices(String userid, String idpEntityId);
  
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

  boolean lockForCrunching();

  void unlockForCrunching();

  boolean lastLogonExists(String userId, String idpEntityId);

  void insertLastLogin(LoginEntry le);

  void updateLastLogin(String userId, String spEntityId, Date loginDate);

  void cleanTables(int retention);

  VersStatistic getVersStats(LocalDate startDate, LocalDate endDate, String spEntityId);

  void insertUniqueLoginInCache(LoginEntry le);

  boolean uniqueUserLogonExists(String userId, Date loginDate, String spEntityId, String idpEntityId);

  void aggregateUniqueLoginsIfNeeded();
}
