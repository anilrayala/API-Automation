# Sample Feature - Keywords Summary


## Summary
Brief descriptions of common Gherkin keywords used in `*.feature` files.

- **Author**: anil@gmail.com.
- **Feature**: List of scenarios.
- **Scenario**: Business rule through list of steps with arguments.
- **Given**: Precondition to be met before executing the main steps.
- **When**: Action or event that triggers the scenario.
- **Then**: Expected outcome or result after the action.
- **And**: Additional steps that can be used with Given, When, Then.
- **But**: Steps that indicate exceptions or contrasts to the previous steps.
- **Scenario Outline**: Template for scenarios that can be run multiple times with different data.
- **Examples**: Data table for Scenario Outline to provide different sets of inputs.
- **Background**: Steps that are common to all scenarios in the feature file and run before each of the scenarios.
- **Doc String**: Use triple double-quotes (`"""`) to define multi-line strings or text blocks.
- **Data Table**: Use pipe-delimited rows (`|`) to define tabular data for steps.
- **Tag**: Use `@tag` to categorize or label scenarios for filtering or grouping.
- **Angle Brackets**: Use `<placeholder>` to denote placeholders in Scenario Outline steps.
- **Quotes**: Use `"` or `'` to define string literals in steps.
- **Comment**: Use `#`, `--`, or `##` to add comments within the feature file.

### Maven command to run tests:
```mvn test -Dcucumber.filter.tags=@AddPlace```
```mvn test```
```mvn clean test```

### Cucumber options:
```cucumber.filter.tags```
```cucumber.features```
```cucumber.glue```

