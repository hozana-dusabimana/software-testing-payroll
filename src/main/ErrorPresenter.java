/**
 * Translates a domain-layer failure into the message an end user should see. (user-friendly messages, FR1.2/FR1.4/FR2.5/FR3.2)
 *
 * EmployeeManager and PayrollService correctly throw exceptions with readable
 * messages for invalid input (validation); this class is the single boundary that
 * catches them and hands back plain text instead of letting a raw exception
 * (or stack trace) reach the user.
 */
public class ErrorPresenter {

    /**
     * Runs the given operation. Returns null if it succeeded, or the
     * user-facing message if it failed with a validation error.
     */
    public static String present(Runnable operation) {
        try {
            operation.run();
            return null;
        } catch (RuntimeException e) {
            return e.getMessage();
        }
    }
}
