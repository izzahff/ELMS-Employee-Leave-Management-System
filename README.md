# ELMS - Employee Leave Management System 

A web-based Employee Leave Management System built for IMNSB, supporting three role-based portals: Employee, Manager, and Admin. Developed as part of a semester project.

## Features

- Role-based login (Employee, Manager, Admin)
- Employee leave application and leave history tracking
- Manager approval workflow for pending leave requests
- Admin management of employees, managers, leave types, and public holidays
- Leave balance tracking (full day and half day)
- PDF generation for leave applications and reports
- Email notifications for leave status updates
- Profile picture upload for employees
- Password reset flow for all roles

## Tech Stack

- **Backend:** Java (Servlets, JSP), layered architecture (Controller, Service, DAO, Model)
- **Database:** Oracle Database
- **Server:** Apache Tomcat 10.1
- **Java Version:** JavaSE-21
- **Frontend:** HTML, CSS, JavaScript

## Project Structure
src/main/java/elms/
├── connection/ # Database connection handling
├── controller/ # Request handling and routing
├── DAO/ # Data access layer (Oracle queries)
├── listener/ # Utility listeners
├── model/ # Entity classes (Employee, Manager, Admin, LeaveApplication, etc.)
├── service/ # Business logic (email notifications, PDF generation)
├── servlet/ # File download and report servlets
└── util/ # Helper utilities

src/main/webapp/
├── Admin/ # Admin portal JSPs
├── Employee/ # Employee portal JSPs
├── Manager/ # Manager portal JSPs
├── css/ # Stylesheets
└── WEB-INF/ # Deployment descriptor and libraries

## Setup Instructions

1. Clone this repository.
2. Import into Eclipse as an existing project (File > Import > Existing Projects into Workspace).
3. Set up an Oracle Database instance (tested with Oracle FREEPDB1 on localhost, port 1521).
4. Open `src/main/java/elms/connection/ConnectionManager.java` and update the following with your own credentials:
```java
   private static final String DB_USER = "YOUR_USERNAME_HERE";
   private static final String DB_PASSWORD = "YOUR_PASSWORD_HERE";
```
5. Run the required SQL scripts to create the schema (add details here if you have a schema file, or note that tables need to be created manually).
6. Deploy the project on Apache Tomcat 10.1 through Eclipse, or export as a WAR file and drop it into Tomcat's `webapps` folder.
7. Access the app at `http://localhost:8080/ELMS_3.0/` and select a role to log in.

## Screenshots

*(Add screenshots here, e.g. the role selection screen, employee dashboard, manager approval view)*

## Author

Izzah Farhanah Faizul
