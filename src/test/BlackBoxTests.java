import java.util.ArrayList;
import java.util.List;

/**
 * Black-box (functional) tests derived only from the functional requirements
 * and the published specification of the payroll/employee API - no knowledge
 * of the internal source code is used to design these cases.
 *
 * Techniques applied: Equivalence Partitioning (EP), Boundary Value Analysis
 * (BVA) and Decision Table testing, as covered in Chapter 2.1 of the course.
 */
public class BlackBoxTests {

    private final List<TestResult> results = new ArrayList<>();

    public List<TestResult> run() {
        equivalencePartitioning_hoursWorked();
        equivalencePartitioning_taxBrackets();
        boundaryValueAnalysis_taxBrackets();
        boundaryValueAnalysis_hourlyRate();
        decisionTable_netPay();
        equivalencePartitioning_employeeManagement();
        equivalencePartitioning_errorPresentation();
        equivalencePartitioning_listingValidationAndReports();
        return results;
    }

    private void check(String id, String description, String input, String expected, String actual, boolean passed) {
        results.add(new TestResult(id, description, input, expected, actual, passed));
    }

    // ---- EP: hoursWorked classes -> {invalid negative, valid regular (0-40), valid overtime (>40)} ----
    private void equivalencePartitioning_hoursWorked() {
        PayrollService payroll = new PayrollService();

        // BB-EP-01: valid regular-hours class
        double gross1 = payroll.calculateGrossPay(20, 10.0);
        check("BB-EP-01", "Gross pay, regular-hours class (0-40h)", "hours=20, rate=10",
                "200.00", String.format("%.2f", gross1), gross1 == 200.0);

        // BB-EP-02: valid overtime-hours class
        double gross2 = payroll.calculateGrossPay(45, 10.0);
        // 40*10 + 5*10*1.5 = 400 + 75 = 475
        check("BB-EP-02", "Gross pay, overtime-hours class (>40h)", "hours=45, rate=10",
                "475.00", String.format("%.2f", gross2), gross2 == 475.0);

        // BB-EP-03: invalid negative-hours class
        String msg = ErrorPresenter.present(() -> payroll.calculateGrossPay(-5, 10.0));
        check("BB-EP-03", "Gross pay, invalid negative-hours class", "hours=-5, rate=10",
                "Hours worked cannot be negative", String.valueOf(msg),
                "Hours worked cannot be negative".equals(msg));
    }

    // ---- EP: tax bracket classes taken directly from the specification ----
    private void equivalencePartitioning_taxBrackets() {
        PayrollService payroll = new PayrollService();

        double r1 = payroll.calculateTaxRate(500);   // class: 0-1000
        check("BB-EP-04", "Tax rate, low-income class (<=1000)", "gross=500",
                "0.10", String.valueOf(r1), r1 == 0.10);

        double r2 = payroll.calculateTaxRate(2000);  // class: 1000-3000
        check("BB-EP-05", "Tax rate, mid-income class (1000-3000)", "gross=2000",
                "0.20", String.valueOf(r2), r2 == 0.20);

        double r3 = payroll.calculateTaxRate(5000);  // class: >3000
        check("BB-EP-06", "Tax rate, high-income class (>3000)", "gross=5000",
                "0.30", String.valueOf(r3), r3 == 0.30);
    }

    // ---- BVA: boundaries of the tax brackets: 1000 and 3000 ----
    private void boundaryValueAnalysis_taxBrackets() {
        PayrollService payroll = new PayrollService();

        double a = payroll.calculateTaxRate(999.99);
        check("BB-BVA-01", "Tax rate just below lower boundary", "gross=999.99",
                "0.10", String.valueOf(a), a == 0.10);

        double b = payroll.calculateTaxRate(1000.00);
        check("BB-BVA-02", "Tax rate at lower boundary", "gross=1000.00",
                "0.10", String.valueOf(b), b == 0.10);

        double c = payroll.calculateTaxRate(1000.01);
        check("BB-BVA-03", "Tax rate just above lower boundary", "gross=1000.01",
                "0.20", String.valueOf(c), c == 0.20);

        double d = payroll.calculateTaxRate(2999.99);
        check("BB-BVA-04", "Tax rate just below upper boundary", "gross=2999.99",
                "0.20", String.valueOf(d), d == 0.20);

        double e = payroll.calculateTaxRate(3000.00);
        // Per specification "1000 < gross <= 3000 -> 20%" this must be 0.20.
        check("BB-BVA-05", "Tax rate at upper boundary", "gross=3000.00",
                "0.20", String.valueOf(e), e == 0.20);

        double f = payroll.calculateTaxRate(3000.01);
        check("BB-BVA-06", "Tax rate just above upper boundary", "gross=3000.01",
                "0.30", String.valueOf(f), f == 0.30);
    }

    // ---- BVA: hourlyRate boundary (must be >= 0) ----
    private void boundaryValueAnalysis_hourlyRate() {
        PayrollService payroll = new PayrollService();

        double zero = payroll.calculateGrossPay(10, 0.0);
        check("BB-BVA-07", "Gross pay at hourly-rate boundary", "hours=10, rate=0.00",
                "0.00", String.format("%.2f", zero), zero == 0.0);

        String msg = ErrorPresenter.present(() -> payroll.calculateGrossPay(10, -0.01));
        check("BB-BVA-08", "Gross pay just below hourly-rate boundary", "hours=10, rate=-0.01",
                "Hourly rate cannot be negative", String.valueOf(msg),
                "Hourly rate cannot be negative".equals(msg));
    }

    // ---- Decision table: hours class x tax bracket ----
    private void decisionTable_netPay() {
        PayrollService payroll = new PayrollService();

        // Row 1: regular hours, low bracket
        double n1 = payroll.calculateNetPay(20, 40.0); // gross=800 -> 10%
        check("BB-DT-01", "Net pay: regular hours / low bracket", "hours=20, rate=40",
                "720.00", String.format("%.2f", n1), n1 == 720.0);

        // Row 2: regular hours, mid bracket
        double n2 = payroll.calculateNetPay(40, 50.0); // gross=2000 -> 20%
        check("BB-DT-02", "Net pay: regular hours / mid bracket", "hours=40, rate=50",
                "1600.00", String.format("%.2f", n2), n2 == 1600.0);

        // Row 3: overtime hours, high bracket
        double n3 = payroll.calculateNetPay(60, 80.0); // gross=40*80+20*80*1.5=3200+2400=5600 -> 30%
        check("BB-DT-03", "Net pay: overtime hours / high bracket", "hours=60, rate=80",
                "3920.00", String.format("%.2f", n3), n3 == 3920.0);

        // Row 4: regular hours, upper tax boundary (gross exactly 3000)
        double n4 = payroll.calculateNetPay(37.5, 80.0); // gross=3000 -> 20% per spec -> net=2400
        check("BB-DT-04", "Net pay: regular hours / upper tax boundary (gross=3000)", "hours=37.5, rate=80",
                "2400.00", String.format("%.2f", n4), n4 == 2400.0);
    }

    // ---- EP / decision table for employee management ----
    private void equivalencePartitioning_employeeManagement() {
        EmployeeManager manager = new EmployeeManager();
        manager.addEmployee(new Employee("E100", "Test User", "QA", 25.0));
        check("BB-EP-07", "Add employee with unique valid ID", "id=E100",
                "1 employee stored", manager.count() + " employee(s) stored", manager.count() == 1);

        String dupMsg = ErrorPresenter.present(() -> manager.addEmployee(new Employee("E100", "Duplicate", "QA", 25.0)));
        check("BB-EP-08", "Add employee with duplicate ID", "id=E100 (again)",
                "An employee with ID E100 already exists", String.valueOf(dupMsg),
                "An employee with ID E100 already exists".equals(dupMsg));

        manager.updateEmployee("E100", "Updated Name", "Finance", 30.0);
        Employee updated = manager.getEmployee("E100");
        boolean updateOk = "Updated Name".equals(updated.getName()) && updated.getHourlyRate() == 30.0;
        check("BB-EP-09", "Update existing employee", "id=E100",
                "name=Updated Name, rate=30.00", updated.getName() + ", rate=" + updated.getHourlyRate(), updateOk);

        String updMsg = ErrorPresenter.present(() -> manager.updateEmployee("E999", "X", "Y", 1.0));
        check("BB-EP-10", "Update non-existent employee", "id=E999",
                "No employee found with ID E999", String.valueOf(updMsg),
                "No employee found with ID E999".equals(updMsg));

        manager.removeEmployee("E100");
        boolean removeOk = manager.getEmployee("E100") == null;
        check("BB-EP-11", "Remove existing employee", "id=E100",
                "employee removed", removeOk ? "employee removed" : "employee still present", removeOk);

        String rmMsg = ErrorPresenter.present(() -> manager.removeEmployee("E999"));
        check("BB-EP-12", "Remove non-existent employee", "id=E999",
                "No employee found with ID E999", String.valueOf(rmMsg),
                "No employee found with ID E999".equals(rmMsg));
    }

    // ---- EP: ErrorPresenter classes -> {operation fails, operation succeeds} (FR2.5) ----
    private void equivalencePartitioning_errorPresentation() {
        PayrollService payroll = new PayrollService();

        // BB-EP-13: failing-operation class - user sees the readable message, not the exception type/stack trace
        String failureMessage = ErrorPresenter.present(() -> payroll.calculateGrossPay(-5, 10.0));
        check("BB-EP-13", "Error presentation, failing-operation class (FR2.5)", "hours=-5, rate=10",
                "Hours worked cannot be negative", String.valueOf(failureMessage),
                "Hours worked cannot be negative".equals(failureMessage));

        // BB-EP-14: succeeding-operation class - no message is produced
        String successMessage = ErrorPresenter.present(() -> payroll.calculateGrossPay(20, 10.0));
        check("BB-EP-14", "Error presentation, succeeding-operation class (FR2.5)", "hours=20, rate=10",
                "No error message shown", successMessage == null ? "No error message shown" : successMessage,
                successMessage == null);
    }

    // ---- EP: listing (FR1.7), add/update validation (FR1.2, FR1.4), reports (FR3) ----
    private void equivalencePartitioning_listingValidationAndReports() {
        EmployeeManager manager = new EmployeeManager();
        PayrollService payroll = new PayrollService();
        ReportGenerator reports = new ReportGenerator(manager, payroll);
        manager.addEmployee(new Employee("E200", "Alice Test", "Finance", 20.0));
        manager.addEmployee(new Employee("E201", "Bob Test", "IT", 35.0));

        // BB-EP-15: list returns every stored employee
        List<Employee> listed = manager.listEmployees();
        boolean listOk = listed.size() == 2 && "E200".equals(listed.get(0).getId()) && "E201".equals(listed.get(1).getId());
        check("BB-EP-15", "List all employees", "2 employees stored",
                "E200, E201", listed.size() + " listed", listOk);

        // BB-EP-16: empty ID class
        String emptyIdMsg = ErrorPresenter.present(() -> manager.addEmployee(new Employee("", "No Id", "IT", 10.0)));
        check("BB-EP-16", "Add employee with empty ID", "id=(empty)",
                "Employee id cannot be empty", String.valueOf(emptyIdMsg),
                "Employee id cannot be empty".equals(emptyIdMsg));

        // BB-EP-17: negative hourly rate on add
        String negRateMsg = ErrorPresenter.present(() -> manager.addEmployee(new Employee("E300", "Neg Rate", "IT", -3.0)));
        check("BB-EP-17", "Add employee with negative hourly rate", "rate=-3",
                "Hourly rate cannot be negative", String.valueOf(negRateMsg),
                "Hourly rate cannot be negative".equals(negRateMsg));

        // BB-EP-18: negative hourly rate on update
        String updRateMsg = ErrorPresenter.present(() -> manager.updateEmployee("E200", "Alice Test", "Finance", -1.0));
        check("BB-EP-18", "Update employee with negative hourly rate", "id=E200, rate=-1",
                "Hourly rate cannot be negative", String.valueOf(updRateMsg),
                "Hourly rate cannot be negative".equals(updRateMsg));

        // BB-EP-19: payslip for an existing employee (45h x 20 -> gross 950, tax 10% = 95, net 855)
        String slip = reports.generatePayslip("E200", 45);
        boolean slipOk = slip.contains("Alice Test") && slip.contains("Gross pay: " + String.format("%.2f", 950.0))
                && slip.contains("Net pay: " + String.format("%.2f", 855.0));
        check("BB-EP-19", "Payslip for existing employee", "id=E200, hours=45",
                "gross 950.00, net 855.00", slipOk ? "gross 950.00, net 855.00" : "payslip values differ", slipOk);

        // BB-EP-20: payslip for unknown employee
        String slipUnknownMsg = ErrorPresenter.present(() -> reports.generatePayslip("E999", 10));
        check("BB-EP-20", "Payslip for unknown employee", "id=E999",
                "No employee found with ID E999", String.valueOf(slipUnknownMsg),
                "No employee found with ID E999".equals(slipUnknownMsg));

        // BB-EP-21: payslip with negative hours
        String slipHoursMsg = ErrorPresenter.present(() -> reports.generatePayslip("E200", -5));
        check("BB-EP-21", "Payslip with negative hours", "id=E200, hours=-5",
                "Hours worked cannot be negative", String.valueOf(slipHoursMsg),
                "Hours worked cannot be negative".equals(slipHoursMsg));

        // BB-EP-22: organization summary (E200: 40h x 20 = 800 -> net 720; E201: 40h x 35 = 1400 -> net 1120; total 1840)
        java.util.Map<String, Double> hours = new java.util.LinkedHashMap<>();
        hours.put("E200", 40.0);
        hours.put("E201", 40.0);
        String summary = reports.generateSummaryReport(hours);
        boolean summaryOk = summary.contains("E200") && summary.contains("E201")
                && summary.contains("Total net payout: " + String.format("%.2f", 1840.0));
        check("BB-EP-22", "Organization payroll summary", "E200 40h, E201 40h",
                "both employees, total net 1840.00", summaryOk ? "both employees, total net 1840.00" : "summary values differ", summaryOk);

        // BB-EP-23: rejected operations leave the data unchanged
        int before = manager.count();
        ErrorPresenter.present(() -> manager.addEmployee(new Employee("E200", "Duplicate", "IT", 10.0)));
        ErrorPresenter.present(() -> manager.removeEmployee("E999"));
        boolean unchanged = manager.count() == before && "Alice Test".equals(manager.getEmployee("E200").getName());
        check("BB-EP-23", "Rejected operations leave data unchanged", "duplicate add + unknown remove",
                "count=" + before + ", E200 unchanged", "count=" + manager.count() + (unchanged ? ", E200 unchanged" : ", data changed"), unchanged);
    }
}
