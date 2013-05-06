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

import org.springframework.jdbc.core.JdbcTemplate;
import org.surfnet.cruncher.model.LoginData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

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

  /*
  INSERT INTO table (key,col1) VALUES (1,2)
  ON DUPLICATE KEY UPDATE col1 = 2;

OR

use plain sql for compatibil
   */

}
