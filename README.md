# Employee Management and Payroll System

A small Java console application (plus a simple web front end) for managing
employees, processing payments and producing reports, together with
black-box and white-box test suites.

- `src/main/` - Employee, EmployeeManager, PayrollService, ReportGenerator, Main
- `src/test/` - black-box tests (equivalence partitioning, boundary values,
  decision table) and white-box tests (statement/branch/path coverage)
- `src/web/` and `web/` - web server and browser front end

## Prerequisites

JDK on PATH (`java -version` / `javac -version`). Built and tested with JDK 24.
No other libraries or build tools are needed.

## 1. Compile

From the project folder:

```bash
javac -d out src/main/*.java src/test/*.java
```

This compiles everything into `out/`, which is safe to delete and regenerate.

## 2. Run the demo

```bash
java -cp out Main
```

Prints a sample payslip for one employee and an organization-wide payroll summary.

## 3. Run the test suites

```bash
java -cp out TestRunner
```

Runs the black-box and white-box suites and prints one Pass/Fail line per test
case plus a summary. Expected: 33/35 black-box and 7/8 white-box cases pass,
10/10 branches covered. All three failures come from one defect:
`PayrollService.calculateTaxRate()` taxes a gross pay of exactly 3000 at 30%
instead of 20% (`grossPay < 3000` should be `grossPay <= 3000`).

## 4. Run the web demo

Quickest way: run `.\run.bat` from the project folder. It compiles everything,
starts the server and opens http://localhost:8080/. Manually:

```bash
javac -d out src/main/*.java src/test/*.java src/web/*.java
java -cp out WebServer
```

Pass another port as an argument if needed, e.g. `java -cp out WebServer 9000`.
Run it from the project folder so the `web/` pages are found.

| Page | Purpose |
|---|---|
| index.html | Dashboard: workforce stats and quick actions |
| employees.html | Add / edit / remove / search employees (FR1) |
| payroll.html | Pay calculator: gross, overtime, tax bracket, net (FR2) |
| reports.html | Payslip and organization payroll summary (FR3) |
| qa.html | QA console: runs the black-box / white-box suites, requirements traceability and validation checks |

## Running from an IDE

In VS Code with the Java extension, open `src/main/Main.java` or
`src/test/TestRunner.java` and use the "Run" code lens above `main`.
