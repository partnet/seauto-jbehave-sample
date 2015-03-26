// Copyright (C) 2014 Partnet, Inc. Confidential and Proprietary
package seauto.sample.config.jbehave;

import java.text.SimpleDateFormat;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jbehave.core.annotations.weld.WeldConfiguration;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.RegexStoryParser;
import org.jbehave.core.reporters.ConsoleOutput;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.MarkUnmatchedStepsAsPending;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.DateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partnet.automation.RuntimeConfiguration;
import com.partnet.automation.jbehave.LoggingStoryReporter;
import com.partnet.automation.jbehave.StoryParameterEnumConverter;
import com.partnet.automation.jbehave.WebDriverHtmlOutputWithImg;


/**
 * Provides an instance of {@link Configuration}.
 * 
 * @author <a href="mailto:rbascom@part.net">rbascom</a>
 */
public final class ConfigurationProducer
{
  static final Logger LOG = LoggerFactory.getLogger(ConfigurationProducer.class);
  
  @Inject
  private RuntimeConfiguration runConfig;
  
  @Produces
  @Singleton
  @WeldConfiguration
  public Configuration getConfiguration()
  {
    LOG.info("ConfigurationProducer.getConfiguration()");
    
    Keywords keywords = new LocalizedKeywords();
    
    ParameterConverters converters = new ParameterConverters().addConverters(
         new DateConverter(new SimpleDateFormat("yyyy-MM-dd")),
         new StoryParameterEnumConverter()
    );
    
    return new MostUsefulConfiguration()
        .useStoryControls(
            new StoryControls()
              .doDryRun(runConfig.doDryRun())
              .doSkipScenariosAfterFailure(false))
              
        .useStepPatternParser(new RegexPrefixCapturingPatternParser())
        .useStoryLoader(new LoadFromClasspath(this.getClass().getClassLoader()))
        
        .useKeywords(keywords)
        .useStepCollector(new MarkUnmatchedStepsAsPending(keywords))
        .useStoryParser(
            new RegexStoryParser(keywords, 
                new ExamplesTableFactory(keywords, new LoadFromClasspath(this.getClass()), converters)))
        .useDefaultStoryReporter(new ConsoleOutput(keywords))
        
        .useStoryReporterBuilder(
            new StoryReporterBuilder()
                .withFormats(Format.CONSOLE, Format.TXT, Format.STATS,
                    WebDriverHtmlOutputWithImg.WEB_DRIVER_HTML_WITH_IMG)
                .withFailureTrace(true)
                .withReporters(new LoggingStoryReporter())
                .withKeywords(keywords)
                )
        .useParameterConverters(converters)
        ;
  }
}
