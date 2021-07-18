# Employee Scheduling System

A Java command-line tool for scheduling Employee shifts, based on the [OptaPlanner](https://www.optaplanner.org/) constraint solving engine.

## 1. Technical Setup

[Link to Setup instructions](TECHNICAL.md)

## 2. About this project

I thought of making this project since I could not find any free solutions which satisfied my needs perfectly in scheduling employees into shifts.

Particularly, I wanted a tool which:
* Is free
* Is very simple/minimalistic
* Allows uploading and downloading of data and allocations in easily machine-readable formats, such as CSV (preferably not Excel, definitely not PDF, and definitely not manually copying the information)
* Allows automatic updates of the allocations when availabilities of employees change (ideally employees would request this in a website and require no interaction from the manager)
* Also allows tweaking of the allocations via a GUI by the manager

The current system I have developed, critically, allows inputting employee information/employee availability and constraints/shift data in CSV files, and outputs allocations in a CSV file (the main use-case). This is a valuable feature for my purposes, since I can then write scripts to add employees to our systems, instead of copying changes over manually. Employee availability data can be easily obtained from services such as Microsoft Forms without performing manual work per employee. This is an advantage over employee scheduling systems such as [`ABC Roster`](https://www.abc-roster.com/) and [`Sling`](https://getsling.com/) because they don't (as far as I can tell, I might be wrong) offer functionality to import employee availabilities/constraints via a file upload.

The system takes into account that employees cannot take overlapping shifts, shifts outside their availability, shifts of a different type to that which they've selected in their availabilities (e.g. if they selected to work online, they shouldn't be required to work in-person), or go above their maximum number of shifts per week. It also tries to satisfy all employees requested minimum number of shifts per week, and tries to avoid large gaps between shifts on the same day for a particular employee (so they aren't waiting for a long time for their next shift). It takes into account that those employees with more experience should be prioritized for shifts.

The system is designed primarily for situations involving casual employees, where both the maximum allowable and preferred minimum number of shifts per week are likely to be low (i.e. there is no hard requirement to give shifts to any particular employee).

In a complex example such as that in [`example 7`](CSVs_command_line/example7_complex_example) the system will keep trying to find a better solution, until it has spent 15 seconds trying to find a better allocation without success, at which point it will give up. In this complex example, of trying to allocate 28 employees across 58 shifts, a brute-force approach would examine 28<sup>58</sup> possibilities - so a brute-force approach of examining all possibilities is not feasible. However, for the aforementioned example, the system is typically able to achieve an apparently optimal result in 15 seconds, and then gives up after another 15 seconds of not improving the outcome. The time spent before giving up can be configured in [`employeesSchedulingSolverConfig.xml`](src/main/resources/employeesSchedulingSolverConfig.xml).

## 3. Project RoadMap

I plan to develop:
* More tests, and polished up commenting
* Add more detailed feedback to user when something goes wrong with the input data
* Further constraints and functionality for adding incremental modifications to allocations when employees change their availability
    * These constraints need to prioritize making the fewest possible changes to the timetable
    * Ideally, employees would be automatically emailed requesting they approve suggested changes to their allocation, or request no changes if the system suggests a new allocation layout.
* A full website (using [`Spring Boot`](https://spring.io/projects/spring-boot), [`Apache Isis`](http://isis.apache.org/)), Amazon Web Services (EC2, Elastic Beanstalk with load balancing, SES, Lambda, RDS), Docker, applicable Javascript libraries, reCAPTCHA, and a PostgreSQL database (Amazon RDS), allowing:
    * Setup of an account
    * Minimalistic uploading of CSV data (I'll probably use [dropzone.js](https://www.dropzonejs.com/))
    * Uploading/downloading of data and allocations
    * Requesting to run the scheduler (will use AWS Lambda)
    * Tracking allocations across schedules for different projects by a manager
    * Persistence of prior allocations in a database (PostgreSQL Amazon RDS), allowing reverting of changes
    * Tweaking allocations and data via the GUI
    * Employees to request changes to their availabilities, and emailing employees who can take the shift/swap requesting they accept a proposed change
        * I am likely to use [`Amazon SES`](https://aws.amazon.com/ses/pricing/) over options such as [`Mailgun API`](https://documentation.mailgun.com/en/latest/api_reference.html) or [`SMTP.com`](https://smtp.com) due to its significantly cheaper pricing.
        * An alternative option may be using [`Microsoft Graph API`](https://docs.microsoft.com/en-us/graph/outlook-mail-concept-overview)
        * Email verification can be performed using services such as [`Email List Verify`](https://www.emaillistverify.com/) or [`Bouncer`](https://www.usebouncer.com/). `Mailgun` has this feature built in but charges more per email. A free alternative may be [`Reacher`](https://help.reacher.email/self-host-guide) but may have licencing restrictions.
    * An Android/iPhone app allowing employees to manage their availability, and managers to manage shifts

Possible features could include:
* A public REST API
    * Would charge for the service, to cover costs
    * JSON REST API with endpoints to:
        * Upload employee information, employee availabilities, and shift information
        * Upload modifications to the above information, and request rerunning of the scheduler to take into account modifications
        * Request a rerun of the scheduler for a particular length of time
        * Downloading of all data
* SMS notifications
    * Could use [`Twilio`](https://www.twilio.com/sms) API
    * Would come with a paid version of the service, to cover costs
    * Would cover all notifications implemented by email in the free version
    * Could allow 2FA

Possible integrations could include:
* Slack/Monday.com - a plugin to create a schedule, using the backend scheduler. Would allow running of commands, including to upload data/create a schedule from all employees in the workspace or a list of employees, and to download this schedule in a CSV file
* Workday/MYOB/Xero - integration with payroll&workforce management services, so employees can immediately be onboarded and processed upon a schedule being created and accepted, and so that if shifts are dropped and then rearranged by the system this is reflected

## 4. CS Theory/Programming skills demonstrated in the Project

* OOP design principles
* Testing - automated [`JUnit unit testing`](https://junit.org/junit5/), `property based testing`, [`quickcheck`](https://github.com/pholser/junit-quickcheck) (randomized property based testing)
* `Test-driven development` (I could have improved here, I didn't follow it rigorously as my design changed frequently as I am new to [`OptaPlanner`](https://www.optaplanner.org/))
* Data Structures & Algorithms - consider the methods `Employee.addAvailabilities` and `Employee.availabilitiesFullyCoveringShiftTime` in [`Employee.java`](src/main/java/com/roster123/employeescheduler/domain/Employee.java)
* `Constraint programming`
* Java
* Gradle
* Data encapsulation - all fields (except public static) are private. Collections containing mutable elements are not returned (use reflection to test some private fields as needed for Optaplanner). If a list of immutable elements is returned, it is an unmodifiable list. As much as practical (whilst working in Optaplanner and opencsv), mutable values are not returned.

## 5. Format of CSV Files

[Link to CSV Format Explanation](CSV_FORMAT.md)
