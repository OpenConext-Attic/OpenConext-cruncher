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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.LoginEntry;

@Named
public class StatisticsRepositoryImpl implements StatisticsRepository {

  private static final Logger LOG = LoggerFactory.getLogger(StatisticsRepositoryImpl.class);

  @Inject
  private JdbcTemplate jdbcTemplate;


  private static final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");

  private String aggregationRecordHash(LoginEntry le) {
    return aggregationRecordHash(le.getIdpEntityId(), le.getSpEntityId(), le.getLoginDate());
  }

  private String aggregationRecordHash(String idpEntityId, String spEntityId, Date loginDate) {
    String input = dateformat.format(loginDate) + "!" + idpEntityId + "!" + spEntityId;
    return DigestUtils.sha1Hex(input);
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
  
  @Override
  public List<LoginData> getLogins(final Timestamp start, final Timestamp end, final String spEntityId, final String idpEntityId, final long interval) {
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    
    String query = "select * from aggregated_log_logins " +
        "where " +
        "entryday >= :startDate AND " +
        "entryday <= :endDate AND " +
        "(:spEntityId IS NULL OR spentityid = :spEntityId) AND " +
        "(:idpEntityId IS NULL OR idpentityid = :idpEntityId) " +
        "group by idpentityid, spentityid " +
        "order by idpentityid, spentityid";
    
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("startDate", start);
    parameterMap.put("endDate", end);
    parameterMap.put("spEntityId", spEntityId);
    parameterMap.put("idpEntityId", idpEntityId);
    
    //result maps
    final Map<String, Map<String, List<LoginData>>> resultMap = new HashMap<String, Map<String, List<LoginData>>>();
    
    namedJdbcTemplate.query(query, parameterMap , new RowMapper<LoginData>(){

      @Override
      public LoginData mapRow(ResultSet rs, int row) throws SQLException {
        LoginData result = new LoginData();
        result.setIdpEntityId(rs.getString("idpentityid"));
        result.setIdpname(rs.getString("idpentityname"));
        result.setSpEntityId(rs.getString("spentityid"));
        result.setSpName(rs.getString("spentityname"));
        result.setTotal(rs.getInt("entrycount"));
        Date entryDate = rs.getDate("entryday");
        //TODO maybe update the start en end to start en end of day
        result.setPointStart(entryDate.getTime());
        result.setPointEnd(entryDate.getTime());
        result.setPointInterval(interval);
        
        //insert into resultMap
        if (null == resultMap.get(result.getIdpEntityId())) {
          resultMap.put(result.getIdpEntityId(), new HashMap<String, List<LoginData>>());
        }
        if (null == resultMap.get(result.getIdpEntityId()).get(result.getSpEntityId())) {
          resultMap.get(result.getIdpEntityId()).put(result.getSpEntityId(), new ArrayList<LoginData>());
        }
        resultMap.get(result.getIdpEntityId()).get(result.getSpEntityId()).add(result);
        
        //this is kinda weird
        return null;
      }
    });
    
    // normalize the resultMap in a list of loginData
    for (String idp : resultMap.keySet()) {
      for (String sp: resultMap.get(idp).keySet()) {
        List<LoginData> current = resultMap.get(idp).get(sp);
        System.out.println("entries found: " + current.size());
      }
    }
    return null;
  }

  @Override
  public List<LoginEntry> getUnprocessedLoginEntries(int nrOfRecords) {
    throw new NotImplementedException("");
  }

  @Override
  public void setLoginEntriesProcessed(List<LoginEntry> entries) {
    throw new NotImplementedException("");
  }

  @Override
  public void updateAggregated(String idpEntityId, String spEntityId, Date loginDate) {
    jdbcTemplate.update("update aggregated_log_logins set entrycount = entrycount + 1 where datespidphash = ?", aggregationRecordHash(idpEntityId, spEntityId, loginDate));
  }

  @Override
  public void insertAggregated(LoginEntry le) {
    LOG.debug("Inserting new aggregated record for date {}, record: {}, hash: {}", new Object[] {le.getLoginDate(), le, aggregationRecordHash(le)});
    jdbcTemplate.update("insert into aggregated_log_logins (entryday,spentityid,idpentityid,spentityname,idpentityname, datespidphash, entrycount)" +
      " values (?, ?, ?, ?, ?, ?, 1)",
      le.getLoginDate(), le.getSpEntityId(), le.getIdpEntityId(), le.getSpEntityName(), le.getIdpEntityName(), aggregationRecordHash(le));
  }

  @Override
  public boolean aggregatedExists(String idpEntityId, String spEntityId, Date loginDate) {
    return jdbcTemplate.queryForInt("select count(*) from aggregated_log_logins where datespidphash = ?", aggregationRecordHash(idpEntityId, spEntityId, loginDate)) == 1;
  }
}
