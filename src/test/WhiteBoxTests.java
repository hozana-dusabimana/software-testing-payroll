import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * White-box (structural) tests. Cases are derived from the internal source
 * code of {@link PayrollService} (statement, branch and path coverage), as
 * covered in Chapter 2.2 of the course, not from the requirements document.
 *
 * PayrollService.calculateGrossPay has 3 decision points (6 branches):
 *   B1 hoursWorked < 0            (true/false)
 *   B2 hourlyRate < 0             (true/false)
 *   B3 hoursWorked > 40           (true/false)
 *
 * PayrollService.calculateTaxRate has 2 decision points (4 branches):
 *   B4 grossPay <= 1000           (true/false)
 *   B5 grossPay < 3000            (true/false)  [as written in the code]
 */
public class WhiteBoxTests {

    private final List<TestResult> results = new ArrayList<>();
    private final Set<String> branchesCovered = new LinkedHashSet<>();
    private static final int TOTAL_BRANCHES = 10; // B1..B5, true+false each

    public List<TestResult> run() {
        statementAndBranchCoverage_calculateGrossPay();
        branchCoverage_calculateTaxRate();
        pathCoverage_taxBoundaryFromCode();
        return results;
    }

    private void check(String id, String description, String input, String expected, String actual, boolean passed) {
        results.add(new TestResult(id, description, input, expected, actual, passed));
    }

    // ---- Statement + branch coverage for calculateGrossPay's 3 decisions ----
    private void statementAndBranchCoverage_calculateGrossPay() {
        PayrollService payroll = new PayrollService();

        // WB-BC-01: B1 = true (hoursWorked < 0)
        String b1TrueMsg = ErrorPresenter.present(() -> payroll.calculateGrossPay(-1, 10));
        boolean b1True = "Hours worked cannot be negative".equals(b1TrueMsg);
        branchesCovered.add("B1-true");
        check("WB-BC-01", "Branch B1 true: hoursWorked < 0", "hours=-1, rate=10",
                "Hours worked cannot be negative", String.valueOf(b1TrueMsg), b1True);

        // WB-BC-02: B1 = false, B2 = true (hourlyRate < 0)
        String b2TrueMsg = ErrorPresenter.present(() -> payroll.calculateGrossPay(10, -1));
        boolean b2True = "Hourly rate cannot be negative".equals(b2TrueMsg);
        branchesCovered.add("B1-false");
        branchesCovered.add("B2-true");
        check("WB-BC-02", "Branch B1 false, B2 true: hourlyRate < 0", "hours=10, rate=-1",
                "Hourly rate cannot be negative", String.valueOf(b2TrueMsg), b2True);

        // WB-BC-03: B1 false, B2 false, B3 = true (hoursWorked > 40)
        double overtime = payroll.calculateGrossPay(45, 10);
        branchesCovered.add("B2-false");
        branchesCovered.add("B3-true");
        check("WB-BC-03", "Branch B1/B2 false, B3 true: hoursWorked > 40", "hours=45, rate=10",
                "475.00", String.format("%.2f", overtime), overtime == 475.0);

        // WB-BC-04: B1 false, B2 false, B3 = false (hoursWorked <= 40)
        double regular = payroll.calculateGrossPay(40, 10);
        branchesCovered.add("B3-false");
        check("WB-BC-04", "Branch B1/B2 false, B3 false: hoursWorked <= 40", "hours=40, rate=10",
                "400.00", String.format("%.2f", regular), regular == 400.0);
    }

    // ---- Branch coverage for calculateTaxRate's 2 decisions ----
    private void branchCoverage_calculateTaxRate() {
        PayrollService payroll = new PayrollService();

        // WB-BC-05: B4 = true (grossPay <= 1000)
        double t1 = payroll.calculateTaxRate(1000);
        branchesCovered.add("B4-true");
        check("WB-BC-05", "Branch B4 true: grossPay <= 1000", "gross=1000",
                "0.10", String.valueOf(t1), t1 == 0.10);

        // WB-BC-06: B4 = false, B5 = true (grossPay < 3000)
        double t2 = payroll.calculateTaxRate(2000);
        branchesCovered.add("B4-false");
        branchesCovered.add("B5-true");
        check("WB-BC-06", "Branch B4 false, B5 true: grossPay < 3000", "gross=2000",
                "0.20", String.valueOf(t2), t2 == 0.20);

        // WB-BC-07: B4 = false, B5 = false (falls to else branch)
        double t3 = payroll.calculateTaxRate(4000);
        branchesCovered.add("B5-false");
        check("WB-BC-07", "Branch B4 false, B5 false: else branch", "gross=4000",
                "0.30", String.valueOf(t3), t3 == 0.30);
    }

    /**
     * Path derived directly from the code's literal condition "grossPay < 3000"
     * rather than from the requirement text "gross <= 3000 -> 20%". Reading the
     * source shows grossPay == 3000.0 makes B5 evaluate to false, sending
     * control down the else branch (30%) - this path test exposes the mismatch
     * between the code path and the specified behavior.
     */
    private void pathCoverage_taxBoundaryFromCode() {
        PayrollService payroll = new PayrollService();

        double atBoundary = payroll.calculateTaxRate(3000.0);
        check("WB-PC-01", "Path: B4 false -> B5 false at literal boundary gross=3000", "gross=3000.00",
                "0.20 (per specification)", String.valueOf(atBoundary), atBoundary == 0.20);
    }

    public String coverageSummary() {
        return branchesCovered.size() + "/" + TOTAL_BRANCHES + " branches exercised";
    }
}
