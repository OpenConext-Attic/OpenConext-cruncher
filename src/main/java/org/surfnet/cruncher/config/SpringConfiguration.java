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
package org.surfnet.cruncher.config;

import com.googlecode.flyway.core.Flyway;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.surfnet.cruncher.message.LoginMessageListener;

import javax.inject.Inject;

@Configuration
@PropertySource("classpath:cruncher.application.properties")
/*
 * The component scan can be used to add packages and exclusions to the default
 * package
 */
@ComponentScan(basePackages = {"org.surfnet.cruncher"})
public class SpringConfiguration {

  @Inject
  Environment env;

  @Bean
  public javax.sql.DataSource dataSource() {
    DataSource dataSource = new DataSource();
    dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
    dataSource.setUrl(env.getProperty("jdbc.url"));
    dataSource.setUsername(env.getProperty("jdbc.username"));
    dataSource.setPassword(env.getProperty("jdbc.password"));
   // dataSource.setTestOnBorrow(true);
   // dataSource.setValidationQuery("SELECT 1");
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
  public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource());
  }

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {
    return new JedisConnectionFactory();
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate() {
    final RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
    template.setConnectionFactory(jedisConnectionFactory());
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new GenericToStringSerializer<Object>(Object.class));
    template.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
    return template;
  }

  @Bean
  public MessageListenerAdapter messageListener() {
    return new MessageListenerAdapter(new LoginMessageListener());
  }

  @Bean
  public RedisMessageListenerContainer redisContainer() {
    final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(jedisConnectionFactory());
    container.addMessageListener(messageListener(), topic());
    return container;
  }

  @Bean
  ChannelTopic topic() {
    return new ChannelTopic("pubsub:queue");
  }

}
