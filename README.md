# Employee Management and Payroll System

A small Java console application (plus a simple web front end) for managing
employees, processing payments and producing reports, together with
black-box and white-box test suites.

- `src/main/` - Employee, EmployeeManager, PayrollService, ReportGenerator, Main
- `src/test/` - black-box tests (equivalence partitioning, boundary values,
  decision table) and white-box tests (statement/branch/path coverage)
- `web/` - browser front end

## Build and run

```bash
javac -d out src/main/*.java src/test/*.java
java -cp out Main          # demo
java -cp out TestRunner    # runs both test suites
```

## Result

33/35 black-box and 7/8 white-box cases pass. All three failures come from one
defect: `PayrollService.calculateTaxRate()` taxes a gross pay of exactly 3000
at 30% instead of 20% (`grossPay < 3000` should be `grossPay <= 3000`).
