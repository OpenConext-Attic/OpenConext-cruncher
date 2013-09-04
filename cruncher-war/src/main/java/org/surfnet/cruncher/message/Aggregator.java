/*
 * Copyright 2013 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.surfnet.cruncher.message;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.surfnet.cruncher.model.LoginEntry;
import org.surfnet.cruncher.repository.StatisticsRepository;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Component("aggregator")
public class Aggregator {
  private static final DateTimeFormatter dateformat = DateTimeFormat.forPattern("yyyy-MM-dd");
  private static final Logger LOG = LoggerFactory.getLogger(Aggregator.class);
  
  public static String aggregationRecordHash(LoginEntry le) {
    return aggregationRecordHash(le.getIdpEntityId(), le.getSpEntityId(), le.getLoginDate());
  }

  public static String aggregationRecordHash(String idpEntityId, String spEntityId, Date loginDate) {
    String input = dateformat.print(loginDate.getTime()) + "!" + idpEntityId + "!" + spEntityId;
    return DigestUtils.sha1Hex(input);
  }

  public static String aggregationRecordHash(String userId, String spEntityId) {
    String input = userId + "!" + spEntityId;
    return DigestUtils.sha1Hex(input);
  }

  @Inject
  private StatisticsRepository statisticsRepository;
  
  @Inject
  private TransactionTemplate transactionTemplate;

  @Value("${aggregation.batch-size}")
  private int batchSize;

  @Value("${aggregation.enabled}")
  private boolean enabled;

  /*
   * When a (runtime) exception occurs, the active bit is *not* set to 0, this means
   * crunching is disabled until the original error is recovered
   */
  public void run() {
    AggregateCounts counts = new AggregateCounts();
    LOG.info("Running aggregation task, batch size {}", batchSize);
    long lockAquired = 0L;
    long loginsRetrieved = 0L;
    long crunched = 0L;
    long lockReleased = 0L;
    long totalTime = 0L;
    long startTime = now();

    if (!enabled) {
      LOG.info("Not running aggregation task, because aggregation.enabled=false");
      return;
    }
    if (statisticsRepository.lockForCrunching()) {
      lockAquired = now();
      List<LoginEntry> entries = statisticsRepository.getUnprocessedLoginEntries(batchSize);
      loginsRetrieved = now();
      LOG.debug("Got {} unprocessed login entries", entries.size());
      counts = aggregateLogin(entries);
      crunched = now();
      statisticsRepository.unlockForCrunching();
      lockReleased = now();

      // convert timestamp to time spend
      totalTime = now() - startTime;
      lockReleased = lockReleased - crunched;
      crunched = crunched - loginsRetrieved;
      loginsRetrieved = loginsRetrieved - lockAquired;
      lockAquired = lockAquired - startTime;
    } else {
      LOG.info("Someone else is crunching, not doing anything");
    }
    LOG.info("logins retrieved in " + loginsRetrieved + " ms, crunching took " + totalTime + " ms");
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("detailed results of this aggregate run:");
      LOG.debug("total number of records handled: " + counts.total);
      LOG.debug("number of new aggregation records inserted: " + counts.aggregated_insert);
      LOG.debug("number of aggregation records updated: " + counts.aggregated_update);
      LOG.debug("number of new user records inserted: " + counts.user_insert);
      LOG.debug("number of user records updated: " + counts.user_update);
      LOG.debug("detailed timing (in ms)");
      LOG.debug("time to aquire the 'lock': " + lockAquired);
      LOG.debug("time to retrieve the logins: " + loginsRetrieved);
      LOG.debug("time to crunch the data: " + crunched);
      LOG.debug("time to release the 'lock': " + lockReleased);
      LOG.debug("total time in this run: " + totalTime);
    }
  }

  private long now() {
    return System.currentTimeMillis();
  }

  /**
   * Use the given LoginEntries to aggregate data in the database.
   * This will
   * @param loginEntries
   */
  public AggregateCounts aggregateLogin(final List<LoginEntry> loginEntries) {
    if (loginEntries == null) {
      throw new IllegalArgumentException("List of loginEntries cannot be null.");
    }
    
    AggregateCounts counts = transactionTemplate.execute(new TransactionCallback<AggregateCounts>() {
      @Override
      public AggregateCounts doInTransaction(TransactionStatus status) {
        AggregateCounts result = new AggregateCounts();
        
        for (LoginEntry le : loginEntries) {
          result.total += 1;
          //aggregate the login
          if (entryForDayExists(le)) {
            LOG.trace("Updating existing aggregated record for date {}, record: {}", le.getLoginDate(), le);
            statisticsRepository.updateAggregated(le.getIdpEntityId(), le.getSpEntityId(), le.getLoginDate());
            result.aggregated_update += 1;
          } else {
            LOG.trace("Inserting aggregated record for date {}, record: {}", le.getLoginDate(), le);
            statisticsRepository.insertAggregated(le);
            result.aggregated_insert += 1;
          }
          
          //aggregate for the user
          if (entryForUserExists(le)) {
            LOG.trace("Updating existing aggregated record for date {}, record: {}", le.getLoginDate(), le);
            statisticsRepository.updateLastLogin(le.getUserId(), le.getSpEntityId(), le.getLoginDate());
            result.user_update += 1;
          } else {
            LOG.trace("Inserting aggregated record for date {}, record: {}", le.getLoginDate(), le);
            statisticsRepository.insertLastLogin(le);
            result.user_insert += 1;
          }
        }
        
        if (loginEntries.size() > 0) {
          statisticsRepository.setLoginEntriesProcessed(loginEntries);
        }
        
        return result;
      }
    });
    
    return counts;
  }

  @PreDestroy
  public void shutdownAggregator() {
    while (!statisticsRepository.lockForCrunching()) {
      LOG.warn("delaying shutdown, cruncher is still running");
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {}
    }
    statisticsRepository.unlockForCrunching();
  }
  
  private boolean entryForUserExists(LoginEntry le) {
    return statisticsRepository.lastLogonExists(le.getUserId(), le.getSpEntityId());
  }

  /**
   * Whether an aggregated record exists for the given record
   * @param le the LoginEntry
   */
  private boolean entryForDayExists(LoginEntry le) {
    return statisticsRepository.aggregatedExists(le.getIdpEntityId(), le.getSpEntityId(), le.getLoginDate());
  }
  
  private class AggregateCounts {
    public int total = 0;
    public int aggregated_insert = 0;
    public int aggregated_update = 0;
    public int user_insert = 0;
    public int user_update = 0;
  }
}
