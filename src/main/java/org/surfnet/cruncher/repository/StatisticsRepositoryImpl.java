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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.LoginEntry;

@Named
@Transactional
public class StatisticsRepositoryImpl implements StatisticsRepository {

  private static final Logger LOG = LoggerFactory.getLogger(StatisticsRepositoryImpl.class);

  @Inject
  private JdbcTemplate jdbcTemplate;

  @Override
  public void aggregateLogin(List<LoginEntry> loginEntries) {
    if (loginEntries == null) {
      throw new IllegalArgumentException("List of loginEntries cannot be null.");
    }
    for (LoginEntry le : loginEntries) {
      addToAggregated(le);
    }
  }

  private void addToAggregated(LoginEntry le) {
    if (entryForDayExists(le.getLoginDate(), le.getSpEntityId(), le.getIdpEntityId())) {
      LOG.debug("Updating existing aggregated record for date {}, record: {}", le.getLoginDate(), le);
      updateDailyWith(le);
    } else {
      LOG.debug("Inserting new aggregated record for date {}, record: {}", le.getLoginDate(), le);
      insertDailyWith(le);
    }
  }

  /**
   * Create a record for a day.
   * @param le the LoginEntry
   */
  private void insertDailyWith(LoginEntry le) {
    jdbcTemplate.update("insert into aggregated_log_logins (entryday,spentityid,idpentityid,spentityname,idpentityname, entrycount) values " +
      "(:entryDate, :spEntityId, :idpEntityId, :spEntityName, :idpEntityName, 1)",
      le.getLoginDate(), le.getSpEntityId(), le.getIdpEntityId(), le.getSpEntityName(), le.getIdpEntityName());
  }

  private void updateDailyWith(LoginEntry le) {
    jdbcTemplate.update("update aggregated_log_logins set entrycount = entrycount + 1 where " +
      "entryday = :thedate and " +
      "spentityid = :spentityid and " +
      "idpentityid = :idpentityid", le.getLoginDate(), le.getSpEntityId() ,le.getIdpEntityId());
  }

  /**
   * Whether an aggregated record exists for the given date
   * @param loginDate
   */
  private boolean entryForDayExists(Date loginDate, String spEntityId, String idpEntityId) {
    return jdbcTemplate.queryForInt("select count(*) from aggregated_log_logins where " +
      "entryday = :thedate and " +
      "spentityid = :spentityid and " +
      "idpentityid = :idpentityid", loginDate, spEntityId, idpEntityId) == 1;
  }

  @Override
  public List<LoginData> getLoginsPerSpPerDay(String idpEntityId) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public List<LoginData> getLoginsPerSpPerDay() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public List<LoginData> getUniqueLogins(final Timestamp start, final Timestamp end, final String spEntityId, final String idpEntityId) {
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    
    String query = "select * from aggregated_log_logins " +
    		"where " +
    		"entryday >= :startDate AND " +
    		"entryday <= :endDate AND " +
    		"(:spEntityId IS NULL OR spentityid = :spEntityId) AND " +
        "(:idpEntityId IS NULL OR idpentityid = :idpEntityId) ";
    		//"group by idpentityid, spentityid";
    
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("startDate", start);
    parameterMap.put("endDate", end);
    parameterMap.put("spEntityId", spEntityId);
    parameterMap.put("idpEntityId", idpEntityId);
    
    return namedJdbcTemplate.query(query, parameterMap , new RowMapper<LoginData>(){

      @Override
      public LoginData mapRow(ResultSet rs, int row) throws SQLException {
        LoginData result = new LoginData();
        result.setIdpEntityId(rs.getString("idpentityid"));
        result.setIdpname(rs.getString("idpentityname"));
        result.setSpEntityId(rs.getString("spentityid"));
        result.setSpName(rs.getString("spentityname"));
        result.setTotal(rs.getInt("entrycount"));
        
        return result;
      }
      
    });
  }
}
