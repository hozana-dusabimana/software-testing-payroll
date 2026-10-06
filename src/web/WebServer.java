import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Small HTTP backend (JDK built-in server, no libraries) that exposes the existing
 * Java modules to the HTML pages in /web, so each functional requirement can be
 * demonstrated live. Business rules stay in EmployeeManager / PayrollService /
 * ReportGenerator; this class only translates HTTP <-> those classes.
 *
 * Every failure is returned as a plain-text user message (FR2.5), never as an
 * exception class name or stack trace.
 */
public class WebServer {

    private static EmployeeManager employees;
    private static final PayrollService payroll = new PayrollService();
    private static ReportGenerator reports;
    private static final Path webRoot = Path.of("web");

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        resetData();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", WebServer::handle);
        server.start();
        System.out.println("Payroll demo running at http://localhost:" + port + "/  (Ctrl+C to stop)");
    }

    private static synchronized void resetData() {
        employees = new EmployeeManager();
        reports = new ReportGenerator(employees, payroll);
        employees.addEmployee(new Employee("E001", "Aline Uwase", "Finance", 20.0));
        employees.addEmployee(new Employee("E002", "Jean Mugisha", "IT", 35.0));
    }

    // ------------------------------------------------------------ routing

    private static void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        try {
            if (path.startsWith("/api/")) {
                String json;
                try {
                    json = route(ex.getRequestMethod(), path, readForm(ex));
                } catch (RuntimeException e) {
                    // FR2.5 / user-friendly messages: the user only ever sees the readable message.
                    send(ex, 400, "application/json",
                            "{\"ok\":false,\"message\":" + q(e.getMessage()) + "}");
                    return;
                }
                send(ex, 200, "application/json", json);
            } else {
                serveStatic(ex, path);
            }
        } catch (Exception e) {
            send(ex, 500, "application/json",
                    "{\"ok\":false,\"message\":\"Something went wrong. Please try again.\"}");
        }
    }

    private static synchronized String route(String method, String path, Map<String, String> f) {
        if (path.equals("/api/employees")) {
            if (method.equals("GET")) return ok("\"employees\":" + employeesJson());
            if (method.equals("POST")) {
                employees.addEmployee(new Employee(f.getOrDefault("id", ""), f.get("name"),
                        f.get("department"), num(f, "rate", "Hourly rate")));
                return ok("\"message\":" + q("Employee " + f.get("id") + " added successfully")
                        + ",\"employees\":" + employeesJson());
            }
        }
        if (path.startsWith("/api/employees/")) {
            String id = URLDecoder.decode(path.substring("/api/employees/".length()), StandardCharsets.UTF_8);
            if (method.equals("PUT")) {
                employees.updateEmployee(id, f.get("name"), f.get("department"), num(f, "rate", "Hourly rate"));
                return ok("\"message\":" + q("Employee " + id + " updated successfully")
                        + ",\"employees\":" + employeesJson());
            }
            if (method.equals("DELETE")) {
                employees.removeEmployee(id);
                return ok("\"message\":" + q("Employee " + id + " removed successfully")
                        + ",\"employees\":" + employeesJson());
            }
        }
        if (path.equals("/api/payroll") && method.equals("POST")) {
            double hours = num(f, "hours", "Hours worked");
            double rate = num(f, "rate", "Hourly rate");
            double gross = payroll.calculateGrossPay(hours, rate);
            double taxRate = payroll.calculateTaxRate(gross);
            double tax = payroll.calculateTax(gross);
            return ok("\"gross\":" + d(gross) + ",\"taxRate\":" + d(taxRate)
                    + ",\"tax\":" + d(tax) + ",\"net\":" + d(gross - tax));
        }
        if (path.equals("/api/payslip") && method.equals("POST")) {
            String slip = reports.generatePayslip(f.getOrDefault("id", ""), num(f, "hours", "Hours worked"));
            return ok("\"report\":" + q(slip));
        }
        if (path.equals("/api/summary") && method.equals("POST")) {
            Map<String, Double> hours = new LinkedHashMap<>();
            for (Employee e : employees.listEmployees()) {
                String key = "hours_" + e.getId();
                String v = f.get(key);
                if (v != null && !v.isBlank()) hours.put(e.getId(), num(f, key, "Hours worked for " + e.getId()));
            }
            return ok("\"report\":" + q(reports.generateSummaryReport(hours)));
        }
        if (path.equals("/api/tests") && method.equals("GET")) return runTests();
        if (path.equals("/api/reset") && method.equals("POST")) {
            resetData();
            return ok("\"message\":" + q("Demo data reset") + ",\"employees\":" + employeesJson());
        }
        throw new IllegalArgumentException("That action is not available.");
    }

    // ------------------------------------------------------------ test runner

    private static String runTests() {
        return ok(TestReport.runAllJson());
    }

    // ------------------------------------------------------------ helpers

    private static String employeesJson() {
        StringBuilder sb = new StringBuilder("[");
        for (Employee e : employees.listEmployees()) {
            if (sb.length() > 1) sb.append(',');
            sb.append("{\"id\":").append(q(e.getId()))
              .append(",\"name\":").append(q(e.getName()))
              .append(",\"department\":").append(q(e.getDepartment()))
              .append(",\"rate\":").append(d(e.getHourlyRate())).append('}');
        }
        return sb.append(']').toString();
    }

    /** Parses a numeric form field, failing with a readable message instead of NumberFormatException. */
    private static double num(Map<String, String> f, String key, String label) {
        String v = f.get(key);
        if (v == null || v.isBlank()) throw new IllegalArgumentException(label + " is required");
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a number");
        }
    }

    private static String ok(String fields) { return "{\"ok\":true," + fields + "}"; }
    private static String d(double v) { return String.format(Locale.ROOT, "%.2f", v); }

    private static String q(String s) {
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

    private static Map<String, String> readForm(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> m = new HashMap<>();
        if (body.isBlank()) return m;
        for (String pair : body.split("&")) {
            int i = pair.indexOf('=');
            String k = URLDecoder.decode(i < 0 ? pair : pair.substring(0, i), StandardCharsets.UTF_8);
            String v = i < 0 ? "" : URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8);
            m.put(k, v);
        }
        return m;
    }

    private static void serveStatic(HttpExchange ex, String path) throws IOException {
        if (path.equals("/")) path = "/index.html";
        Path file = webRoot.resolve(path.substring(1)).normalize();
        if (!file.startsWith(webRoot.normalize()) || !Files.isRegularFile(file)) {
            send(ex, 404, "text/plain", "Page not found");
            return;
        }
        String name = file.getFileName().toString();
        String type = name.endsWith(".html") ? "text/html" : name.endsWith(".css") ? "text/css"
                : name.endsWith(".js") ? "application/javascript" : "application/octet-stream";
        send(ex, 200, type, Files.readAllBytes(file));
    }

    private static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        send(ex, code, type, body.getBytes(StandardCharsets.UTF_8));
    }

    private static void send(HttpExchange ex, int code, String type, byte[] body) throws IOException {
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.sendResponseHeaders(code, body.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(body); }
    }
}
