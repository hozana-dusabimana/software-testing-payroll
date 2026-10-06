# How to Run

This is a plain console Java project — no server, no browser, no build tool required beyond the JDK.

## Prerequisites

- JDK installed and on PATH (`java -version` / `javac -version`). Built and tested with JDK 24.

## 1. Compile

From the `Homework` folder:

```bash
cd "F:\modules\auca\software testing techniques\Homework"
javac -d out src/main/*.java src/test/*.java
```

This compiles everything into an `out/` directory (safe to delete and regenerate any time).

## 2. Run the demo

```bash
java -cp out Main
```

Prints a sample payslip for one employee and an organization-wide payroll summary.

## 3. Run the test suite

```bash
java -cp out TestRunner
```

Runs the black-box (Equivalence Partitioning, Boundary Value Analysis, Decision Table) and
white-box (branch/path coverage) suites, printing one Pass/Fail line per test case plus a
summary. Expected result: 33/35 black-box cases pass, 7/8 white-box cases pass, 10/10 branches
covered — the 3 failures all trace to the same known defect in
`PayrollService.calculateTaxRate()` (see `README.md`).

## 4. Run the web demo (for presenting how the requirements were tested)

Quickest way: from the `Homework` folder run `.\run.bat` (PowerShell or cmd). It compiles everything,
starts the server and opens http://localhost:8080/ in your browser. Manual steps:

```bash
javac -d out src/main/*.java src/test/*.java src/web/*.java
java -cp out WebServer
```

Then open http://localhost:8080/ (pass another port as an argument, e.g. `java -cp out WebServer 9000`).
Run it from the `Homework` folder so the `web/` pages are found. No extra libraries are needed.

| Page | Purpose |
|---|---|
| index.html | Dashboard: workforce stats and quick actions |
| employees.html | Add / edit / remove / search employees (FR1) |
| payroll.html | Pay calculator: gross, overtime, tax bracket, net (FR2) |
| reports.html | Payslip and organization payroll summary (FR3) |
| qa.html | QA Console for the demo: runs the real black-box / white-box suites, requirements traceability, validation checks and payment test cases, filterable by requirement |

## Running from an IDE (e.g. VS Code)

With the Java extension installed, open `src/main/Main.java` or `src/test/TestRunner.java` and
use the "Run" code lens above `public static void main(...)` instead of the terminal commands.
