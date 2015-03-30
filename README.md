SeAuto JBehave Sample
=====================

# Introduction

This project is intended to be a template project for those wanting to quickly
implement and use SeAuto for JBehave.

# Setup instructions

## Prerequisites: 
* Download and install the [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads/) 
* Download and install [Apache Maven](http://maven.apache.org/download.cgi) 
  * Any version on that page will work if you have a preference
* Firefox, for the demo to run

## Get Started
Run this single command to use a hosted template (a.k.a. archetype) of this project:
```bash
mvn archetype:generate -Dfilter=seauto
```
Select the framework you would like to use, and you are on your way! This will use one of the pre-built hosted templates to get you started with SeAuto that tailors the project to your organization's needs.

You may want to download the various Driver Binaries for your project. To do this, run this command inside of your newly created SeAuto project directory:
```bash
mvn com.partnet:seauto-driver-manager:download
```

## To run the SeAuto JBehave project
To run, execute this command inside of your SeAuto project directory:

`mvn clean integration-test`
You should see a Firefox browser launch, go to Bing.com, and search for Partnet, verifying Partnet is in the search results.

After the test runs, HTML reports are available via `target/jbehave/view/index.html`, with screen shots for failed tests directly in the reports.

# Documentation
Please see the main SeAuto [documentation](//partnet.github.io/seauto/#/getStarted) for further information.

## To build the archetype from scratch:
````bash
mvn archetype:create-from-project
cd target/generated-sources/archetype
mvn install
cd /to/new/project/dir
mvn archetype:generate -DarchetypeCatalog=local
````
Then follow the instructions inserting your GroupId, etc..
