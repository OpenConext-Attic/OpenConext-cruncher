package org.surfnet.cruncher.message;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.surfnet.cruncher.repository.StatisticsRepository;

@Component("cleaner")
public class Cleaner {
  private static final Logger LOG = LoggerFactory.getLogger(Cleaner.class);
  
  @Inject
  private StatisticsRepository statisticsRepository;
  
  @Value("${cleaner.retention}")
  private int retention;
  
  @Value("${cleaner.enabled}")
  private boolean enabled;
  
  public void run() {
    if (!enabled) {
      LOG.info("cleaning disabled, because cleaning.enabled=false");
      return;
    }
    LOG.info("Running database cleanup for the cruncher retention period is " + retention + " month(s)");
    statisticsRepository.cleanTables(retention);
  }
  
  void setRetention(int retention) {
    this.retention = retention;
  }
}
