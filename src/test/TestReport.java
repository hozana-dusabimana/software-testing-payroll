import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Single source of truth for the requirement hierarchy and the traceability
 * from every test case to the (sub-)requirement it verifies. Used by the
 * QA console (WebServer) and by TestRunner's json mode, which feeds the
 * Word report so the report can never drift from the executed tests.
 *
 * The scenario has three requirements: manage employees (FR1), process
 * payments (FR2) and produce reports (FR3). Each is broken into sub-requirements.
 */
public class TestReport {

    /** {id, title, description}. Top-level ids have no dot. */
    public static final String[][] REQUIREMENTS = {
        {"FR1", "Manage Employees", "The system shall manage employee records."},
        {"FR1.1", "Add employee", "The system shall allow adding an employee with an ID, name, department and hourly rate."},
        {"FR1.2", "Add validation", "The system shall reject an employee with an empty ID, a duplicate ID or a negative hourly rate, show a user-friendly message and leave existing data unchanged."},
        {"FR1.3", "Update employee", "The system shall allow updating the name, department and hourly rate of an existing employee."},
        {"FR1.4", "Update validation", "The system shall reject an update for an unknown employee ID or a negative hourly rate, with a user-friendly message."},
        {"FR1.5", "Remove employee", "The system shall allow removing an existing employee."},
        {"FR1.6", "Remove validation", "The system shall reject removal of an unknown employee ID, with a user-friendly message."},
        {"FR1.7", "List employees", "The system shall retrieve and list all employee records."},
        {"FR2", "Process Payments", "The system shall calculate what each employee is paid."},
        {"FR2.1", "Gross pay", "The system shall pay the first 40 hours at the hourly rate."},
        {"FR2.2", "Overtime", "The system shall pay hours beyond 40 at 1.5 times the hourly rate."},
        {"FR2.3", "Tax bracket", "The system shall apply tax on gross pay: up to 1000 = 10%, above 1000 up to 3000 = 20%, above 3000 = 30%."},
        {"FR2.4", "Net pay", "The system shall compute tax as gross x tax rate and net pay as gross minus tax."},
        {"FR2.5", "Payment validation", "The system shall reject negative hours or a negative hourly rate and show a user-friendly message (never an exception name or stack trace); a valid calculation shows no error."},
        {"FR3", "Produce Reports", "The system shall produce payroll reports."},
        {"FR3.1", "Payslip", "The system shall generate a payslip for one employee (name, ID, department, hours, gross pay, tax, net pay)."},
        {"FR3.2", "Payslip validation", "The system shall reject a payslip for an unknown employee or negative hours, with a user-friendly message."},
        {"FR3.3", "Payroll summary", "The system shall generate an organization-wide summary listing each employee's hours and net pay and the total net payout."},
    };

    private static final Map<String, String> REQ = new HashMap<>();
    static {
        map("FR1.1", "BB-EP-07");
        map("FR1.2", "BB-EP-08", "BB-EP-16", "BB-EP-17", "BB-EP-23");
        map("FR1.3", "BB-EP-09");
        map("FR1.4", "BB-EP-10", "BB-EP-18");
        map("FR1.5", "BB-EP-11");
        map("FR1.6", "BB-EP-12");
        map("FR1.7", "BB-EP-15");
        map("FR2.1", "BB-EP-01", "BB-BVA-07", "WB-BC-04");
        map("FR2.2", "BB-EP-02", "WB-BC-03");
        map("FR2.3", "BB-EP-04", "BB-EP-05", "BB-EP-06", "BB-BVA-01", "BB-BVA-02", "BB-BVA-03",
                "BB-BVA-04", "BB-BVA-05", "BB-BVA-06", "WB-BC-05", "WB-BC-06", "WB-BC-07", "WB-PC-01");
        map("FR2.4", "BB-DT-01", "BB-DT-02", "BB-DT-03");
        map("FR2.3/FR2.4", "BB-DT-04");
        map("FR2.5", "BB-EP-03", "BB-BVA-08", "BB-EP-13", "BB-EP-14", "WB-BC-01", "WB-BC-02");
        map("FR3.1", "BB-EP-19");
        map("FR3.2", "BB-EP-20", "BB-EP-21");
        map("FR3.3", "BB-EP-22");
    }

    private static void map(String req, String... ids) {
        for (String id : ids) REQ.put(id, req);
    }

    public static String req(String testId) {
        return REQ.getOrDefault(testId, "");
    }

    // ------------------------------------------------------------ JSON

    public static String q(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c < 0x20 ? ' ' : c);
            }
        }
        return sb.append('"').toString();
    }

    public static String resultsJson(List<TestResult> list) {
        StringBuilder sb = new StringBuilder("[");
        for (TestResult r : list) {
            if (sb.length() > 1) sb.append(',');
            sb.append("{\"id\":").append(q(r.id))
              .append(",\"req\":").append(q(req(r.id)))
              .append(",\"description\":").append(q(r.description))
              .append(",\"input\":").append(q(r.input))
              .append(",\"expected\":").append(q(r.expected))
              .append(",\"actual\":").append(q(r.actual))
              .append(",\"passed\":").append(r.passed).append('}');
        }
        return sb.append(']').toString();
    }

    public static String requirementsJson() {
        StringBuilder sb = new StringBuilder("[");
        for (String[] r : REQUIREMENTS) {
            if (sb.length() > 1) sb.append(',');
            sb.append("{\"id\":").append(q(r[0])).append(",\"title\":").append(q(r[1]))
              .append(",\"description\":").append(q(r[2])).append('}');
        }
        return sb.append(']').toString();
    }

    /** Runs both suites and returns the JSON fields (requirements, blackBox, whiteBox, coverage). */
    public static String runAllJson() {
        WhiteBoxTests wb = new WhiteBoxTests();
        List<TestResult> white = wb.run();
        List<TestResult> black = new BlackBoxTests().run();
        return "\"requirements\":" + requirementsJson()
                + ",\"blackBox\":" + resultsJson(black)
                + ",\"whiteBox\":" + resultsJson(white)
                + ",\"coverage\":" + q(wb.coverageSummary());
    }
}
