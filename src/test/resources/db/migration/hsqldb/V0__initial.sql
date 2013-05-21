DROP TABLE IF EXISTS log_logins;
CREATE TABLE log_logins (
  loginstamp timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  userid varchar(1000) NOT NULL,
  spentityid varchar(1000) DEFAULT NULL,
  idpentityid varchar(1000) DEFAULT NULL,
  spentityname varchar(1000) DEFAULT NULL,
  idpentityname varchar(1000) DEFAULT NULL,
  useragent varchar(1024) DEFAULT NULL,
  voname varchar(1024) DEFAULT NULL,
  id bigint generated by default as identity (start with 20000),
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS aggregated_log_logins;
CREATE TABLE aggregated_log_logins (
  entryday date NOT NULL,
  spentityid varchar(1000) DEFAULT NULL,
  idpentityid varchar(1000) DEFAULT NULL,
  spentityname varchar(1000) DEFAULT NULL,
  idpentityname varchar(1000) DEFAULT NULL,
  entrycount bigint NOT NULL,
  datespidphash varchar(100) NOT NULL,
  id bigint generated by default as identity (start with 20000),
  PRIMARY KEY (id),
  UNIQUE (datespidphash)
);

DROP TABLE IF EXISTS user_log_logins;
CREATE TABLE user_log_logins (
  loginstamp timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  userid varchar(1000) NOT NULL,
  spentityid varchar(1000) DEFAULT NULL,
  spentityname varchar(1000) DEFAULT NULL,
  id bigint generated by default as identity (start with 20000),
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS conversion_migration;
CREATE TABLE conversion_migration (
  loginstamp timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  log_logins_processed_id bigint NOT NULL,
  id bigint generated by default as identity (start with 20000),
  PRIMARY KEY (id)
);

INSERT INTO log_logins (loginstamp, userid, spentityid, idpentityid)
  VALUES ('2012-04-18 11:48:41','user_1', 'sp1', 'idp1');
INSERT INTO log_logins (loginstamp, userid, spentityid, idpentityid)
  VALUES ('2012-04-19 11:48:41','user_1', 'sp2', 'idp2');
INSERT INTO log_logins (loginstamp, userid, spentityid, idpentityid)
  VALUES ('2012-04-20 11:48:41','user_1', 'sp1', 'idp2');
INSERT INTO log_logins (loginstamp, userid, spentityid, idpentityid)
  VALUES ('2012-04-20 11:48:41','user_2', 'sp3', 'idp2');
INSERT INTO log_logins (loginstamp, userid, spentityid, idpentityid)
  VALUES ('2012-04-21 11:48:41','user_3', 'sp1', 'idp2');
INSERT INTO log_logins (loginstamp, userid, spentityid, idpentityid)
  VALUES ('2012-04-21 11:48:41','user_4', 'sp1', 'idp2');
INSERT INTO log_logins (loginstamp, userid, spentityid, idpentityid)
  VALUES ('2012-04-21 11:48:41','user_4', 'sp2', 'idp3');
INSERT INTO log_logins (loginstamp, userid, spentityid, idpentityid)
  VALUES ('2012-04-22 11:48:41','user_4', 'sp3', 'idp3');

  
  

INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-01', 'sp1', 'idp1', 20, 1);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-02', 'sp1', 'idp1', 20, 2);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-03', 'sp1', 'idp1', 20, 3);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-04', 'sp1', 'idp1', 20, 4);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-05', 'sp1', 'idp1', 20, 5);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-06', 'sp1', 'idp1', 20, 6);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-07', 'sp1', 'idp1', 20, 7);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-08', 'sp1', 'idp1', 20, 8);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-09', 'sp1', 'idp1', 20, 9);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-10', 'sp1', 'idp1', 20, 10);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-11', 'sp1', 'idp1', 20, 11);
INSERT INTO aggregated_log_logins (entryday, spentityid, idpentityid, entrycount, datespidphash)
  VALUES('2013-01-12', 'sp1', 'idp1', 20, 12);