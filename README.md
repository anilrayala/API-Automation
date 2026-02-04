# Sample Feature - Keywords Summary

## Summary
Brief descriptions of common Gherkin keywords used in `*.feature` files.

- **Author**: anil@gmail.com
- **Feature**: List of scenarios.
- **Scenario**: Business rule through list of steps with arguments.
- **Given**: Precondition to be met before executing the main steps.
- **When**: Action or event that triggers the scenario.
- **Then**: Expected outcome or result after the action.
- **And**: Additional steps that can be used with Given, When, Then.
- **But**: Steps that indicate exceptions or contrasts to the previous steps.
- **Scenario Outline**: Template for scenarios that can be run multiple times with different data.
- **Examples**: Data table for Scenario Outline to provide different sets of inputs.
- **Background**: Steps that are common to all scenarios in the feature file and run before each scenario.
- **Doc String**: Use triple double-quotes (`"""`) to define multi-line strings or text blocks.
- **Data Table**: Use pipe-delimited rows (`|`) to define tabular data for steps.
- **Tag**: Use `@tag` to categorize or label scenarios for filtering or grouping.
- **Angle Brackets**: Use `<placeholder>` to denote placeholders in Scenario Outline steps.
- **Quotes**: Use `"` or `'` to define string literals in steps.
- **Comment**: Use `#`, `--`, or `##` to add comments within the feature file.

---

## Maven Commands to Run Tests

```bash
mvn test -Dcucumber.filter.tags=@AddPlace
mvn test
mvn clean test
mvn test verify
mvn test verify -Dcucumber.filter.tags=@AddPlace
```

---

## Cucumber Options

- cucumber.filter.tags
- cucumber.features
- cucumber.glue

---

## Cucumber Reports

- Cucumber generates reports inside the **target** folder.
- HTML and JSON reports can be found at:

```text
target/cucumber-reports/
```

Example:

```text
target/cucumber-reports/cucumber.json
```

---

## Jenkins Setup

### Start Jenkins

```bash
D:\Jenkins>java -jar jenkins.war
```

If error appears:

```text
Running with Java 23 from C:\Program Files\Java\jdk-23, which is not fully supported.
Run the command again with the --enable-future-java flag to bypass this error.
Supported Java versions are: [17, 21, 25]
See https://jenkins.io/redirect/java-support/ for more information.
```

Run:

```bash
D:\Jenkins>java -jar jenkins.war --enable-future-java
```

---

### Get Initial Admin Password

```text
C:\Users\anil\.jenkins\secrets\initialAdminPassword
```

Example:

```text
724931c76fe542e98d0456e065eb81c5
```

Open:

```text
http://localhost:8080/
```

Login:

```text
username: anil
password: anil****
```

---

### Install Cucumber Reports Plugin

- Manage Jenkins
- Manage Plugins
- Available
- Search: cucumber reports
- Install without restart

---

## Create Jenkins Job (Maven Project)

- Jenkins Dashboard → New Item
- Enter Item Name
- Select Maven project
- Click OK

---

### Source Code Management

- Select Git
- Provide repository URL

OR

```text
file:///D:/path/to/repo
```

Advanced → Use custom workspace → Provide local repo path

---

### Build Configuration

Under Build:

- Invoke top-level Maven targets

Goals and options:

```text
clean test
```

---

### Post-Build Actions

- Publish Cucumber reports

JSON path:

```text
target\cucumber-reports\cucumber.json
```

Save job.

---

### Run Job

- Click Build Now
- Click build number
- Click Cucumber reports

---

## Configure Tags in Jenkins Maven Job

Append in Goals and options:

```text
-Dcucumber.filter.tags=@"$tag"
```

Enable:

- This job is parameterized

Add Parameter:

- Choice Parameter

Name:

```text
tag
```

Choices:

```text
@AddPlace
@DeletePlace
@UpdatePlace
```

Save job.

---

### Build With Parameters

- Click Build with Parameters
- Select tag
- Run build

Selected tag filters scenarios.

---

## End of README
