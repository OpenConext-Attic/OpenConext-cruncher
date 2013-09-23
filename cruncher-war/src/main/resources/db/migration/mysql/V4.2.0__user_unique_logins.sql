CREATE TABLE `user_unique_logins` (
  `spentityid` varchar(1000) NOT NULL,
  `idpentityid` varchar(1000) NOT NULL,
  `entrycount` int(11) NOT NULL,
  `timespan` int(11) NOT NULL,
  `month` int(11) DEFAULT NULL,
  `year` int(11) DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `user_unique_logins_cache` (
  `userid` varchar(1000) NOT NULL,
  `spentityid` varchar(1000) NOT NULL,
  `idpentityid` varchar(1000) NOT NULL,
  `timespan` int(11) NOT NULL,
  `month` int(11) DEFAULT NULL,
  `year` int(11) DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
