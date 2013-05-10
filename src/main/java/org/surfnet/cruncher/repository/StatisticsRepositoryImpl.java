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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.surfnet.cruncher.model.LoginData;

@Named
public class StatisticsRepositoryImpl implements StatisticsRepository {

  @Inject
  private JdbcTemplate jdbcTemplate;

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
    
    String query = "select * from log_logins " +
    		"where " +
    		"loginstamp >= :startDate AND " +
    		"loginstamp <= :endDate AND " +
    		"(:spEntityId IS NULL OR spentityid = :spEntityId) AND " +
        "(:idpEntityId IS NULL OR idpentityid = :idpEntityId)";
    
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
        result.setLoginTime(rs.getTimestamp("loginstamp").getTime());
        
        return result;
      }
      
    });
  }
}
