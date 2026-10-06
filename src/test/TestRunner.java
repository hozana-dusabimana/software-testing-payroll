import java.util.List;

public class TestRunner {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("json")) {
            System.out.println("{" + TestReport.runAllJson() + "}");
            return;
        }
        System.out.println("===== BLACK-BOX (FUNCTIONAL) TESTS =====");
        List<TestResult> blackBox = new BlackBoxTests().run();
        printAndSummarize(blackBox);

        System.out.println();
        System.out.println("===== WHITE-BOX (STRUCTURAL) TESTS =====");
        WhiteBoxTests whiteBoxTests = new WhiteBoxTests();
        List<TestResult> whiteBox = whiteBoxTests.run();
        printAndSummarize(whiteBox);
        System.out.println("Coverage: " + whiteBoxTests.coverageSummary());
    }

    private static void printAndSummarize(List<TestResult> results) {
        int passed = 0;
        for (TestResult r : results) {
            System.out.println(r);
            if (r.passed) passed++;
        }
        System.out.printf("%d/%d passed, %d failed%n", passed, results.size(), results.size() - passed);
    }
}
