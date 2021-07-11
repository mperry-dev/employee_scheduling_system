# CSV Format Explanation

The examples on this page are from the folder [`CSVs_command_line/example5_simpler_working_example`](CSVs_command_line/example5_simpler_working_example).

## 1. employee_information.csv

This file represents on each line, the information about an employee, excluding availability. Particularly, it includes their id, name, mobile, email, importance, maximum weekly shifts, and minimum weekly shifts.

Example:

```
employeeId,name,mobile,email,importance,maxWeeklyShifts,minWeeklyShifts
employee1234567,Homer,+61411999700,Homer@fakeemail7632456745321345678654321345.com,13,5,1
employee1234580,Krusty,+61411999713,Krusty@fakeemail7632456745321345678654321345.com,5,,
```

The fields are as follows:
* `employeeId`: the unique business identifier of the employee. This field is required, and must be unique, otherwise the system will reject the CSV file and terminate.
* `name`: the name of the employee. It is optional to include this.
* `mobile`: the mobile number of the employee. This must follow international format, including country code, e.g. `+61481500997`. The system will reject the input CSV file and terminate if it contains an invalid mobile number. It is optional to include this.
* `email`: the email of the employee. If this is not a valid email format, the system will reject the CSV file and terminate. It is optional to include this.
* `importance`: the importance of the employee. Including this is optional. A higher importance will result in the employee being prioritized for shifts, whilst not including it will result in the employee having 0 importance.
* `maxWeeklyShifts`: the maximum number of weekly shifts the employee is allowed to have. Including this is optional. If this is not included, the employee will have no upper bound on the number of weekly shifts allowed. If included, adhering to this maximum is a hard constraint of the system.
* `minWeeklyShifts`: the minimum number of weekly shifts the employee prefers to have. Including this is optional. If this is not included, the employee will be assigned a minimum weekly shifts preference of 0 shifts. Adhering to this minimum is not a hard constraint of the system - the system will try to satisfy the preferred minimum weekly shifts of all employees, but it is not required to.

## 2. employee_availability.csv

This file represents on each line, a single instance of employee availability. Each employee can have multiple lines in this file, or none if they have no availability. It includes employee id, the type of the availability, and the start and end times.

Example:

```
employeeId,type,start,end
employee1234567,online,12/07/2021 9:00,19/07/2021 9:00
employee1234580,in-person,12/07/2021 8:00,19/07/2021 9:00
```

The fields are as follows:
* `employeeId`: this should match an employeeId in the file `employee_information.csv`. If it does not, the availability will not be considered by the system. This field is required - if it is not included, the system will reject the input CSV file and terminate.
* `type`: this is the shift type the employee is willing to do during this availability, such as an online shift or in-person shift. This is used to match against the type field of a shift from `shift_information.csv`. This is optional - if not included, the employee will be considered for all types of shift by the system. If it is included, adhering to the type requirement of the employee is a hard constraint of the system.
* `start`: this is the start time of the availability, as a date and time combination. It must be in the pattern `d/MM/yyyy H:mm` (i.e. `day/month/year hour:minute`), with 24-hour time, such as `30/01/2020 22:40`. This is a required field - if it is not included, the system will reject the input CSV file and terminate.
* `end`: this is the end time of the availability, as a date and time combination. It must be in the pattern `d/MM/yyyy H:mm` (i.e. `day/month/year hour:minute`), with 24-hour time, such as `30/01/2020 22:40`. This is a required field - if it is not included, the system will reject the input CSV file and terminate.

If the start time is not strictly before the end time, the system will reject the CSV file and terminate.

## 3. shift_information.csv

This file represents on each line, information about a single shift being offered. It includes shift id, the type of shift, and the start and end times of the shift.

Example:

```
shiftId,type,start,end
shift123,online,12/07/2021 10:00,12/07/2021 11:00
shift124,in-person,12/07/2021 8:05,12/07/2021 9:00
shift125,in-person,12/07/2021 9:05,12/07/2021 10:00
```

The fields are as follows:
* `shiftId`: the unique business identifier of the shift. This field is required, and must be unique, otherwise the system will reject the CSV file and terminate.
* `type`: this is the type of the shift, such as an online shift or in-person shift. This field is required - if it is not included, the system will reject the CSV file and terminate.
* `start`: this is the start time of the shift, as a date and time combination. It must be in the pattern `d/MM/yyyy H:mm` (i.e. `day/month/year hour:minute`), with 24-hour time, such as `30/01/2020 22:40`. This is a required field - if it is not included, the system will reject the input CSV file and terminate.
* `end`: this is the end time of the shift, as a date and time combination. It must be in the pattern `d/MM/yyyy H:mm` (i.e. `day/month/year hour:minute`), with 24-hour time, such as `30/01/2020 22:40`. This is a required field - if it is not included, the system will reject the input CSV file and terminate.

If the start time is not strictly before the end time, the system will reject the CSV file and terminate.

## 4. ALLOCATIONS_OUTPUT.csv

This is the output CSV format of the system. It contains the shift id, the employee id, the start and end times, and the type of the shift. It's essence is to represent the employee performing the shift, and all the information about the shift loaded from `shift_information.csv`.

Each line will represent a single shift (uniquely identified by `shiftId`). All shifts will be output in this file if the program succeeds.

Example:

```
shiftId,employeeId,start,end,type
shift123,employee1234567,12/07/2021 10:00,12/07/2021 11:00,online
shift124,employee1234580,12/07/2021 8:05,12/07/2021 9:00,in-person
shift125,employee1234580,12/07/2021 9:05,12/07/2021 10:00,in-person
```

The fields are as follows:
* `shiftId`: the unique business identifier of the shift, as taken from `shift_information.csv`.
* `employeeId`: the unique business identifier of the employee assigned to the shift, as taken from `employee_information.csv`.
* `start`: the start time of this shift, as taken from `shift_information.csv`.
* `end`: the end time of this shift, as taken from `shift_information.csv`.
* `type`: the type of the shift, as taken from `shift_information.csv`.

## 5. A note on DateTime format

The format of `d/MM/yyyy H:mm` requires the DateTimes in the format `day/month/year hour:minute`. DateTime days input into the system can be represented as 1 or 2 digits, months as 1 or 2 digits, years as 1 to 4 digits, hours as 1 or 2 digits, and minutes as 1 or 2 digits.

When outputting, if applicable (i.e. `days 1-9` or `hours 0-9`), the day or hour values will be 1 digit. The months and minutes will always be output as 2 digits, and the year will always be output as 4 digits.