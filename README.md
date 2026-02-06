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

### Welcome to GraphiQL
- https://rahulshettyacademy.com/gq/graphql
- ! means it is a mandatory field


- GraphiQL is an in-browser tool for writing, validating, and
- testing GraphQL queries.

- Type queries into this side of the screen, and you will see intelligent
- typeaheads aware of the current GraphQL type schema and live syntax and
- validation errors highlighted within the text.

- GraphQL queries typically start with a "{" character. Lines that start
- with a # are ignored.

- An example GraphQL query might look like:

```text    
{
     field(arg: "value") {
       subField
     }
}
```

- Keyboard shortcuts:

-  Prettify Query:  Shift-Ctrl-P (or press the prettify button above)

-     Merge Query:  Shift-Ctrl-M (or press the merge button above)

-       Run Query:  Ctrl-Enter (or press the play button above)

-   Auto Complete:  Ctrl-Space (or just start typing)

## End of README

Introduction to Rest Api's and where it is used in project architecture
real time usage of api's in industry and examples
understanding of GET, POST, PUT, DELETE HTTP CRUD operations of api's
what are path, query parameters and headers in rest api
Postman tool for testing api's and how to use it
how to create a collection in postman and organize api's
setting up maven project for api testing with dependencies
validate status codes
Assetions on json response body and headers through automation code
parsing json response using JsonPath and validating values
integrating multiple api with each other and validating the flow with common json response data
building end to end automation using POST, GET, PUT, DELETE api's together
importance of junit and testng in api automation and how to use them
understanding structure of complex nested json response and its array notations how to validate it
retrieving the json array size and its elements using jsonpath and validating it
iterating over every element of json array and validating it
retrieving jsnon nodes on condition logic using jsonpath and validating it
real time example to solve business logic through json response

### Handling dynamic json payloads with parameterization and data driven testing using testng data provider
- why dynamic json payloads are important to understand
- sending parameters to payload from test
- understanding testng data provider for parameterization
- example of parametarization of api tests with multiple data sets
- how to handle static json payloads

basic authentication and token based authentication in api's
how to send files as attachment in post api calls
handling oAuth2.0 authentication in api's and how to generate access tokens for client credentials and authorization code grant types

serialization and deserialization of json response to pojo classes
- what is serialization and deserialization in api testing
- how to create pojo classes for json response
- using libraries like jackson or gson for serialization and deserialization
- example of deserializing json response to pojo and validating values
- example of serializing pojo to json payload and sending it in api request
- advantages of using pojo classes for api testing and how it improves code readability and maintainability
- handling nested json response with pojo classes and validating complex data structures
- best practices for creating pojo classes and maintaining them in api automation projects
- real time example of using serialization and deserialization in api testing to solve business logic and validate end to end flows
- common challenges faced during serialization and deserialization and how to overcome them in api automation projects

Request and response specifications in Rest Assured for reusable api test code
- what are request and response specifications in Rest Assured
- how to create request specifications for common request configurations like base URI, headers, authentication, etc
- how to create response specifications for common response validations like status code, content type, etc
- example of using request and response specifications in api tests to reduce code duplication and improve maintainability
- best practices for organizing request and response specifications in api automation projects

cucumber and jenkins integration for api test automation

GraphQL api testing and how it differs from REST api testing
- what is GraphQL and how it differs from REST
- how to send GraphQL queries and mutations in api tests
- validating GraphQL responses and handling errors in GraphQL api testing
- example of testing a GraphQL api with Rest Assured and validating the response data
- best practices for testing GraphQL apis and how to handle complex queries and mutations in api automation projects
- query variables

git basics

using excel for testData and reading the values into tests using HashMap and converting to json

core java basics





