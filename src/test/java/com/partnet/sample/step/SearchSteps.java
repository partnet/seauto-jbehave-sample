/* Copyright (c) 2014 Partnet, Inc. Confidential and Proprietary. */

package com.partnet.sample.step;


import java.util.Map;

import javax.inject.Inject;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.annotations.weld.WeldStep;
import org.junit.Assert;

import com.partnet.sample.config.framework.StoryContext;
import com.partnet.sample.page.home.HomePage;
import com.partnet.sample.page.search.SearchResultsPage;

/**
 * @author <a href="mailto:bbarker@part.net">bbarker</a>
 */

@WeldStep
public class SearchSteps
{
  
  @Inject
  private StoryContext context;
  
  @Given("I am on Bing's home page")
  public void givenIAmOnBingsHomePage()
  {
    context.site().open();
  }
  
  @When("I search for $searchPhrase")
  public void whenIsearchForPhrase(String searchPhrase)
  {
    context.getPage(HomePage.class)
      .setSearchPhrase(searchPhrase)
      .clickSearch();
  }
  
  @Then("I will see Partnet's home page in the list of results")
  public void thenIWillSeePartnetInResults()
  {
    Map<String, String> majorSearchResults = context.getPage(SearchResultsPage.class).getMajorSearchResults();
    
    String partnet = "Partnet";
    
    Assert.assertTrue(String.format("Major search result links in did not contain '%s'!", partnet), 
        majorSearchResults.containsKey(partnet));
  }
}
