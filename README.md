## DBTestCompare

**Application to compare results of two SQL queries** 

It reads tests definitions in XML format form specified directory and
than runs them (as TestNG test). If two SQL returns different results -> test fails. Application supports TeamCity
Service Messages (##teamcity messages) so if teamcityLogsEnabled is set to "true" (config file or command line parameter), you will see nice
test tree in TeamCity logs.

To execute tests run program:

```
java -jar DBTestCompare-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Application provides following features:
- Platform independence - possibility to run on Windows and Linux as well (Java Runtime Environment 8 needed)
- Supports all databases with JDBC drivers provided (tested with Microsoft SQL Server, Teradara, PostgreSQL and MySQL\MariaDB)
- Supports all Continuous Integration tools thanks to TestNG Java unit test framework
- Possibility to compare data between two different database engines even for huge data sets 
without "Out of memory problem" thanks to incremental solution
- Possibility to compare data in one database engine in the fastest way using MINUS/EXCEPT Sql operator
- No need to compile program in order to add new tests - thanks to Test Adapter DataDriven mechanism from TestNG 
- Possibility to execute test in parallel by setting Threads parameter in connection file.
- Possibility to build multilevel tests structure
- Possibility to save query results to flat files
- Real time test execution progress in TeamCity
- Possibility to store Queries in separated files or inline in xml test definitions
- Connection pool used for executing tests - connections to databases are not closed 
after each tests, but when all tests are finished
- Possibility to compare query to expected data defined in csv file
- Possibility to compare query to expected number of rows defined in XML test definition
- Possibility to choose exit code in case  of test failure
- Possibility to connect to OLAP - compare mdx queries
- Possibility to defined "delta" precision of comparing floating point numbers
- Possibility to filter tests for execution by including or excluding
- Standard TestNG html test report in "test-output" folder

Program searches for tests definitions by default in folder "test-definitions".
JDBC drivers must be present in "jdbc_drivers" folder.
Licensed database drivers are NOT included, only open source like MySQL, MariaDB and PostgreSQL. Download licensed database drivers 
from the producer of database e.g. Microsoft and put them in "jdbc_drivers" folder (on the same level as *.jar file).

You can override some of the application configuration properties, run app with:

 -DtestsDir=path             -set tests directory (default: test-definitions)
 
 -DteamcityLogsEnabled=true  -log test output in TeamCity format
 
 -DfilterInclude=a.b,g.z.f   -comma separated directories or test files which you want to include
 
 -DfilterExclude=a.b.test    -comma separated directories or test files which you want to exclude

for example:

```
java -DtestsDir=my_tests -jar DBTestCompare-1.0-SNAPSHOT-jar-with-dependencies.jar
```

3'rd party libraries:
Software:
- SIROCCO :: Text Table Formatter
- Apache Log4j
- com.sun.xml.bind :: JAXB Runtime
- com.mchange :: c3p0 - a JDBC Connection pooling / Statement caching library

#### Where to start?
-------------
- See [Getting started](https://github.com/ObjectivityBSS/Link).

Checkout the code or get compiled jar file from [releases page](https://github.com/ObjectivityBSS/Link)

To compile app to runnable fat jar file, run (Maven must be installed first):
```
mvn clean compile assembly:single
```
jar will be created in target directory.

You can manage application by attached ANT (ANT must be installed first) build.xml file (in folder \deploy), script allows to :

-compile app

-replace tokens in connection definition  

-replace tokens in SQL queries

more details [here](https://github.com/ObjectivityBSS/Link)
