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
package org.surfnet.cruncher.message;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.surfnet.cruncher.config.SpringConfigurationTest;

public class NotificationMessageListenerTest extends SpringConfigurationTest {

//  @Inject
  private RedisTemplate< String, Object > redisTemplate;

//  @Inject
  private ChannelTopic topic;

//  @Test
  public void testOnMessage() throws Exception {
//    for (int i = 0; i < 100; i++) {
//      System.out.println("Sending message...........");
//      redisTemplate.convertAndSend( topic.getTopic(), "Message " + counter.incrementAndGet() +
//              ", " + Thread.currentThread().getName() );
//    }
//    Thread.sleep(2500000);
//    BoundListOperations<String,Object> queue = redisTemplate.boundListOps("messages");
//    Object first;
//    while ((first = queue.leftPop()) != null) {
//      System.out.println(first);
//    }
//    redisTemplate.execute()

  }
}
