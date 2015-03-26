// Copyright (C) 2014 Partnet, Inc. Confidential and Proprietary
package seauto.sample.config.jbehave;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.ScenarioType;
import org.jbehave.core.annotations.weld.WeldConfiguration;
import org.jbehave.core.annotations.weld.WeldStep;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import seauto.sample.config.framework.StoryContext;
import seauto.sample.config.framework.StoryContextProvider;
import seauto.sample.config.selenium.ConfigurableDriverProvider;

import com.partnet.automation.Browser;
import com.partnet.automation.HtmlView;
import com.partnet.automation.annotation.StoryScoped;
import com.partnet.automation.selenium.AbstractConfigurableDriverProvider;
import com.partnet.automation.selenium.DriverProvider;

/**
 * Controls the management of resources that need to be handled before and after all the stories as well as
 * before and after each story. Specifically, the management of the {@link WebDriverProvider} and {@link StoryContextProvider}.
 * 
 * @author <a href="mailto:rbascom@part.net">rbascom</a>
 */
@WeldStep
public class StoryLifecycleListener
{
  private static final Logger LOG = LoggerFactory.getLogger(StoryLifecycleListener.class);

  //If this changes, make sure the pattern in WebDriverHtmlOutputWithImg is changed as well!
  private static final String screenshotPathPattern = "{0}/screenshots/failed-scenario-{1}.png";
  
  private static final String DEBUG_SYSTEM_PROPERTY = "test.config.debug";
  private static Set<String> pastUUIDs = new HashSet<>();
  private final DriverProvider driverProvider;
  private final StoryContextProvider contextProvider;
  
  @Inject
  @StoryScoped
  private StoryContext context;
  
  @Inject
  @WeldConfiguration
  private Configuration configuration;
  
  
  @Inject
  public StoryLifecycleListener(final DriverProvider driverProvider, final StoryContextProvider contextProvider)
  {
    this.driverProvider = driverProvider;
    this.contextProvider = contextProvider;
  }

  /**
   * BeforeStory annotation is used to indicate code that should be executed before each story is run.
   * 
   * It is important to note, that {@link WebDriverProvider#initialize()}, puts the created {@link WebDriver} 
   * on the current thread and must not be called before story execution. JBehave uses a thread pool to execute
   * stories, thus the delay of the initialization until the story has begun execution to ensure the WebDriver
   * is placed on the correct thread. 
   */
  @BeforeStory
  public void beforeStory(@Named("browser")String browserName)
  {
    LOG.debug("beforeStory() on thread {}", Thread.currentThread());
    
    //skip starting browser/context if doing a dry run
    if (!configuration.storyControls().dryRun())
    {
      Browser browser = Browser.valueOfByName(browserName.toUpperCase());
      LOG.debug("Init web driver - {}", browser);
      ((ConfigurableDriverProvider)driverProvider).launch(browser);
      
      LOG.debug("Initialize context");
      this.contextProvider.initialize();
    }
  }
  
  /**
   * AfterStory annotation is used to indicate code that should be executed after each story is run.
   */
  @AfterStory
  public void afterStory()
  {
    LOG.debug("afterStory() on thread {}", Thread.currentThread());

    //skip killing browser/context if doing a dry run
    if (!configuration.storyControls().dryRun()) {
      
      try {
        this.context.clear();
        this.contextProvider.end();
      } catch (NullPointerException e) {
        LOG.error("There was a problem shutting down the context!", e);
      }
      
      if (isDebugModeEnabled() && !getBrowser().isHeadless()) {
        LOG.info("Debug mode is enabled. The browser will not exit until OK is clicked on the java dialog");

        showAlwaysOnTopDialog("Debug Mode", "Debug mode is enabled. Click OK to kill browser");
      }
      
      try {
        this.driverProvider.end();
      } catch (WebDriverException e) {
        LOG.error("There was a problem ending the WebDriver!", e);
      }
    }
    
  }
  
  private void showAlwaysOnTopDialog(String title, String message)
  {
    final JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);

    final JDialog dialog = new JDialog(new JFrame(), title, true);
    dialog.setContentPane(optionPane);

    optionPane.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent e)
      {
        dialog.setVisible(false);
      }
    });

    dialog.pack();
    dialog.setAlwaysOnTop(true);
    dialog.setVisible(true);
    
    dialog.dispose();
  }

  /**
   * Takes a screenshot after a regular scenario
   * @param uuidWrappedFailure
   */
  @AfterScenario(uponType = ScenarioType.NORMAL, uponOutcome = Outcome.FAILURE)
  public void afterNormalScenario(UUIDExceptionWrapper uuidWrappedFailure)
  {
    LOG.debug("afterScenario normal failure on thread {}", Thread.currentThread());
    afterFailure(uuidWrappedFailure);
  }

  /**
   * Takes a screenshot after a scenario run with an Examples table.
   * @param uuidWrappedFailure
   */
  @AfterScenario(uponType = ScenarioType.EXAMPLE, uponOutcome = Outcome.FAILURE)
  public void afterExampleScenario(UUIDExceptionWrapper uuidWrappedFailure)
  {
    LOG.debug("afterScenario example failure on thread {}", Thread.currentThread());
    afterFailure(uuidWrappedFailure);
  }
  
  private void afterFailure(UUIDExceptionWrapper uuidWrappedFailure)
  {
    //be sure it is not a duplicate, and is not a pending step.
    if(uniqueFailure(uuidWrappedFailure) && !(uuidWrappedFailure instanceof PendingStepFound))
    {
      try {
        logDriverInfo();
        takeScreenshot(uuidWrappedFailure);
        saveHtml(uuidWrappedFailure);
      }
      catch (WebDriverException e) {
        LOG.error("There was a problem interacting with the web driver!", e);
      }
      
      //print out stacktrace
      LOG.error("Stacktrace for failure {}", uuidWrappedFailure.getUUID().toString(), uuidWrappedFailure.getCause());
    }
    
    
  }

  @AfterScenario(uponType = ScenarioType.NORMAL, uponOutcome = Outcome.SUCCESS)
  public void afterNormalSuccessSenario() 
  {
    //nothing
  }
  
  @BeforeStories
  public void beforeStories()
  {
    LOG.debug("beforeStories() on thread {}", Thread.currentThread());
  }

  @AfterStories
  public void afterStories()
  {
    LOG.debug("afterStories() on thread {}", Thread.currentThread());
  }

  /**
   * This method is used to log the information about the current WebDriver, such as:
   * <ul>
   *   <li>Cookies</li>
   *   <li>Session ID</li>
   *   <li>URL of the failure</li>
   * </ul>
   */
  private void logDriverInfo()
  {
    
    StringBuilder sb = new StringBuilder();
    for(Cookie cookie : driverProvider.get().manage().getCookies()) {
      sb.append("\n").append(cookie.toString());
    }
    
    LOG.info("Cookies: {}", sb.toString());
    
    String sessionId = null;
    if (driverProvider.get() instanceof RemoteWebDriver) {
      sessionId = ((RemoteWebDriver)driverProvider.get()).getSessionId().toString();
    }
    
    else{
      Browser browser = getBrowser(); 
      switch(browser)
      {
        case CHROME :
          sessionId = ((ChromeDriver)driverProvider.get()).getSessionId().toString();
          break;
        case FIREFOX :
          sessionId = ((FirefoxDriver)driverProvider.get()).getSessionId().toString();
          break;
        case HTMLUNIT :
          sessionId = String.format("N/A for %s", browser);
          break;
        case IE :
          sessionId = ((InternetExplorerDriver)driverProvider.get()).getSessionId().toString();
          break;
        case PHANTOMJS :
          sessionId = ((PhantomJSDriver)driverProvider.get()).getSessionId().toString();
          break;
        default :
          sessionId = String.format("Instructions have not been setup to get session ID for %s", browser);
          break;
        
      }
    }
    
    
    LOG.info("Selenium session ID: {}", sessionId);
    
    LOG.error("URL of failure: '{}'", driverProvider.get().getCurrentUrl());
  }
  
  /**
   * Takes a screenshot using the UUID as the name of the screenshot
   * @param failure
   */
  private void takeScreenshot(UUIDExceptionWrapper failure)
  {
    if (failure != null) {

      //we don't take screen-shots for Pending Steps
      if (failure instanceof PendingStepFound) {
        LOG.debug("Don't take screenshot of a pending step");
        return;
      }
      
      LOG.debug("cause: {} (UUID: {})", failure.getUUID(), failure.getLocalizedMessage());
      String screenshotPath = screenshotPath(failure.getUUID());
      LOG.info("Take screenshot, save to {}", screenshotPath);

      if (getBrowser() == Browser.HTMLUNIT) {
        ((ConfigurableDriverProvider)driverProvider).saveScreenshotForHtmlUnit(screenshotPath, 
            context.site().getBaseUrl(driverProvider.get().getCurrentUrl()));
      }
      else {
        driverProvider.saveScreenshotAs(screenshotPath);
      }

    }
    else
      LOG.debug("UUID: {}, Scenario/story passed", failure);
  }
  
  /**
   * Saves the html for the current failure.
   * @param uuidWrapper
   */
  private void saveHtml(UUIDExceptionWrapper uuidWrapper)
  {
    ((AbstractConfigurableDriverProvider)driverProvider)
      .saveHtml(screenshotPath(uuidWrapper.getUUID())
        .replaceAll("png$", "html"), context.site().getBaseUrl(driverProvider.get().getCurrentUrl()));
  }
  
  private Browser getBrowser()
  {
    return Browser.getBrowser(driverProvider.get());
  }
  
  private String screenshotPath(UUID uuid)
  {
    String sUuid = uuid.toString();

    return MessageFormat.format(screenshotPathPattern, configuration.storyReporterBuilder().outputDirectory(), sUuid);
  }
  
  /**
   * Determines if the given UUID is unique, and keeps a history of the seen UUIDs
   * 
   * @param uuid
   * @return
   */
  private boolean uniqueFailure(UUIDExceptionWrapper uUIDExceptionWrapper)
  {
    
    String sUuid = uUIDExceptionWrapper.getUUID().toString();
    
    if (!pastUUIDs.contains(sUuid)) {
      pastUUIDs.add(sUuid);
      LOG.debug("Failure UUID: {}", sUuid);
      return true;
    }
    
    LOG.debug("This uuid has been seen before! {}", sUuid);
    return false;
  }
  
  private boolean isDebugModeEnabled()
  {
    return Boolean.getBoolean(DEBUG_SYSTEM_PROPERTY);
  }
  
}
