import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** Produces payslips and organization-wide payroll summaries. (FR3) */
public class ReportGenerator {

    private final EmployeeManager employeeManager;
    private final PayrollService payrollService;

    public ReportGenerator(EmployeeManager employeeManager, PayrollService payrollService) {
        this.employeeManager = employeeManager;
        this.payrollService = payrollService;
    }

    public String generatePayslip(String employeeId, double hoursWorked) {
        Employee employee = employeeManager.getEmployee(employeeId);
        if (employee == null) {
            throw new NoSuchElementException("No employee found with ID " + employeeId);
        }
        double gross = payrollService.calculateGrossPay(hoursWorked, employee.getHourlyRate());
        double tax = payrollService.calculateTax(gross);
        double net = gross - tax;

        return String.format(
                "Payslip%n" +
                "Employee: %s (%s)%n" +
                "Department: %s%n" +
                "Hours worked: %.2f%n" +
                "Gross pay: %.2f%n" +
                "Tax: %.2f%n" +
                "Net pay: %.2f",
                employee.getName(), employee.getId(), employee.getDepartment(),
                hoursWorked, gross, tax, net);
    }

    public String generateSummaryReport(Map<String, Double> hoursWorkedByEmployeeId) {
        List<Employee> employees = employeeManager.listEmployees();
        StringBuilder sb = new StringBuilder("Organization Payroll Summary\n");
        double totalNet = 0;
        for (Employee employee : employees) {
            double hours = hoursWorkedByEmployeeId.getOrDefault(employee.getId(), 0.0);
            double gross = payrollService.calculateGrossPay(hours, employee.getHourlyRate());
            double net = gross - payrollService.calculateTax(gross);
            totalNet += net;
            sb.append(String.format("%s - %s: %.2f hours, net pay %.2f%n",
                    employee.getId(), employee.getName(), hours, net));
        }
        sb.append(String.format("Total net payout: %.2f", totalNet));
        return sb.toString();
    }
}
