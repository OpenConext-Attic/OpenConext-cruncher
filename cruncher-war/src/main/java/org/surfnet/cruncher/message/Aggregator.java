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

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.surfnet.cruncher.model.LoginEntry;
import org.surfnet.cruncher.repository.StatisticsRepository;

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

  @Value("${aggregation.batch-size}")
  private int batchSize;

  /*
   * When a (runtime) exception occurs, the active bit is *not* set to 0, this means
   * crunching is disabled until the original error is recovered
   */
  public void run() {
    LOG.debug("Running aggregation task, batch size {}", batchSize);
    if (statisticsRepository.lockForCrunching()) {
      List<LoginEntry> entries = statisticsRepository.getUnprocessedLoginEntries(batchSize);
      LOG.debug("Got {} unprocessed login entries", entries.size());
      aggregateLogin(entries);
      if (entries.size() > 0) {
        statisticsRepository.setLoginEntriesProcessed(entries);
      }
      statisticsRepository.unlockForCrunching();
    } else {
      LOG.debug("Someone else is crunching, not doing anything");
    }
  }

  /**
   * Use the given LoginEntries to aggregate data in the database.
   * This will
   * @param loginEntries
   */
  public void aggregateLogin(List<LoginEntry> loginEntries) {
    if (loginEntries == null) {
      throw new IllegalArgumentException("List of loginEntries cannot be null.");
    }

    for (LoginEntry le : loginEntries) {
      //aggregate the login
      if (entryForDayExists(le)) {
        LOG.trace("Updating existing aggregated record for date {}, record: {}", le.getLoginDate(), le);
        statisticsRepository.updateAggregated(le.getIdpEntityId(), le.getSpEntityId(), le.getLoginDate());
      } else {
        LOG.trace("Inserting aggregated record for date {}, record: {}", le.getLoginDate(), le);
        statisticsRepository.insertAggregated(le);
      }
      
      //aggregate for the user
      if (entryForUserExists(le)) {
        LOG.trace("Updating existing aggregated record for date {}, record: {}", le.getLoginDate(), le);
        statisticsRepository.updateLastLogin(le.getUserId(), le.getSpEntityId(), le.getLoginDate());
      } else {
        LOG.trace("Inserting aggregated record for date {}, record: {}", le.getLoginDate(), le);
        statisticsRepository.insertLastLogin(le);
      }
    }
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

}
