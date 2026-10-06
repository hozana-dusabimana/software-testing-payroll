import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** Manages employee records: add, update, remove, list. (FR1) */
public class EmployeeManager {

    private final Map<String, Employee> employees = new LinkedHashMap<>();

    public void addEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (employees.containsKey(employee.getId())) {
            throw new IllegalStateException("An employee with ID " + employee.getId() + " already exists");
        }
        employees.put(employee.getId(), employee);
    }

    public void updateEmployee(String id, String name, String department, double hourlyRate) {
        Employee employee = employees.get(id);
        if (employee == null) {
            throw new NoSuchElementException("No employee found with ID " + id);
        }
        employee.setName(name);
        employee.setDepartment(department);
        employee.setHourlyRate(hourlyRate);
    }

    public void removeEmployee(String id) {
        if (!employees.containsKey(id)) {
            throw new NoSuchElementException("No employee found with ID " + id);
        }
        employees.remove(id);
    }

    public Employee getEmployee(String id) {
        return employees.get(id);
    }

    public List<Employee> listEmployees() {
        return new ArrayList<>(employees.values());
    }

    public int count() {
        return employees.size();
    }
}
