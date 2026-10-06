public class Employee {

    private final String id;
    private String name;
    private String department;
    private double hourlyRate;

    public Employee(String id, String name, String department, double hourlyRate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Employee id cannot be empty");
        }
        if (hourlyRate < 0) {
            throw new IllegalArgumentException("Hourly rate cannot be negative");
        }
        this.id = id;
        this.name = name;
        this.department = department;
        this.hourlyRate = hourlyRate;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public double getHourlyRate() { return hourlyRate; }

    public void setName(String name) { this.name = name; }
    public void setDepartment(String department) { this.department = department; }

    public void setHourlyRate(double hourlyRate) {
        if (hourlyRate < 0) {
            throw new IllegalArgumentException("Hourly rate cannot be negative");
        }
        this.hourlyRate = hourlyRate;
    }

    @Override
    public String toString() {
        return String.format("Employee{id=%s, name=%s, department=%s, hourlyRate=%.2f}",
                id, name, department, hourlyRate);
    }
}
