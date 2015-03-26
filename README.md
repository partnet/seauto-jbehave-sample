SeAuto JBehave Sample
======

Introduction
------
This project is intended to be a template project for those wanting to quickly
implement and use SeAuto for JBehave.

## Setup instructions


#### Prerequisites: 
* Download and install the [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/) 
* Download and install [Apache Maven](http://maven.apache.org/download.cgi) 
  * Any version on that page will work if you have a preference
* Firefox, for the demo to run

#### Run:

This repository and the SeAuto repo need to be cloned.
SeAuto will need to run a `mvn clean install` in the home directory to install it's package into your local repo

#### To build the archetype:
````bash
mvn archetype:create-from-project
cd target/genrated-sources/archetype
mvn install
cd /to/new/project/dir
mvn archetype:generate -DarchetypeCatalog=local
````
Then follow the instructions inserting your GroupId, etc..

#### To run this project or the built archetype
To run:

`mvn clean integration-test`
You should see a firefox browser launch, go to Bing.com and search for Partnet, verifying Partnet is in the search results

After the test runs, HTML reports are availble via `target/jbehave/view/index.html`, with screenshots for failed tests directly in the reports.

## Documentation
Please see the [documentation](http://mercury.part.net/WebContent/#/getStarted)