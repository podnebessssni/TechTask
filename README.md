# QA automation exercise

Java 17 test project for SauceDemo UI scenarios and the Restful Booker booking API. UI tests use Selenide, API tests use REST Assured, and JUnit 5 runs both.

## Prerequisites

For local execution, install Java 17+, Maven 3.9+, and Chrome or Chromium. The test targets are public services, so the machine needs network access to them.

For Docker execution, Docker Engine with the `docker` CLI is sufficient; Java, Maven, and Chromium are installed in the image. Docker needs network access during the build to download packages and Maven dependencies, and when running to reach the two public test systems.

## Local execution

Run UI tests:

```bash
mvn test -Dgroups=ui
```

Run API tests:

```bash
mvn test -Dgroups=api
```

Run all tests:

```bash
mvn test
```

The build writes JUnit XML and text reports to `target/surefire-reports`. To generate an HTML summary after running the tests:

```bash
mvn test org.apache.maven.plugins:maven-surefire-report-plugin:3.5.2:report
```

Open `target/site/surefire-report.html` to view the summary.

The UI defaults to headless Chrome and `https://www.saucedemo.com`. Override settings with `-DbaseUrl=https://...` and `-Dheadless=false`. The API base URL defaults to `https://restful-booker.herokuapp.com` and can be changed with `-DapiBaseUrl=https://...`.

## Docker execution

Build an image containing Java 17, Maven, Chromium, and ChromeDriver, then run all tests:

```bash
docker build -t qa-automation-tests .
docker run --rm qa-automation-tests
```

Run only one test group in Docker:

```bash
docker run --rm qa-automation-tests mvn -B test -Dgroups=ui
docker run --rm qa-automation-tests mvn -B test -Dgroups=api
```

## Test approach

- **Additional scenarios:** An unknown booking ID should return HTTP 404. Invalid login is checked with two parameterized username/password combinations.

- **UI locators:** SauceDemo's `data-test` attributes are used for controls and inventory entries because they are purpose-built and stable. The cart link is also located by its `data-test` attribute.
- **Synchronization:** Selenide conditions wait for the Products heading, inventory collection, and cart item. No fixed sleeps are used. Selecting the sort option is followed by reading the ordered names for assertion.
- **Structure:** The UI uses a small page-object layer (`Pages`) to keep locators and actions out of test methods. The API test keeps its request payload in one immutable map, extracts the ID from the create response, then reads and checks the persisted booking.
- **Isolation:** The API test creates its own booking with unique-enough sample data and does not depend on cleanup. UI tests start at the login page and close the browser after each test.
- **Assumptions/limits:** Restful Booker is a public demo service and may be intermittently unavailable or reset data. The create endpoint returns HTTP 200 for a successful booking, as specified by its API. The API test validates all requested fields plus `additionalneeds`; it does not delete the created booking because the public API may require authentication for deletion.
