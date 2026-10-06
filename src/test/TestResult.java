public class TestResult {
    public final String id;
    public final String description;
    public final String input;
    public final String expected;
    public final String actual;
    public final boolean passed;

    public TestResult(String id, String description, String input, String expected, String actual, boolean passed) {
        this.id = id;
        this.description = description;
        this.input = input;
        this.expected = expected;
        this.actual = actual;
        this.passed = passed;
    }

    @Override
    public String toString() {
        return String.format("[%s] %-45s | input=%-18s | expected=%-8s | actual=%-8s | %s",
                id, description, input, expected, actual, passed ? "PASS" : "FAIL");
    }
}
