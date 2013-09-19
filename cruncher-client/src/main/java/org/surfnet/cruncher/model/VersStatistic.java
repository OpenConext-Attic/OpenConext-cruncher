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
package org.surfnet.cruncher.model;

import java.util.HashMap;
import java.util.Map;

public class VersStatistic {
  private long totalLoginCount;
  private long uniqueLoginCount;
  private Map<String, Long> institutionCounts = new HashMap<String,Long>();
  
  public long getTotalLoginCount() {
    return totalLoginCount;
  }
  public void setTotalLoginCount(long totalLoginCount) {
    this.totalLoginCount = totalLoginCount;
  }
  public long getUniqueLoginCount() {
    return uniqueLoginCount;
  }
  public void setUniqueLoginCount(long uniqueLoginCount) {
    this.uniqueLoginCount = uniqueLoginCount;
  }
  public Map<String, Long> getInstitutionCounts() {
    return institutionCounts;
  }
  public void addInstitutionCount(final String key, final Long value) {
    if (null == institutionCounts.get(key)) {
      institutionCounts.put(key, value);
    } else {
      institutionCounts.put(key, (institutionCounts.get(key)+value));
    }
  }
}
