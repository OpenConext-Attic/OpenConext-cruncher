package org.surfnet.cruncher.message;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.surfnet.cruncher.unittest.config.SpringConfigurationForTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfigurationForTest.class)
@Transactional
@TransactionConfiguration(defaultRollback=true)
public class CleanerTest {

  @Inject
  private JdbcTemplate cruncherJdbcTemplate;
  
  @Inject
  private Cleaner cleaner;
  
  @Test
  public void cleanerRemovesNoData() {
    int loginRecords = cruncherJdbcTemplate.queryForInt("select count(*) from aggregated_log_logins");
    int userRecords = cruncherJdbcTemplate.queryForInt("select count(*) from user_log_logins");
    cleaner.setRetention(10000);
    cleaner.run();
    int new_loginRecords = cruncherJdbcTemplate.queryForInt("select count(*) from aggregated_log_logins");
    int new_userRecords = cruncherJdbcTemplate.queryForInt("select count(*) from user_log_logins");
    // no records should have been removed
    assertEquals(loginRecords, new_loginRecords);
    assertEquals(userRecords, new_userRecords);
  }
  
  @Test
  public void cleanerRemovesData() {
    int loginRecords = cruncherJdbcTemplate.queryForInt("select count(*) from aggregated_log_logins");
    int userRecords = cruncherJdbcTemplate.queryForInt("select count(*) from user_log_logins");
    insertLoginTestData();
    int new_loginRecords = cruncherJdbcTemplate.queryForInt("select count(*) from aggregated_log_logins");
    int new_userRecords = cruncherJdbcTemplate.queryForInt("select count(*) from user_log_logins");
    assertTrue(new_loginRecords == loginRecords+2);
    assertTrue(new_userRecords == userRecords+2);
    cleaner.setRetention(30);
    cleaner.run();
    new_loginRecords = cruncherJdbcTemplate.queryForInt("select count(*) from aggregated_log_logins");
    new_userRecords = cruncherJdbcTemplate.queryForInt("select count(*) from user_log_logins");
    // old record should have been removed
    assertEquals(loginRecords, new_loginRecords);
    assertEquals(userRecords, new_userRecords);
  }
  
  @Test
  public void cleanerKeepsOldData() {
    int loginRecords = cruncherJdbcTemplate.queryForInt("select count(*) from aggregated_log_logins");
    int userRecords = cruncherJdbcTemplate.queryForInt("select count(*) from user_log_logins");
    insertLoginTestData();
    int new_loginRecords = cruncherJdbcTemplate.queryForInt("select count(*) from aggregated_log_logins");
    int new_userRecords = cruncherJdbcTemplate.queryForInt("select count(*) from user_log_logins");
    assertTrue(new_loginRecords == loginRecords+2);
    assertTrue(new_userRecords == userRecords+2);
    cleaner.setRetention(31);
    cleaner.run();
    new_loginRecords = cruncherJdbcTemplate.queryForInt("select count(*) from aggregated_log_logins");
    new_userRecords = cruncherJdbcTemplate.queryForInt("select count(*) from user_log_logins");
    // old record should not have been removed
    assertTrue(new_loginRecords == loginRecords+2);
    assertTrue(new_userRecords == userRecords+2);
  }
  
  private void insertLoginTestData() {
    Calendar history = new GregorianCalendar();
    history.setLenient(true);
    history.add(Calendar.MONTH, -30);
    cruncherJdbcTemplate.update("insert into aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash) VALUES (?, 'tempsp', 'tempidp', 30, 'hash')", history.getTime());
    cruncherJdbcTemplate.update("insert into user_log_logins (loginstamp, userid, spentityid, idpentityid, usersphash) VALUES (?, 'user1', 'sp1', 'idp1', 'user1hash')", history.getTime());
    history.add(Calendar.DAY_OF_WEEK, -2);
    cruncherJdbcTemplate.update("insert into aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash) VALUES (?, 'tempsp', 'tempidp', 40, 'newhash')", history.getTime());
    cruncherJdbcTemplate.update("insert into user_log_logins (loginstamp, userid, spentityid, idpentityid, usersphash) VALUES (?, 'user2', 'sp1', 'idp1', 'user2hash')", history.getTime());
  }
}
