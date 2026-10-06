import java.util.LinkedHashMap;
import java.util.Map;

/** Demo entry point wiring the employee/payroll/reporting modules together. */
public class Main {
    public static void main(String[] args) {
        EmployeeManager employeeManager = new EmployeeManager();
        PayrollService payrollService = new PayrollService();
        ReportGenerator reportGenerator = new ReportGenerator(employeeManager, payrollService);

        employeeManager.addEmployee(new Employee("E001", "Aline Uwase", "Finance", 20.0));
        employeeManager.addEmployee(new Employee("E002", "Jean Mugisha", "IT", 35.0));

        System.out.println(reportGenerator.generatePayslip("E001", 45));
        System.out.println();

        Map<String, Double> hours = new LinkedHashMap<>();
        hours.put("E001", 45.0);
        hours.put("E002", 40.0);
        System.out.println(reportGenerator.generateSummaryReport(hours));

        System.out.println();
        System.out.println("Invalid operations (caught and shown as friendly messages, not stack traces):");
        safe("Add duplicate employee", () ->
                employeeManager.addEmployee(new Employee("E001", "Someone Else", "IT", 15.0)));
        safe("Update unknown employee", () ->
                employeeManager.updateEmployee("E999", "Ghost", "Nowhere", 10.0));
        safe("Remove unknown employee", () ->
                employeeManager.removeEmployee("E999"));
        safe("Payslip for unknown employee", () ->
                System.out.println(reportGenerator.generatePayslip("E999", 10)));
        safe("Payslip with negative hours", () ->
                System.out.println(reportGenerator.generatePayslip("E001", -5)));
        safe("Payslip with negative hourly rate", () -> {
            employeeManager.addEmployee(new Employee("E003", "Bad Rate", "IT", -1.0));
            System.out.println(reportGenerator.generatePayslip("E003", 10));
        });
    }

    /** Runs a user-facing action and prints ErrorPresenter's message if it fails. */
    private static void safe(String action, Runnable operation) {
        String message = ErrorPresenter.present(operation);
        if (message != null) {
            System.out.println("  - " + action + ": " + message);
        }
    }
}
