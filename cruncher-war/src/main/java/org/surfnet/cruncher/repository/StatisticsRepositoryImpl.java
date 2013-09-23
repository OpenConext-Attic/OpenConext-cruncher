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

import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static org.surfnet.cruncher.message.Aggregator.aggregationRecordHash;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.surfnet.cruncher.model.LoginData;
import org.surfnet.cruncher.model.LoginEntry;
import org.surfnet.cruncher.model.SpStatistic;
import org.surfnet.cruncher.model.VersStatistic;

@Named
public class StatisticsRepositoryImpl implements StatisticsRepository {

  private static final Logger LOG = LoggerFactory.getLogger(StatisticsRepositoryImpl.class);
  private static final long POINT_INTERVAL = 24L * 60L * 60L * 1000L;
  
  /**
   * This simple enum is used to identify different timespan 'types' in the unique
   * user logins table. Currently we only use and support unique logins per month.
   * The database is however already prepared for unique logins per week or year if
   * required.
   */
  private enum TimeSpan {
    MONTH(12), WEEK(52), YEAR(1970);
    private final int code;
    private TimeSpan(int code) {
      this.code = code;
    }
    public int getCode() {
      return this.code;
    }
  }

  @Inject
  private JdbcTemplate ebJdbcTemplate;
  
  @Inject
  private JdbcTemplate cruncherJdbcTemplate;

  /**
   * {@inheritDoc}
   */
  @Override
  public List<LoginData> getLogins(final LocalDate start, final LocalDate end, final String idpEntityId, final String spEntityId) {
    final List<LoginData> result = new ArrayList<LoginData>();
    
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(cruncherJdbcTemplate);
    
    String query = "select * from aggregated_log_logins " +
        "where " +
        "entryday >= :startDate AND " +
        "entryday <= :endDate AND " +
        "(:spEntityId IS NULL OR spentityid = :spEntityId) AND " +
        "(:idpEntityId IS NULL OR idpentityid = :idpEntityId) " +
        "order by idpentityid, spentityid, entryday ";

    Map<String, Object> parameterMap = getParameterMap(start, end, idpEntityId, spEntityId);
    
    namedJdbcTemplate.query(query, parameterMap , new RowMapper<Object>() {
      private Map<LocalDate, Integer> queryResult = new HashMap<LocalDate, Integer>();
      private LoginData currentAggregate = null;
      
      @Override
      public Object mapRow(ResultSet rs, int row) throws SQLException {
        LoginData currentRow = getLoginDataFromRow(rs);
        
        /*
         * aggregate if sp/idp entityid differs from previous record
         * do not aggregate if on first record
         * if on last record, aggregate last entries
         */
        if (!currentRow.equals(currentAggregate) && !rs.isFirst()) {
          //record is different, aggregate previous one and start fresh
          result.add(aggregateCurrentEntry(currentAggregate, start, end));
          queryResult = new HashMap<LocalDate, Integer>();
          
        }
        queryResult.put(new LocalDate(rs.getDate("entryday")), rs.getInt("entrycount"));
        currentAggregate = currentRow;
        
       if (rs.isLast()) {
         // aggregate last set
         result.add(aggregateCurrentEntry(currentAggregate, start, end));
       }

       /*
        * This is kinda weird, but single row results are stored in 
        * queryResult (hashmap) or aggregated in result (List<loginData)
        */
       return null;
     }

     private LoginData aggregateCurrentEntry(final LoginData loginData, final LocalDate start, final LocalDate end) {
       LocalDate current = start;
        
       int total = 0;
       while (current.isBefore(end.plusDays(1))) {
         Integer count = queryResult.get(current);
         if (count == null) {
           loginData.getData().add(0);
         } else {
           loginData.getData().add(count);
           total += count;
         }
         current = current.plusDays(1);
       }
       loginData.setTotal(total);
       loginData.setPointStart(start.toDate().getTime());
       loginData.setPointEnd(end.toDate().getTime());
       loginData.setPointInterval(POINT_INTERVAL);
       return loginData;
     }
     
     private LoginData getLoginDataFromRow(ResultSet rs) throws SQLException {
       LoginData result = new LoginData();
       result.setIdpEntityId(rs.getString("idpentityid"));
       result.setIdpname(rs.getString("idpentityname"));
       result.setSpEntityId(rs.getString("spentityid"));
       result.setSpName(rs.getString("spentityname"));
       return result;
     }
    });
    return result;
  }

  @Override
  public List<SpStatistic> getActiveServices(String userid, String idpEntityId) {
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(cruncherJdbcTemplate);
    
    String query = "select loginstamp as loginstamp, spentityid as spentityid, " +
    		"spentityname as spentityname from user_log_logins " +
        "where " +
        "userid = :userId AND " +
        "idpentityid = :idpEntityId ";
    
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
    Long aggregateStartingPoint = cruncherJdbcTemplate.queryForLong("select aggregatepoint from aggregate_meta_data");
    
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(ebJdbcTemplate);
    
    String query = "select * from log_logins where id > :startingPoint order by id LIMIT :batchSize";
    
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("batchSize", nrOfRecords);
    parameterMap.put("startingPoint", aggregateStartingPoint);
    
    return namedJdbcTemplate.query(query, parameterMap , new RowMapper<LoginEntry>(){
      @Override
      public LoginEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        String idpEntityId = rs.getString("idpentityid");
        String idpEntityName = rs.getString("idpentityname");
        Date loginDate = new Date(rs.getTimestamp("loginstamp").getTime());
        String spEntityId = rs.getString("spentityid");
        String spEntityName = rs.getString("spentityname");
        String userId = rs.getString("userid");
        return new LoginEntry(id, idpEntityId, idpEntityName, loginDate, spEntityId, spEntityName, userId);
      }
    });
  }

  @Override
  public void setLoginEntriesProcessed(List<LoginEntry> entries) {
    LoginEntry last = entries.get(entries.size()-1);
    cruncherJdbcTemplate.update("update aggregate_meta_data set aggregatepoint = ?", last.getId());
  }

  @Override
  public void updateAggregated(String idpEntityId, String spEntityId, Date loginDate) {
    cruncherJdbcTemplate.update("update aggregated_log_logins set entrycount = entrycount + 1 where datespidphash = ?", aggregationRecordHash(idpEntityId, spEntityId, loginDate));
  }

  @Override
  public void insertAggregated(LoginEntry le) {
    LOG.debug("Inserting new aggregated record for date {}, record: {}, hash: {}", new Object[] {le.getLoginDate(), le, aggregationRecordHash(le)});
    cruncherJdbcTemplate.update("insert into aggregated_log_logins (entryday,spentityid,idpentityid,spentityname,idpentityname, datespidphash, entrycount)" +
      " values (?, ?, ?, ?, ?, ?, 1)",
      le.getLoginDate(), le.getSpEntityId(), le.getIdpEntityId(), le.getSpEntityName(), le.getIdpEntityName(), aggregationRecordHash(le));
  }

  @Override
  public boolean aggregatedExists(String idpEntityId, String spEntityId, Date loginDate) {
    return cruncherJdbcTemplate.queryForInt("select count(*) from aggregated_log_logins where datespidphash = ?", aggregationRecordHash(idpEntityId, spEntityId, loginDate)) == 1;
  }

  @Override
  public boolean lockForCrunching() {
    int rowCount = cruncherJdbcTemplate.update("update aggregate_meta_data set active=1 where active=0");
    return rowCount != 0;
  }

  @Override
  public void unlockForCrunching() {
    cruncherJdbcTemplate.update("update aggregate_meta_data set active=0");
  }

  @Override
  public boolean lastLogonExists(String userId, String spEntityId) {
    return cruncherJdbcTemplate.queryForInt("select count(*) from user_log_logins where usersphash = ?", aggregationRecordHash(userId, spEntityId)) == 1;
  }

  @Override
  public void insertLastLogin(LoginEntry le) {
    LOG.debug("Inserting new aggregated user record for last login on date {}, record: {}", new Object[] {le.getLoginDate(), le});
    cruncherJdbcTemplate.update("insert into user_log_logins (loginstamp,userid,spentityid,spentityname,idpentityid,usersphash)" +
      " values (?, ?, ?, ?, ?, ?)",
      le.getLoginDate(), le.getUserId(), le.getSpEntityId(), le.getSpEntityName(), le.getIdpEntityId(), aggregationRecordHash(le.getUserId(), le.getSpEntityId()));
  }

  @Override
  public void updateLastLogin(String userId, String spEntityId, Date loginDate) {
    cruncherJdbcTemplate.update("update user_log_logins set loginstamp = ? where usersphash = ?", loginDate, aggregationRecordHash(userId, spEntityId));
  }

  private Map<String, Object> getParameterMap(LocalDate start, LocalDate end, String idpEntityId, String spEntityId) {
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("startDate", new Date(start.toDateMidnight().getMillis()));
    parameterMap.put("endDate", new Date(end.toDateMidnight().getMillis()));
    parameterMap.put("spEntityId", spEntityId);
    parameterMap.put("idpEntityId", idpEntityId);
    return parameterMap;
  }

  @Override
  public void cleanTables(int retention) {
    Calendar retentionTime = createRetentionTime(retention);
    int logins_cleaned = cleanAggregatedLogins(retentionTime.getTime());
    int users_cleaned = cleanUserLogins(retentionTime.getTime());
    LOG.info("removed {} logins records and {} user records", logins_cleaned, users_cleaned);
  }

  private Calendar createRetentionTime(int retention) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    Calendar retentionTime = new GregorianCalendar();
    retentionTime.setLenient(true);
    retentionTime.add(Calendar.MONTH, -retention);
    LOG.debug("Cleaning tables to date " + sdf.format(retentionTime.getTime()));
    return retentionTime;
  }
  
  private int cleanAggregatedLogins(Date retentionTime) {
    String sql = "delete from aggregated_log_logins where entryday <= ?";
    return cruncherJdbcTemplate.update(sql, retentionTime);
  }
  
  private int cleanUserLogins(Date retentionTime) {
    String sql = "delete from user_log_logins where loginstamp <= ?";
    return cruncherJdbcTemplate.update(sql, retentionTime);
  }

  @Override
  public VersStatistic getVersStats(LocalDate startDate, LocalDate endDate, String spEntityId) {
    final VersStatistic result = new VersStatistic();
    
    NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(cruncherJdbcTemplate);
    String query =
        "select idpentityid, sum(entrycount) as loginCount from aggregated_log_logins " +
        "where " +
        "entryday >= :startDate AND " +
        "entryday <= :endDate AND " +
        "spentityid = :spEntityId " +
        "group by idpentityid " +
        "order by idpentityid";
    
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("startDate", new Date(startDate.toDateMidnight().getMillis()));
    parameterMap.put("endDate", new Date(endDate.toDateMidnight().getMillis()));
    parameterMap.put("spEntityId", spEntityId);
    
    namedJdbcTemplate.query(query, parameterMap , new RowMapper<VersStatistic>() {
      
      @Override
      public VersStatistic mapRow(ResultSet rs, int row) throws SQLException {
        String idpEntityId = rs.getString("idpEntityId");
        result.addInstitutionLoginCount(idpEntityId, rs.getLong("loginCount"));
        
        // no rowbased result
        return null;
      }
    });
    
    return result;
  }

  @Override
  public void insertUniqueLoginInCache(LoginEntry le) {
    Calendar entryDate = new GregorianCalendar();
    entryDate.setTime(le.getLoginDate());
    int month = entryDate.get(MONTH) + 1; //this stupid thing is 0 based
    int year = entryDate.get(YEAR);
    
    LOG.debug("Inserting new unique user record for user {} on date {}, record: {}", new Object[] {le.getUserId(), le.getLoginDate(), le});
    cruncherJdbcTemplate.update("insert into user_unique_logins_cache (userid,spentityid,idpentityid,timespan,month,year)" +
      " values (?, ?, ?, ?, ?, ?)",
      le.getUserId(), le.getSpEntityId(), le.getIdpEntityId(), TimeSpan.MONTH.getCode(), month, year);
  }

  @Override
  public boolean uniqueUserLogonExists(String userId, Date loginDate, String spEntityId, String idpEntityId) {
    NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(cruncherJdbcTemplate);
    String query =
    		"select count(*) from user_unique_logins_cache " +
    		"where " +
    		"userId=:userId AND " +
    		"spEntityId=:spEntityId AND " +
    		"idpEntityId=:idpEntityId AND " +
    		"timespan=:timespan AND " +
    		"month=:month AND " +
    		"year=:year";
    
    Calendar entryDate = new GregorianCalendar();
    entryDate.setTime(loginDate);
    int month = entryDate.get(MONTH) + 1; //this stupid thing is 0 based
    int year = entryDate.get(YEAR);
    
    Map<String, Object> parameterMap = new HashMap<String, Object>();
    parameterMap.put("userId", userId);
    parameterMap.put("spEntityId", spEntityId);
    parameterMap.put("idpEntityId", idpEntityId);
    parameterMap.put("timespan", TimeSpan.MONTH.getCode());
    parameterMap.put("month", month);
    parameterMap.put("year", year);
    
    return namedTemplate.queryForInt(query, parameterMap) > 0;
  }

  @Override
  public void aggregateUniqueLoginsIfNeeded() {
    Calendar cruncherTime = getCurrentCruncherTime();
    int month = cruncherTime.get(MONTH) + 1; //this stupid thing is 0 based
    int year = cruncherTime.get(YEAR);
    
    /* check whether we have rows to aggregate */
    int rowCount = cruncherJdbcTemplate.queryForInt("select count(*) from user_unique_logins_cache where month < ? OR year < ?", month, year);
    if (rowCount > 0) {
      LOG.debug("found {} rows in the unique user login cache that need to move to the unique user login table", rowCount);
      aggregateUserUniqueLogins(month, year);
    } else {
      LOG.debug("currently no records found to aggregate in the user_unique_logins tables");
    }
  }

  private void aggregateUserUniqueLogins(int month, int year) {
    String query = "insert into user_unique_logins " +
        "(spentityid, idpentityid, entrycount, timespan, month, year) " +
        "select " +
        "spentityid, idpentityid, count(*), timespan, month, year " +
        "from user_unique_logins_cache  " +
        "where " +
        "month < ? OR year < ? " +
        "group by spentityid, idpentityid, timespan, month, year";
    cruncherJdbcTemplate.update(query, month, year);
    
    /* remove the entries we just aggregated */
    cruncherJdbcTemplate.update("delete from user_unique_logins_cache where month < ? OR year < ?", month, year);
  }

  private Calendar getCurrentCruncherTime() {
    /* determine the current point in time where the cruncher works on */
    Date currentDate = cruncherJdbcTemplate.queryForObject("select entryday from aggregated_log_logins order by id DESC LIMIT 1;", Date.class);
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(currentDate);
    return calendar;
  }

  @Override
  public long getTotalUniqueLogins(String spEntityId, Integer month, Integer year) {
    String query = "select SUM(entrycount) " +
    		"from user_unique_logins " +
    		"where " +
    		"spentityid=? and timespan=? and month=? and year=?";
    return cruncherJdbcTemplate.queryForLong(query, spEntityId, TimeSpan.MONTH.getCode(), month, year);
  }
}
