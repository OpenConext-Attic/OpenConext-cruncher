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

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.core.spi.scanning.Scanner;
import org.surfnet.cruncher.resource.CruncherResource;

import java.util.Set;

/**
 * PackagesResourceConfig that simply adds the relevant Resources to the
 * configuration. This is not as much magic as the use of a
 * {@link com.sun.jersey.core.spi.scanning.PackageNamesScanner}, but it this way much easier to override a
 * resource and add this one instead of the default.
 *
 */
public class DefaultPackagesResourceConfig extends PackagesResourceConfig {

  /**
   * We need to "fool" the initialization process with at least one package name.
   */
  public DefaultPackagesResourceConfig() {
    this(Class.class.getPackage().getName());
  }
  public DefaultPackagesResourceConfig(String... packages) {
    super(packages);
  }

  @Override
  public void init(Scanner scanner) {
    Set<Class<?>> classes = getClasses();
    /*
     * The REST resource
     */
    classes.add(CruncherResource.class);
  }
}

