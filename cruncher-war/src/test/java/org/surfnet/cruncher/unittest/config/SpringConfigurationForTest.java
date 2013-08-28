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
package org.surfnet.cruncher.unittest.config;

import com.googlecode.flyway.core.Flyway;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;

@Configuration
@PropertySource({"classpath:application.properties","classpath:cruncher.properties"})
@ImportResource("classpath:property-placeholder.xml")
/*
 * The component scan can be used to add packages and exclusions to the default
 * package
 */
@ComponentScan(basePackages = {"org.surfnet.cruncher.test.config","org.surfnet.cruncher.repository","org.surfnet.cruncher.message","org.surfnet.cruncher.resource"})
@EnableTransactionManagement
public class SpringConfigurationForTest {

  @Inject
  Environment env;

  /*
   * For test we only have one datasource. In this database we also populate
   * some EB tables;
   */
  @Bean
  public javax.sql.DataSource dataSource() {
    DataSource dataSource = new DataSource();
    dataSource.setDriverClassName(env.getProperty("eb.jdbc.driverClassName"));
    dataSource.setUrl(env.getProperty("eb.jdbc.url"));
    dataSource.setUsername(env.getProperty("eb.jdbc.username"));
    dataSource.setPassword(env.getProperty("eb.jdbc.password"));
    return dataSource;
  }

  @Bean
  public Flyway flyway() {
    final Flyway flyway = new Flyway();
    flyway.setInitOnMigrate(true);
    flyway.setDataSource(dataSource());
    String locationsValue = env.getProperty("flyway.migrations.location");
    String[] locations = locationsValue.split("\\s*,\\s*");
    flyway.setLocations(locations);
    flyway.migrate();
    return flyway;
  }

  @Bean
  public JdbcTemplate ebJdbcTemplate() {
    return new JdbcTemplate(dataSource());
  }
  
  @Bean
  public JdbcTemplate cruncherJdbcTemplate() {
    return new JdbcTemplate(dataSource());
  }
  
  @Bean
  public PlatformTransactionManager transactionManager() {
    return new DataSourceTransactionManager(dataSource());
  }
  
  @Bean
  public TransactionTemplate transactionTemplate() {
    return new TransactionTemplate(transactionManager());
  }
}
