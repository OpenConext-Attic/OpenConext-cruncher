CREATE TABLE `log_logins` (
  `loginstamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `userid` varchar(1000) NOT NULL,
  `spentityid` varchar(1000) DEFAULT NULL,
  `idpentityid` varchar(1000) DEFAULT NULL,
  `spentityname` varchar(1000) DEFAULT NULL,
  `idpentityname` varchar(1000) DEFAULT NULL,
  `useragent` varchar(1024) DEFAULT NULL,
  `voname` varchar(1024) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `aggregated_log_logins` (
  `entryday` date NOT NULL,
  `spentityid` varchar(1000) NOT NULL,
  `idpentityid` varchar(1000) NOT NULL,
  `spentityname` varchar(1000) DEFAULT NULL,
  `idpentityname` varchar(1000) DEFAULT NULL,
  `entrycount` int(11) NOT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `COMPOUND_AGGREGATED_DATA` (`entryday`,`spentityid`,`idpentityid`),
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `user_log_logins` (
  `loginstamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `userid` varchar(1000) NOT NULL,
  `spentityid` varchar(1000) NOT NULL,
  `spentityname` varchar(1000) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `COMPOUND_USER_DATA` (`userid`,`spentityid`),
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
