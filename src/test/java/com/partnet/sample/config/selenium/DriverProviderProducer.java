// Copyright (C) 2014 Partnet, Inc. Confidential and Proprietary
package com.partnet.sample.config.selenium;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Responsible for providing a {@link WebDriverProvider}. 
 *
 * @author <a href="mailto:rbascom@part.net">Ryan Bascom</a>
 */
public final class DriverProviderProducer
{
  private final static Logger LOG = LoggerFactory.getLogger(DriverProviderProducer.class);

  @Produces
  @Singleton
  public ConfigurableDriverProvider getWebDriverProvider()
  {
    LOG.info("WebDriverProviderProducer getWebDriverProvider");
    return new ConfigurableDriverProvider();
  }
  
}
