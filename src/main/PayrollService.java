/**
 * Computes gross pay, tax and net pay for an employee. (FR2)
 *
 * Specification given to the developer:
 *  - Regular hours are the first 40 hours in the period, paid at the hourly rate.
 *  - Hours beyond 40 are overtime, paid at 1.5x the hourly rate.
 *  - Tax brackets on gross pay:
 *      gross <= 1000            -> 10%
 *      1000 <  gross <= 3000    -> 20%
 *      gross >  3000            -> 30%
 */
public class PayrollService {

    private static final double REGULAR_HOURS_LIMIT = 40.0;
    private static final double OVERTIME_MULTIPLIER = 1.5;

    public double calculateGrossPay(double hoursWorked, double hourlyRate) {
        if (hoursWorked < 0) {
            throw new IllegalArgumentException("Hours worked cannot be negative");
        }
        if (hourlyRate < 0) {
            throw new IllegalArgumentException("Hourly rate cannot be negative");
        }
        double regularHours = Math.min(hoursWorked, REGULAR_HOURS_LIMIT);
        double overtimeHours = hoursWorked > REGULAR_HOURS_LIMIT ? hoursWorked - REGULAR_HOURS_LIMIT : 0.0;
        return regularHours * hourlyRate + overtimeHours * hourlyRate * OVERTIME_MULTIPLIER;
    }

    public double calculateTaxRate(double grossPay) {
        if (grossPay <= 1000.0) {
            return 0.10;
        } else if (grossPay < 3000.0) {
            return 0.20;
        } else {
            return 0.30;
        }
    }

    public double calculateTax(double grossPay) {
        return grossPay * calculateTaxRate(grossPay);
    }

    public double calculateNetPay(double hoursWorked, double hourlyRate) {
        double gross = calculateGrossPay(hoursWorked, hourlyRate);
        return gross - calculateTax(gross);
    }
}
