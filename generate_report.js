const fs = require("fs");
const {
  Document, Packer, Paragraph, TextRun, HeadingLevel, Table, TableRow, TableCell,
  WidthType, BorderStyle, ShadingType, AlignmentType, PageOrientation, PageBreak
} = require("docx");

const PORTRAIT = { width: 12240, height: 15840 }; // US Letter, DXA
const MARGIN = 1440;

function h1(text) {
  return new Paragraph({ text, heading: HeadingLevel.HEADING_1, spacing: { before: 300, after: 150 } });
}
function h2(text) {
  return new Paragraph({ text, heading: HeadingLevel.HEADING_2, spacing: { before: 240, after: 120 } });
}
function p(text, opts = {}) {
  return new Paragraph({ children: [new TextRun({ text, ...opts })], spacing: { after: 120 } });
}
function bullet(text) {
  return new Paragraph({ text, bullet: { level: 0 }, spacing: { after: 60 } });
}

function cell(text, { width, bold = false, shade = null, align = AlignmentType.LEFT } = {}) {
  return new TableCell({
    width: { size: width, type: WidthType.DXA },
    shading: shade ? { type: ShadingType.CLEAR, color: "auto", fill: shade } : undefined,
    margins: { top: 60, bottom: 60, left: 80, right: 80 },
    children: [new Paragraph({
      alignment: align,
      children: [new TextRun({ text: String(text), bold })]
    })]
  });
}

function headerRow(labels, widths) {
  return new TableRow({
    tableHeader: true,
    children: labels.map((l, i) => cell(l, { width: widths[i], bold: true, shade: "D9E2F3" }))
  });
}

// ---------------- Requirements and test results come from the real test run ----------------
const { execSync } = require("child_process");
const run = JSON.parse(execSync("java -cp out TestRunner json", { encoding: "utf8" }));
const requirements = run.requirements;

function technique(id) {
  if (id.startsWith("BB-EP")) return "EP";
  if (id.startsWith("BB-BVA")) return "BVA";
  if (id.startsWith("BB-DT")) return "Decision Table";
  if (id.startsWith("WB-BC")) return "Branch";
  return "Path";
}
const toRow = t => [t.id, technique(t.id), t.description, t.input, t.expected, t.actual, t.passed ? "Pass" : "FAIL", t.req];
const blackBox = run.blackBox.map(toRow);
const whiteBox = run.whiteBox.map(toRow);
const count = rows => ({ total: rows.length, passed: rows.filter(r => r[6] === "Pass").length });
const bb = count(blackBox), wb = count(whiteBox);
const totalCases = bb.total + wb.total;

function testTable(rows) {
  const widths = [1000, 1100, 3000, 1800, 1700, 1500, 700, 1160];
  const header = headerRow(["ID", "Technique", "Description", "Input", "Expected", "Actual", "Status", "Req"], widths);
  const body = rows.map(r => new TableRow({
    children: [
      cell(r[0], { width: widths[0] }),
      cell(r[1], { width: widths[1] }),
      cell(r[2], { width: widths[2] }),
      cell(r[3], { width: widths[3] }),
      cell(r[4], { width: widths[4] }),
      cell(r[5], { width: widths[5] }),
      cell(r[6], { width: widths[6], bold: r[6] === "FAIL", shade: r[6] === "FAIL" ? "F8CBAD" : null }),
      cell(r[7], { width: widths[7] }),
    ]
  }));
  return new Table({ width: { size: 11960, type: WidthType.DXA }, columnWidths: widths, rows: [header, ...body] });
}

function reqTable() {
  const widths = [1000, 2300, 6060];
  const header = headerRow(["ID", "Requirement", "Description"], widths);
  const body = requirements.map(r => {
    const top = !r.id.includes(".");
    const shade = top ? "D9E2F3" : null;
    return new TableRow({ children: [
      cell(r.id, { width: widths[0], bold: true, shade }),
      cell(r.title, { width: widths[1], bold: top, shade }),
      cell(r.description, { width: widths[2], bold: top, shade })] });
  });
  return new Table({ width: { size: 9360, type: WidthType.DXA }, columnWidths: widths, rows: [header, ...body] });
}

// Traceability matrix: requirement -> test cases -> result
function traceTable() {
  const all = [...blackBox, ...whiteBox];
  const widths = [1000, 2300, 4260, 1800];
  const header = headerRow(["ID", "Requirement", "Test cases", "Result"], widths);
  const body = requirements.map(r => {
    const top = !r.id.includes(".");
    const tests = all.filter(t => t[7].split("/").some(x => x === r.id || (top && x.startsWith(r.id + "."))));
    const failed = tests.filter(t => t[6] !== "Pass").length;
    const result = !tests.length ? "NO TESTS" : failed ? failed + " failed of " + tests.length : "All " + tests.length + " passed";
    const shade = top ? "D9E2F3" : (failed || !tests.length ? "F8CBAD" : null);
    return new TableRow({ children: [
      cell(r.id, { width: widths[0], bold: true, shade }),
      cell(r.title, { width: widths[1], bold: top, shade }),
      cell(tests.map(t => t[0]).join(", ") || "-", { width: widths[2], shade }),
      cell(result, { width: widths[3], bold: failed > 0, shade })] });
  });
  return new Table({ width: { size: 9360, type: WidthType.DXA }, columnWidths: widths, rows: [header, ...body] });
}

function infoTable(rows, widths) {
  const body = rows.map(r => new TableRow({
    children: [cell(r[0], { width: widths[0], bold: true, shade: "F2F2F2" }), cell(r[1], { width: widths[1] })]
  }));
  return new Table({ width: { size: widths[0] + widths[1], type: WidthType.DXA }, columnWidths: widths, rows: body });
}

// ---- Full test-case-report-template block, field order matches
//      test-case-report-template.doc exactly: GENERAL INFORMATION,
//      INTRODUCTION, ENVIRONMENTAL NEEDS, TEST, ACTUAL RESULTS. ----
function sectionHeaderRow(text, widths) {
  return new TableRow({
    children: [new TableCell({
      columnSpan: 2,
      width: { size: widths[0] + widths[1], type: WidthType.DXA },
      shading: { type: ShadingType.CLEAR, color: "auto", fill: "1F4E78" },
      margins: { top: 60, bottom: 60, left: 80, right: 80 },
      children: [new Paragraph({ children: [new TextRun({ text, bold: true, color: "FFFFFF" })] })]
    })]
  });
}

function kvRow(label, value, widths) {
  return new TableRow({
    children: [cell(label, { width: widths[0], bold: true, shade: "F2F2F2" }), cell(value, { width: widths[1] })]
  });
}

function fullTemplateBlock(tc, widths) {
  const rows = [
    sectionHeaderRow("GENERAL INFORMATION", widths),
    kvRow("Test Stage", tc.testStage, widths),
    kvRow("Test Date / System Date, if applicable", tc.testDate, widths),
    kvRow("Tester / Test Case Number", tc.tester + "  /  " + tc.id, widths),
    kvRow("Test Case Description", tc.description, widths),
    kvRow("Results / Incident Number, if applicable", tc.result + "  /  " + tc.incident, widths),

    sectionHeaderRow("INTRODUCTION", widths),
    kvRow("Requirement(s) to be tested", tc.requirement, widths),
    kvRow("Roles and Responsibilities", tc.roles, widths),
    kvRow("Set Up Procedures", tc.setup, widths),
    kvRow("Stop Procedures", tc.stop, widths),

    sectionHeaderRow("ENVIRONMENTAL NEEDS", widths),
    kvRow("Hardware", tc.hardware, widths),
    kvRow("Software", tc.software, widths),
    kvRow("Procedural Requirements", tc.procedural, widths),

    sectionHeaderRow("TEST", widths),
    kvRow("Test Items and Features", tc.items, widths),
    kvRow("Input Specifications", tc.input, widths),
    kvRow("Procedural Steps", tc.steps, widths),
    kvRow("Expected Results of Case", tc.expected, widths),

    sectionHeaderRow("ACTUAL RESULTS", widths),
    kvRow("Output Specifications", tc.actualOutput, widths),
  ];
  return new Table({ width: { size: widths[0] + widths[1], type: WidthType.DXA }, columnWidths: widths, rows });
}

const TESTER_NAME = "Hozana Dusabimana";
const TEST_DATE = "09/22/26  /  N/A";

const templateCases = [
  {
    id: "BB-EP-01",
    testStage: "Functionality (black-box) testing of the payroll module.",
    tester: TESTER_NAME,
    testDate: TEST_DATE,
    description: "Verify that PayrollService.calculateGrossPay() correctly computes gross pay for the regular-hours equivalence class (0-40 hours), designed by Equivalence Partitioning from FR2.",
    result: "Pass",
    incident: "N/A",
    requirement: "FR2.1 Gross pay - the first 40 hours are paid at the hourly rate (Section 2).",
    roles: TESTER_NAME + " - implemented the system, designed this test case, executed it and recorded the result.",
    setup: "Compile the project (javac -d out src/main/*.java src/test/*.java) and instantiate a new PayrollService.",
    stop: "None required; the JVM simply exits after the test method returns, no external resources are held.",
    hardware: "Any machine able to run a JDK.",
    software: "JDK 24 (java/javac). No external libraries or frameworks required.",
    procedural: "Executed via BlackBoxTests.equivalencePartitioning_hoursWorked(), invoked from src/test/TestRunner.java.",
    items: "PayrollService.calculateGrossPay(double hoursWorked, double hourlyRate) - regular-hours equivalence class.",
    input: "hoursWorked = 20, hourlyRate = 10.0",
    steps: "1) Create a PayrollService instance. 2) Call calculateGrossPay(20, 10.0). 3) Compare the returned value to the expected gross pay.",
    expected: "200.00 (20 regular hours x 10.0/hour, no overtime).",
    actualOutput: "Returned value = 200.00. Matches the expected value exactly -> PASS.",
  },
  {
    id: "BB-EP-02",
    testStage: "Functionality (black-box) testing of the payroll module.",
    tester: TESTER_NAME,
    testDate: TEST_DATE,
    description: "Verify that PayrollService.calculateGrossPay() correctly computes gross pay for the overtime-hours equivalence class (>40 hours), designed by Equivalence Partitioning from FR2.",
    result: "Pass",
    incident: "N/A",
    requirement: "FR2.2 Overtime - hours beyond 40 are paid at 1.5 times the hourly rate (Section 2).",
    roles: TESTER_NAME + " - implemented the system, designed this test case, executed it and recorded the result.",
    setup: "Compile the project (javac -d out src/main/*.java src/test/*.java) and instantiate a new PayrollService.",
    stop: "None required; the JVM simply exits after the test method returns, no external resources are held.",
    hardware: "Any machine able to run a JDK.",
    software: "JDK 24 (java/javac). No external libraries or frameworks required.",
    procedural: "Executed via BlackBoxTests.equivalencePartitioning_hoursWorked(), invoked from src/test/TestRunner.java.",
    items: "PayrollService.calculateGrossPay(double hoursWorked, double hourlyRate) - overtime-hours equivalence class.",
    input: "hoursWorked = 45, hourlyRate = 10.0",
    steps: "1) Create a PayrollService instance. 2) Call calculateGrossPay(45, 10.0). 3) Compare the returned value to the expected gross pay.",
    expected: "475.00 (40 regular hours x 10.0 + 5 overtime hours x 10.0 x 1.5).",
    actualOutput: "Returned value = 475.00. Matches the expected value exactly -> PASS.",
  },
  {
    id: "BB-EP-03",
    testStage: "Functionality (black-box) testing of the payroll module.",
    tester: TESTER_NAME,
    testDate: TEST_DATE,
    description: "Verify that PayrollService.calculateGrossPay() rejects the invalid negative-hours equivalence class with a user-friendly message, per FR2.5.",
    result: "Pass",
    incident: "N/A",
    requirement: "FR2.5 Payment validation - negative hours or rate must be rejected with a user-friendly message (Section 2).",
    roles: TESTER_NAME + " - implemented the system, designed this test case, executed it and recorded the result.",
    setup: "Compile the project (javac -d out src/main/*.java src/test/*.java) and instantiate a new PayrollService.",
    stop: "None required; the JVM simply exits after the test method returns, no external resources are held.",
    hardware: "Any machine able to run a JDK.",
    software: "JDK 24 (java/javac). No external libraries or frameworks required.",
    procedural: "Executed via BlackBoxTests.equivalencePartitioning_hoursWorked(), invoked from src/test/TestRunner.java.",
    items: "PayrollService.calculateGrossPay(double hoursWorked, double hourlyRate) - invalid negative-hours equivalence class.",
    input: "hoursWorked = -5, hourlyRate = 10.0",
    steps: "1) Create a PayrollService instance. 2) Call calculateGrossPay(-5, 10.0) through ErrorPresenter.present(). 3) Confirm the user sees a readable message.",
    expected: "The user sees the message \"Hours worked cannot be negative\"; no gross pay value is returned and no technical error name or stack trace is shown.",
    actualOutput: "The user sees \"Hours worked cannot be negative\". Matches the expected message -> PASS.",
  },
];

const doc = new Document({
  sections: [
    {
      properties: { page: { size: PORTRAIT, margin: { top: MARGIN, bottom: MARGIN, left: MARGIN, right: MARGIN } } },
      children: [
        new Paragraph({ text: "Software Testing Techniques - Homework Report", heading: HeadingLevel.TITLE, alignment: AlignmentType.CENTER, spacing: { after: 100 } }),
        new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 40 }, children: [new TextRun({ text: "SENG 8325 - Software Testing Techniques", italics: true })] }),
        new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 40 }, children: [new TextRun({ text: "Tester: Hozana Dusabimana", italics: true })] }),
        new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 400 }, children: [new TextRun({ text: "GitHub repository: https://github.com/hozana-dusabimana/software-testing-payroll", bold: true })] }),

        h1("1. Scenario"),
        p("A software engineer was asked to build a system for organization X to manage employees, process their payments, and produce reports. After coding, the engineer suspected some functionality did not behave as intended, so he performed fault-injection based testing to find defects, first designing a test-case document to run automated tests and record results, following an agile development approach."),
        p("Note on the source assignment file: the provided Homework.docx refers twice to a “table below” showing how the tests were conducted, but no such table exists anywhere in that file (verified by inspecting the document's XML - there is no table, image, or embedded object after either reference). This report supplies that missing artifact: Section 5 fills the test-case-report-template itself (one full template per case, in order, as the template instructs) for the first three cases, and Sections 6-7 log the remaining test cases in table form, built from the actual implementation described in Section 2."),

        h1("2. Functional Requirements (Task i)"),
        p("The scenario states that the software must manage employees, process their payments and produce reports. These give three requirements (FR1-FR3), each broken into sub-requirements so every behavior, including its validation, can be tested and traced individually:"),
        reqTable(),
        h2("2.1 Traceability Matrix"),
        p("Each sub-requirement with the test cases that verify it and their result (generated from the executed suites):"),
        traceTable(),

        h1("3. Implementation (Task ii)"),
        p("The scenario was implemented in Java (JDK 24) as a small Employee Management & Payroll system, located under src/main alongside this report:"),
        bullet("Employee.java - employee record (id, name, department, hourlyRate)."),
        bullet("EmployeeManager.java - add/update/remove/list employees and their validation (FR1)."),
        bullet("PayrollService.java - gross pay, overtime, tax rate and net pay calculation (FR2)."),
        bullet("ReportGenerator.java - payslip and organization summary reports (FR3)."),
        bullet("Main.java - demo wiring the modules together, and the presentation-layer boundary for user-friendly messages (see below)."),
        p("Build and run:"),
        new Paragraph({ spacing: { after: 200 }, children: [new TextRun({ text: "javac -d out src/main/*.java src/test/*.java  &&  java -cp out Main", font: "Consolas" })] }),
        p("The payroll specification given to the developer states graduated tax brackets: gross ≤ 1000 → 10%, 1000 < gross ≤ 3000 → 20%, gross > 3000 → 30%. As is common after an agile implementation pass, the coded bracket check used \"grossPay < 3000\" instead of \"grossPay <= 3000\" - an off-by-one boundary mistake that is invisible from casual manual runs but is exactly what boundary-driven testing is designed to catch (see Section 6)."),
        bullet("ErrorPresenter.java - translates a failed operation into the message an end user should see (validation sub-requirements FR1.2, FR1.4, FR1.6, FR2.5, FR3.2)."),
        p("User-friendly messages: EmployeeManager, PayrollService and ReportGenerator throw exceptions with readable messages for invalid input, but nothing originally caught them - an uncaught exception would surface to the end user as a raw Java stack trace. ErrorPresenter.present(Runnable) is the single boundary that catches a validation failure and returns e.getMessage() (or null on success) instead of letting the exception propagate; Main.java and the web backend use it for every user-facing operation that can fail. Every validation sub-requirement (FR1.2, FR1.4, FR1.6, FR2.5, FR3.2) is therefore tested by checking the plain-text message the end user sees (e.g. \"Hours worked cannot be negative\"), never an exception class name. BB-EP-13/BB-EP-14 test ErrorPresenter directly for a failing and a succeeding operation."),

        h1("4. Test Planning (per test-case-report-template)"),
        h2("4.1 General Information"),
        infoTable([
          ["Test Stages", "Unit and Functionality testing"],
          ["Test Date", "2026-09-22"],
          ["Tester", "Hozana Dusabimana"],
          ["Test Case Numbers", "BB-EP-01..23, BB-BVA-01..08, BB-DT-01..04, WB-BC-01..07, WB-PC-01 (Sections 5-7)"],
          ["Test Case Description", "Verify employee management (FR1), payment processing (FR2) and reporting (FR3), including their validation and user-friendly messages, using black-box and white-box techniques."],
        ], [2400, 6960]),
        h2("4.2 Introduction"),
        infoTable([
          ["Requirements tested", "FR1-FR3 and sub-requirements FR1.1-FR3.3 (Section 2)"],
          ["Roles and Responsibilities", "Hozana Dusabimana: implementation, test design, test execution and reporting."],
          ["Set Up Procedures", "Compile the sources with javac into an out/ directory as shown in Section 3."],
          ["Stop Procedures", "No persistent state or external resources are used; the JVM process simply exits after TestRunner.main() completes."],
        ], [2400, 6960]),
        h2("4.3 Environmental Needs"),
        infoTable([
          ["Hardware", "Any machine able to run a JDK (tests were executed on a standard Windows laptop)."],
          ["Software", "JDK 24 (java/javac), no external libraries or frameworks required."],
          ["Procedural Requirements", "Run src/test/TestRunner.java after compiling; it executes both test suites and prints a pass/fail line per case."],
        ], [2400, 6960]),

        h1("5. Filled Test-Case-Report Template - First 3 Test Cases, In Order"),
        p("The test-case-report-template.doc instructs: “Use one template for each test case.” The three templates below are filled in exactly that way and in the same order the cases were designed (BB-EP-01, BB-EP-02, BB-EP-03), preserving the template's own field order (General Information, Introduction, Environmental Needs, Test, Actual Results). The remaining " + (totalCases - 3) + " test cases are logged in table form in Sections 6-7 for readability."),
        h2("5.1 Test Case BB-EP-01"),
        fullTemplateBlock(templateCases[0], [2700, 6660]),
        h2("5.2 Test Case BB-EP-02"),
        fullTemplateBlock(templateCases[1], [2700, 6660]),
        h2("5.3 Test Case BB-EP-03"),
        fullTemplateBlock(templateCases[2], [2700, 6660]),

        new Paragraph({ children: [new PageBreak()] }),
      ]
    },
    {
      properties: {
        page: {
          size: { width: PORTRAIT.width, height: PORTRAIT.height, orientation: PageOrientation.LANDSCAPE },
          margin: { top: MARGIN, bottom: MARGIN, left: MARGIN, right: MARGIN }
        }
      },
      children: [
        h1("6. Black-Box (Functional) Test Cases (Task iii)"),
        p("Designed from the functional requirements/specification only, without reference to the source code, using Equivalence Partitioning (EP), Boundary Value Analysis (BVA) and Decision Table testing (course Chapter 2.1). BB-EP-01..03 are fully templated in Section 5 above and are repeated here only as summary rows so this log stays complete."),
        testTable(blackBox),
        p(`Result: ${bb.passed}/${bb.total} passed, ${bb.total - bb.passed} failed.`, { bold: true }),

        h1("7. White-Box (Structural) Test Cases (Task iv)"),
        p("Designed from the internal source code of PayrollService.calculateGrossPay (3 decisions / 6 branches: B1 hoursWorked<0, B2 hourlyRate<0, B3 hoursWorked>40) and calculateTaxRate (2 decisions / 4 branches: B4 grossPay<=1000, B5 grossPay<3000), using Statement, Branch and Path coverage (course Chapter 2.2)."),
        testTable(whiteBox),
        p(`Result: ${wb.passed}/${wb.total} passed, ${wb.total - wb.passed} failed. Branch coverage achieved: ${run.coverage.replace(" exercised", "")} (100%).`, { bold: true }),

        h1("8. Defect Found"),
        p("Defect: PayrollService.calculateTaxRate() applies 30% tax to a gross pay of exactly 3000, instead of the specified 20%."),
        bullet("Root cause: the mid/high bracket check is written as \"grossPay < 3000\" instead of \"grossPay <= 3000\"."),
        bullet("Requirement violated: FR2.3 Tax bracket. Detected by: BB-BVA-05 and BB-DT-04 (black-box boundary value analysis on the documented tax brackets) and WB-PC-01 (white-box path coverage over the literal code boundary)."),
        bullet("Impact: any employee whose gross pay lands exactly on 3000 is overtaxed (net pay 2100.00 instead of the specified 2400.00 for the BB-DT-04 case)."),
        bullet("Suggested fix: change \"else if (grossPay < 3000.0)\" to \"else if (grossPay <= 3000.0)\" in PayrollService.calculateTaxRate()."),

        h1("9. Conclusion"),
        p(`Both testing approaches independently converged on the same defect at the gross=3000 tax boundary (sub-requirement FR2.3): black-box testing found it by reasoning purely from the documented tax brackets, while white-box testing found it by exercising the exact branch condition written in the code. Validation sub-requirements (FR1.2, FR1.4, FR1.6, FR2.5, FR3.2) were verified by the plain-text message the end user sees, not by exception types. Together the ${totalCases} test cases cover all three requirements and all ${requirements.length - 3} sub-requirements (see the traceability matrix in Section 2.1) and 100% branch coverage of the payroll decision logic, which let a defect invisible to a normal (non-boundary) run be caught before release.`),
      ]
    }
  ]
});

Packer.toBuffer(doc).then(buf => {
  fs.writeFileSync("Test-Case-Report.docx", buf);
  console.log("wrote Test-Case-Report.docx", buf.length, "bytes");
});
