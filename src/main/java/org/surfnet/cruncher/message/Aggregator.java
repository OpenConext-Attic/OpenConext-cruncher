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

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.surfnet.cruncher.model.LoginEntry;
import org.surfnet.cruncher.repository.StatisticsRepository;

@Component("aggregator")
public class Aggregator {
  private static final Logger LOG = LoggerFactory.getLogger(Aggregator.class);

  @Inject
  private StatisticsRepository statisticsRepository;

  @Value("${aggregation.batch-size}")
  private int batchSize;

  @Transactional
  public void run() {
    LOG.debug("Running aggregation task, batch size {}", batchSize);
    List<LoginEntry> entries = statisticsRepository.getUnprocessedLoginEntries(batchSize);
    LOG.debug("Got {} unprocessed login entries", entries.size());
    aggregateLogin(entries);
    statisticsRepository.setLoginEntriesProcessed(entries);
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
      if (entryForDayExists(le)) {
        LOG.trace("Updating existing aggregated record for date {}, record: {}", le.getLoginDate(), le);
        statisticsRepository.updateAggregated(le.getIdpEntityId(), le.getSpEntityId(), le.getLoginDate());
      } else {
        LOG.trace("Inserting aggregated record for date {}, record: {}", le.getLoginDate(), le);
        statisticsRepository.insertAggregated(le);
      }
    }
  }

  /**
   * Whether an aggregated record exists for the given record
   * @param le the LoginEntry
   */
  private boolean entryForDayExists(LoginEntry le) {
    return statisticsRepository.aggregatedExists(le.getIdpEntityId(), le.getSpEntityId(), le.getLoginDate());
  }

}
