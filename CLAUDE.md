# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A Java 8 + Maven CLI tool that compares the results of two SQL queries (or a SQL query vs a CSV / expected row count) across heterogeneous databases. Tests are driven by **XML definitions, not Java code** — adding a test does not require recompilation. Execution is wired through TestNG, so every XML file becomes a test instance in the TestNG report.

## Build, run, package

```
mvn clean package
```
Produces `target/DBTestCompare-<version>-jar-with-dependencies.jar` via `maven-shade-plugin`. The shade config hard-codes a `Class-Path` entry pointing at `jdbc_drivers/*` and an `Add-Opens: java.base/java.lang` entry (for JAXB on JDK 9+) in the jar manifest — JDBC drivers are **not** bundled. Place driver jars in `jdbc_drivers/` next to the assembled jar.

Shade (not the assembly plugin) is used because the application bundles `log4j-core` plus `log4j-1.2-api`, which both ship a binary `META-INF/.../Log4j2Plugins.dat`. Shade's `Log4j2PluginCacheFileTransformer` merges those caches; the assembly plugin would overwrite one with the other, leaving log4j 2 without its XML configuration parser and silently falling back to a default appender (which then breaks `Printer.init`'s lookup of the `stdout` appender).

Run (after `cd target` or wherever the jar lives):
```
java -jar DBTestCompare-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Common system property overrides (see [RunTests.java:44-47](src/main/java/uk/co/objectivity/test/db/RunTests.java#L44-L47)):
- `-DtestsDir=path` — directory scanned for tests (default `test-definitions`)
- `-DteamcityLogsEnabled=true` — emit `##teamcity` service messages
- `-DfilterInclude=a.b,g.z.f` / `-DfilterExclude=a.b.test` — comma-separated include/exclude lists, matched against the dotted path derived from test directory layout

`mvn test` is **not** the way to run tests in this repo — there are no JUnit/TestNG classes to exercise. Tests run only via the assembled jar's `main` (or the ANT `testdbq.run` target below).

### ANT-driven build + execute (used by CI)

`deploy/build.xml` is the canonical end-to-end pipeline. It calls Maven, copies test definitions and JDBC drivers into `target/`, performs token replacement, then runs the jar:
```
ant -noinput -buildfile ./deploy/build.xml -Dconfig=DEV02 compile tokens.copy.files testdbq.run
```
The `-Dconfig` value selects a properties file from [deploy/Config/](deploy/Config/) (e.g. `DEV01`, `DEV02`) that fills `${...}` tokens in connection XML at copy time. SQL/XML token replacement of `@datefrom@` / `@dateto@` also happens here ([deploy/build.xml:50-57](deploy/build.xml#L50-L57)).

### Local databases

`docker/docker-compose.yml` spins up SQL Server, MySQL, and Postgres preloaded with `dbquality` schemas (init SQL under `docker/my-*/sql-scripts`). Credentials match `deploy/Config/DEV02`.

## Architecture

### Entry point & test discovery flow

1. [RunTests.main](src/main/java/uk/co/objectivity/test/db/RunTests.java) reads `<testsDir>/cmpSqlResults-config.xml` (the **single global config** — datasources, thread count, filter, logger, exit code on failure) via JAXB into `CmpSqlResultsConfig`.
2. [DataSource.init](src/main/java/uk/co/objectivity/test/db/utils/DataSource.java) builds a c3p0 `ComboPooledDataSource` per `<datasource>` entry and registers a JVM shutdown hook to close pools. Datasources are looked up by `name` for the lifetime of the run.
3. `RunTests.runTestNG` programmatically builds a TestNG suite with `DBTestCompare.class` as the only test class, parallel mode `INSTANCES`, thread count from config.
4. [TestDataProvider.getTestsConfiguration](src/main/java/uk/co/objectivity/test/db/TestDataProvider.java) (the TestNG `@DataProvider`) recursively walks `test-definitions/`, producing one `TestParams` per `*.xml` it finds. **The directory path becomes the dotted test name** (`fetch_compare_test.inline_sql_SQL_SERVER_PostgreSQL`), which is what `filterInclude`/`filterExclude` match against.
5. Each XML is JAXB-unmarshalled into `CmpSqlResultsTest`. Validation in `validateTestConfig` enforces the per-mode SQL count (MINUS/FETCH need 2 sqls, FILE/KEY/NMB_OF_RESULTS need 1) and that FILE/KEY supply `<file filename="...">` / `keyColumns`. Failures are stored as a skip message on `TestParams` rather than throwing — the test still runs and reports the misconfiguration.
6. [DBTestCompare.testSQLResults](src/main/java/uk/co/objectivity/test/db/DBTestCompare.java) is the single `@Test`. `@Factory` creates one instance per `TestParams`. It implements `ITest` so each instance reports under its own dotted name in the TestNG report.

### Comparator strategy

The `<compare mode="...">` attribute in a test XML drives the whole comparison. [CompareMode](src/main/java/uk/co/objectivity/test/db/beans/CompareMode.java) is a fat enum that maps each mode to a concrete `Comparator` subclass:

| Mode | Comparator | What it does |
|------|------------|--------------|
| `MINUS` | `MinusComparator` | Wraps the two SQLs in a `MINUS`/`EXCEPT` query — fastest, single-engine |
| `FETCH` | `FetchComparator` | Streams both result sets row-by-row using JDBC `fetchSize` — needed for cross-engine comparison of huge data sets |
| `FILE` | `FileComparator` | Compares query output against a CSV |
| `KEY` | `KeyComparator` | Compares against CSV using a key column (also emits an Excel report) |
| `NMB_OF_RESULTS` | `NmbOfResultsComparator` | Runs query, asserts row count via `<assert>` conditions |

All comparators extend [Comparator](src/main/java/uk/co/objectivity/test/db/comparators/Comparator.java) and return a `TestResults` (an integer `nmbOfRows` of differences plus text output). The TestNG assertion in `DBTestCompare` is uniformly `assertEquals(testResults.getNmbOfRows(), 0)` — a comparator signals failure by returning a non-zero diff count, never by throwing.

### XML bean layer

The JAXB beans live in [src/main/java/uk/co/objectivity/test/db/beans/xml/](src/main/java/uk/co/objectivity/test/db/beans/xml/). There is **no XSD** — schema rules are enforced by the imperative validation in `TestDataProvider.validateTestConfig`. When adding a new XML attribute, update both the bean and that validator.

`Sql` elements may contain inline SQL **or** a `filename` attribute pointing to a sibling `.sql` file (mutually exclusive, enforced in `validateAndPrepareSql`). After validation, the file contents are inlined into the bean, so downstream comparators never see the file/inline distinction.

### Aggregators & transformers

Two pluggable column-level mechanisms applied during row comparison:
- [aggregators/](src/main/java/uk/co/objectivity/test/db/aggregators/) — replace a column value with an aggregate (e.g. `SumIntegersAggregator`, `DateAggregator`) gated by a `condition/` (Equals / NotEquals)
- [transformers/](src/main/java/uk/co/objectivity/test/db/transformers/) — normalize values before compare (e.g. `RemoveLeadingZerosTransformer`, `SwapTransformer`)

Adding a new aggregator/transformer requires extending the type enum (`AggregatorType` / `TransformerType`) — that enum is the factory, mirroring the `CompareMode` pattern.

### Logging & reporting

- log4j 1.2 API on top of log4j2-core (the `log4j-1.2-api` bridge dep). Code still uses `org.apache.log4j.Logger`. Native config lives in [src/main/resources/log4j2.xml](src/main/resources/log4j2.xml) — the appender name `stdout` referenced from [Printer.init](src/main/java/uk/co/objectivity/test/db/utils/Printer.java) must match the `<Console name="stdout">` element there.
- TestNG HTML report lands in `test-output/`.
- `Printer` accumulates per-test text tables (Sirocco) and Excel output (POI) for KEY mode.
- `TCMessages` formats `##teamcity[...]` service messages when `teamcityLogsEnabled=true`.

## CI

[.github/workflows/github-actions.yml](.github/workflows/github-actions.yml) and [azure-pipelines.yml](azure-pipelines.yml) both:
1. Pull the latest git tag → `dBTestCompareVersion`
2. `setDBTestCompareVersion.ps1` rewrites the version in `pom.xml`
3. `downloadBackupAndJDBCDrivers.ps1` pulls licensed JDBC drivers + a SQL Server backup
4. `docker-compose up -d` boots local DBs
5. `restoreBackup.ps1` restores the backup
6. `ant ... compile tokens.copy.files testdbq.run` builds and runs all tests
7. Publish JUnit XML, upload artifacts, on tag push attach zip to GitHub release

## Conventions worth knowing

- **Java 8 source/target** (`<source>1.8</source>`). Don't introduce Java 9+ syntax.
- The package root `uk.co.objectivity.test.db` reflects the project's pre-Accenture origin (Objectivity). Keep it.
- Test names are **derived from the filesystem**. Renaming or moving a `test-definitions/` subdirectory silently changes the dotted name used by `filterInclude`/`filterExclude` — update any CI invocations that pin those filters.
- A misconfigured XML test is **reported as a failed test**, not silently skipped — see `validateTestConfig` populating `skipTestMessage`, which `testSQLResults` throws as a `TestException`.
- `cmpSqlResults-config.xml` is **per testsDir**, not per test — there is no per-test override of datasources or thread count.
