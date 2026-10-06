# Software Testing Techniques - Homework

## What was wrong with the original assignment file

`Homework.docx` (in the parent folder) references "the table below" twice but
contains no table, image, or embedded object anywhere in the file (confirmed
by inspecting its XML: zero `<w:tbl>` elements and no media). The scenario
also gave no concrete system to test, so before any test cases could be
produced, a matching implementation had to be built first.

## What this folder contains

- `src/main/` - a small Java 24 Employee Management & Payroll system
  (Employee, EmployeeManager, PayrollService, ReportGenerator, Main),
  implementing the functional requirements identified from the scenario.
- `src/test/` - a hand-rolled black-box test suite (Equivalence Partitioning,
  Boundary Value Analysis, Decision Table) and white-box test suite
  (statement/branch/path coverage), run via `TestRunner`.
- `generate_report.js` - builds `Test-Case-Report.docx` from the actual test
  results (run `node generate_report.js` after `npm install docx` to
  regenerate it).
- `Test-Case-Report.docx` - the finished report: functional requirements,
  implementation summary, and the black-box/white-box test-case tables
  (the "table below" the original homework was missing), following the
  structure of `test-case-report-template.doc`.

## Build & run

```bash
javac -d out src/main/*.java src/test/*.java
java -cp out Main          # demo
java -cp out TestRunner    # runs both test suites, prints Pass/Fail per case
```

## Result

33/35 black-box cases pass and 7/8 white-box cases pass. The 3 failures all
trace to one defect: `PayrollService.calculateTaxRate()` taxes a gross pay of
exactly 3000 at 30% instead of the specified 20% (off-by-one boundary:
`grossPay < 3000` should be `grossPay <= 3000`). See Section 7 of
`Test-Case-Report.docx` for details.
