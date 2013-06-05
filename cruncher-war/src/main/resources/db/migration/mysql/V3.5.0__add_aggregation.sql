CREATE TABLE `aggregated_log_logins` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `entryday` date NOT NULL,
  `spentityid` varchar(1000) NOT NULL,
  `idpentityid` varchar(1000) NOT NULL,
  `spentityname` varchar(1000) DEFAULT NULL,
  `idpentityname` varchar(1000) DEFAULT NULL,
  `entrycount` int(11) NOT NULL,
  `datespidphash` char(40) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `entryday` (`entryday`),
  KEY `spentityid` (`spentityid`),
  KEY `idpentityid` (`idpentityid`),
  UNIQUE KEY `COMPOUND_AGGREGATED_DATA` (datespidphash)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `user_log_logins` (
  `loginstamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `userid` varchar(1000) NOT NULL,
  `spentityid` varchar(1000) NOT NULL,
  `spentityname` varchar(1000) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `COMPOUND_USER_DATA` (userid(100), spentityid(100))
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE aggregate_meta_data (
  aggregatepoint bigint NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

INSERT INTO aggregate_meta_data (aggregatepoint)
  VALUES (0);