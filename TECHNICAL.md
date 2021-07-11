# Setup Instructions

## 1. Installation of Dependencies

### 1.1. Installing Java 11

Navigate to https://www.oracle.com/java/technologies/javase-jdk11-downloads.html in your browser, and download and install the appropriate version of Java.

For Windows, you may need to add additional **PATH** variables for `javac` and `java`. To do this, use the commands

`where javac`

`where java`

Then add the output to a System Environment PATH variable. To do this, search `Environment Variables` in Cortana, select `Edit the System Environment Variables`, and follow [these instructions to add to the PATH variable](https://java.com/en/download/help/path.html).

You should now be able to run the `java` and `javac` commands in your terminal, to run Java Class files and compile Java files respectively.

### 1.2. Installing Gradle 7.1.1

Download the zip file from (download should start automatically): https://gradle.org/next-steps/?version=7.1.1&format=bin

You should follow the installation instructions provided:

https://gradle.org/install/#manually

**Linux**

For Linux users, note that you may have to edit the `~/.bashrc` file to permanently change the PATH variable by appending the line:

`export PATH=$PATH:/opt/gradle/gradle-7.1.1/bin`

After modifying the `~/.bashrc` file, you would also need to close all terminals and then make a new terminal (`~/.bashrc` runs when you start a terminal). Note here that `/opt/gradle` is the directory you chose to install gradle in, as specified in the [manual gradle install instructions](https://gradle.org/install/#manually) above.

**Mac**

For Mac users, you *may* need to add `/opt/gradle/gradle-7.1.1/bin` to your *PATH* environment variable by appending to `/etc/paths` instead of `~/.bashrc`, as described in this article: https://www.architectryan.com/2012/10/02/add-to-the-path-on-mac-os-x-mountain-lion/

**Windows**

For Windows users, you will probably need to add `/opt/gradle/gradle-7.1.1/bin` to your environment variables, as described in the above [manual gradle install instructions](https://gradle.org/install/#manually).

## 2. Running/Testing the project

### 2.1. Running the project

Navigate to the root directory of the project (containing the `build.gradle` file), and run in a terminal (replacing `/mnt/c/Users/12345/Desktop/folder_of_CSVs` with your preferred folder of CSV files):

`gradle run --args="/mnt/c/Users/12345/Desktop/folder_of_CSVs"`

Alternatively, run an example using one of:

* `gradle runExample1`
* `gradle runExample2`
* `gradle runExample3`
* `gradle runExample4`
* `gradle runExample5`
* `gradle runExample6`
* `gradle runExample7`

### 2.2. Testing the project with Jacoco

Navigate to the root directory of the project (containing the `build.gradle` file), and run in a terminal:

`gradle test`

The coverage checking report will be in the file: *build/reports/jacoco/test/html/index.html*

The test report will be in the file: *build/reports/tests/test/index.html*

You can open the above reports in Chrome/Firefox, and browse the report.

### 2.3. Testing the project with Clover

If you previously made a Clover coverage report, delete the `build` folder if you wish the report to cover all classes.

Navigate to the root directory of the project (containing the `build.gradle` file), and run in a terminal:

`gradle cloverGenerateReport -b test_clover.gradle`

The coverage checking report will be in the file: *build/reports/clover/html/index.html*

A summary coverage report in PDF form will be in the file: *build/reports/clover/clover.pdf*

The test report will be in the file: *build/reports/tests/test/index.html*

You can open the above reports in Chrome/Firefox, and browse the report.

NOTE: Jacoco doesn't show coverage in statements throwing exceptions, as per [the Jacoco FAQ](https://www.eclemma.org/jacoco/trunk/doc/faq.html). Clover is a useful alternative to Jacoco since it doesn't have this issue, and also has statement coverage instead of line coverage. However, the latest version of Java supported by Clover is Java 9 (we simply tell Clover that it is Java 9 code instead of the Java 11 code that it is).

### 2.3. Getting dependencies into the `lib` folder

Navigate to the root directory of the project (containing the `build.gradle` file), and run in a terminal:

`gradle getDeps`

This step isn't necessary to run or test the project via the command line - it is useful for some IDEs such as `VSCode`, to be able to run the project from within the IDE.

### 2.4 Configuring Time Spent Finding a Solution

In the file [`employeesSchedulingSolverConfig.xml`](src/main/resources/employeesSchedulingSolverConfig.xml), change the number in the tag `<unimprovedSecondsSpentLimit>10</unimprovedSecondsSpentLimit>` to the number of seconds you wish the constraint solver to keep looking for a solution whilst it hasn't made any improvements to the score.

Alternatively, when running the method `employeeSchedulingSystem.allocateShifts`, input a non-null value to instead configure the number of seconds in total to spend looking for a solution (replacing the configuration in [`employeesSchedulingSolverConfig.xml`](src/main/resources/employeesSchedulingSolverConfig.xml)).

The more time spent, the more likely it is to find a good solution!

### 2.5 Current State of Tests

Please note that all of the currently failing tests are examples of emails/international mobile numbers I am not sure are/aren't valid. I will investigate these cases further.

It is best to examine coverage levels using `Clover`, because `Jacoco` often doesn't detect coverage on lines with exceptions.

Currently the level of coverage is fairly high at 90.4% statement coverage (the exception to this is some methods in the [`EmployeeSchedulingSystem`](src/employeeschedulingsystem/EmployeeSchedulingSystem.java) class, such as to load a file from an absolute path). However, there are still edge-cases left to write `JUnit` tests for, such as for different combinations of user-input errors being made simultaneously. More testing is needed for complex methods such as `Employee.addAvailabilities` and `Employee.availabilitiesFullyCoveringShiftTime` in the [`Employee`](src/employeeschedulingsystem/Employee.java) class, and for the constraints in [`AllocationsConstraintProvider`](src/employeeschedulingsystem/AllocationsConstraintProvider.java). I will work on fixing this when I have time (I am currently quite busy).