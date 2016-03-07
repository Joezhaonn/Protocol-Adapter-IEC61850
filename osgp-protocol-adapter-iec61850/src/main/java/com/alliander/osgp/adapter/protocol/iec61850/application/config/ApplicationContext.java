/**
 * Copyright 2014-2016 Smart Society Services B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */
package com.alliander.osgp.adapter.protocol.iec61850.application.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.alliander.osgp.core.db.api.application.config.OsgpCoreDbApiPersistenceConfig;

/**
 * An application context Java configuration class. The usage of Java
 * configuration requires Spring Framework 3.0
 */
@Configuration
@ComponentScan(basePackages = { "com.alliander.osgp.adapter.protocol.iec61850", "com.alliander.osgp.core.db.api" })
@EnableTransactionManagement()
@Import({ MessagingConfig.class, OsgpCoreDbApiPersistenceConfig.class, Iec61850Config.class })
@PropertySource("file:${osp/osgpAdapterProtocolIec61850/config}")
public class ApplicationContext {

}