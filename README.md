# Code Review
## Feedback
First of all, I would suggest to use the SonarLint plugin which is going to highlight many issues that are present in the code such as the assignation of values for static fields in the constructor, close resources, useless assignments to variables and so on.

Moreover this is a typical spaghetti code case where is complex and difficult to follow the sequence of the program making it very hard to maintain it. To solve that is recommended to break up the code into smaller pieces using pure functions (always return the same result given the same input).

Additionally there are some lines of code that can be reusable in order to comply with the DRY (Don Repeat Yourself) principle

# Refactoring Section
## TODOs
- Completing documentation with Javadoc since only the public and most important apis are documented
- Making unit tests for the DatabaseHandler class
- (Optional) Although the test cases used (integration tests) are enough to demonstrate the expected behavior of the API a few more can be written (including unit tests)
