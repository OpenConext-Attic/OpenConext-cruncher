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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.LoginEntry;
import org.surfnet.cruncher.model.SpStatistic;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Named
public class StatisticsRepositoryImpl implements StatisticsRepository {

  private static final Logger LOG = LoggerFactory.getLogger(StatisticsRepositoryImpl.class);

  @Inject
  private JdbcTemplate jdbcTemplate;




  private String aggregationRecordHash(LoginEntry le) {
    return aggregationRecordHash(le.getIdpEntityId(), le.getSpEntityId(), le.getLoginDate());
  }

  private String aggregationRecordHash(String idpEntityId, String spEntityId, Date loginDate) {
    final DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
    String input = dateformat.format(loginDate) + "!" + idpEntityId + "!" + spEntityId;
    return DigestUtils.sha1Hex(input);
  }



  @Override
  public List<LoginData> getUniqueLogins(final LocalDate start, final LocalDate end, final String idpEntityId, final String spEntityId) {
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    
    String query = "select * from aggregated_log_logins " +
    		"where " +
    		"entryday >= :startDate AND " +
    		"entryday <= :endDate AND " +
    		"(:spEntityId IS NULL OR spentityid = :spEntityId) AND " +
        "(:idpEntityId IS NULL OR idpentityid = :idpEntityId) ";
    		//"group by idpentityid, spentityid";
    
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("startDate", new Date(start.toDateMidnight().getMillis()));
    parameterMap.put("endDate", new Date(end.toDateMidnight().getMillis()));
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
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<LoginData> getLogins(final LocalDate start, final LocalDate end, final String idpEntityId, final String spEntityId) {
    final List<LoginData> result = new ArrayList<LoginData>();
    
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    
    String query = "select * from aggregated_log_logins " +
        "where " +
        "entryday >= :startDate AND " +
        "entryday <= :endDate AND " +
        "(:spEntityId IS NULL OR spentityid = :spEntityId) AND " +
        "(:idpEntityId IS NULL OR idpentityid = :idpEntityId) " +
        "order by idpentityid, spentityid ";
    
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("startDate", new Date(start.toDateMidnight().getMillis()));
    parameterMap.put("endDate", new Date(end.toDateMidnight().getMillis()));
    parameterMap.put("spEntityId", spEntityId);
    parameterMap.put("idpEntityId", idpEntityId);
    
    namedJdbcTemplate.query(query, parameterMap , new RowMapper<Object>() {
      Map<LocalDate, Integer> queryResult = new HashMap<LocalDate, Integer>();
      String currentAggregateSp = null;
      String currentAggregateIdp = null;
      
      @Override
      public Object mapRow(ResultSet rs, int row) throws SQLException {
        String spEntityId = rs.getString("spentityid");
        String idpEntityId = rs.getString("idpentityid");
        /*
         * aggregate if sp/idp entityid differs from previous record
         * do not aggregate if on first record
         * if on last record, aggregate last entries
         */
        if ((!spEntityId.equals(currentAggregateSp) || !idpEntityId.equals(currentAggregateIdp)) && !rs.isFirst()) {
          result.add(aggregateCurrentEntry(rs, start, end, currentAggregateIdp, currentAggregateSp));
          queryResult = new HashMap<LocalDate, Integer>();
        } 
        currentAggregateIdp = idpEntityId;
        currentAggregateSp = spEntityId;
        queryResult.put(new LocalDate(rs.getDate("entryday")), rs.getInt("entrycount"));
        
       if (rs.isLast()) {
         // aggregate last set
         result.add(aggregateCurrentEntry(rs, start, end, currentAggregateIdp, currentAggregateSp));
       }

       /*
        * This is kinda weird, but single row results are stored in 
        * queryResult (hashmap) or aggregated in result (List<loginData)
        */
       return null;
     }

     private LoginData aggregateCurrentEntry(final ResultSet rs, final LocalDate start, final LocalDate end, final String idpEntityId, final String spEntityId) throws SQLException {
       //aggregate
       LoginData loginData = new LoginData();
       loginData.setIdpEntityId(idpEntityId);
       loginData.setSpEntityId(spEntityId);
       loginData.setIdpname(rs.getString("idpentityname"));
       loginData.setSpName(rs.getString("spentityname"));
       
       LocalDate current = start;
        
       int total = 0;
       while (current.isBefore(end.plusDays(1))) {
         if (null == queryResult.get(current)) {
           loginData.getData().add(0);
         } else {
           loginData.getData().add(queryResult.get(current));
           total += queryResult.get(current);
         }
         current = current.plusDays(1);
       }
       loginData.setTotal(total);
       loginData.setPointStart(start.toDate().getTime());
       loginData.setPointEnd(end.toDate().getTime());
       loginData.setPointInterval(1000L * 60L * 60L * 24L);
       return loginData;
     }
    });
    return result;
  }

  @Override
  public List<SpStatistic> getActiveServices(String userid, String idpEntityId) {
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    
    String query = "select max(loginstamp) as loginstamp, spentityid as spentityid, " +
    		"max(spentityname) as spentityname from log_logins " +
        "where " +
        "userid = :userId AND " +
        "idpentityid = :idpEntityId " +
        "group by spentityid";
    
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("userId", userid);
    parameterMap.put("idpEntityId", idpEntityId);
    
    return namedJdbcTemplate.query(query, parameterMap , new RowMapper<SpStatistic>(){

      @Override
      public SpStatistic mapRow(ResultSet rs, int row) throws SQLException {
        SpStatistic result = new SpStatistic();
        result.setEntryTime(rs.getTimestamp("loginstamp").getTime());
        result.setSpEntityId(rs.getString("spentityid"));
        result.setSpName(rs.getString("spentityname"));

        return result;
      }
      
    });
  }

  @Override
  public List<LoginEntry> getUnprocessedLoginEntries(int nrOfRecords) {
    Long aggregateStartingPoint = jdbcTemplate.queryForLong("select aggregatepoint from aggregate_meta_data");
    
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    
    String query = "select * from log_logins where loginstamp > :startingPoint order by loginstamp LIMIT :batchSize";
    
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("batchSize", nrOfRecords);
    parameterMap.put("startingPoint", new Timestamp(aggregateStartingPoint));
    
    return namedJdbcTemplate.query(query, parameterMap , new RowMapper<LoginEntry>(){
      @Override
      public LoginEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        String idpEntityId = rs.getString("idpentityid");
        String idpEntityName = rs.getString("idpentityname");
        Date loginDate = new Date(rs.getTimestamp("loginstamp").getTime());
        String spEntityId = rs.getString("spentityid");
        String spEntityName = rs.getString("spentityname");
        String userAgent = rs.getString("useragent");
        String userId = rs.getString("userid");
        String voName = rs.getString("voname");
        return new LoginEntry(idpEntityId, idpEntityName, loginDate, spEntityId, spEntityName, userAgent, userId, voName);
      }
    });
  }

  @Override
  public void setLoginEntriesProcessed(List<LoginEntry> entries) {
    LoginEntry last = entries.get(entries.size()-1);
    jdbcTemplate.update("update aggregate_meta_data set aggregatepoint = ?", last.getLoginDate().getTime());
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
