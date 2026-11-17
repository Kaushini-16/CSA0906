package parking.model;

public abstract class Vehicle {
    private final String regNo;
    private int minutes;                       // private + validating setter

    protected Vehicle(String regNo) {
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("regNo required");
        this.regNo = regNo;
    }
    public String getRegNo() { return regNo; }
    public int getMinutes() { return minutes; }
    public void setMinutes(int minutes) {
        if (minutes < 0) throw new IllegalArgumentException("duration cannot be negative");
        this.minutes = minutes;
    }
    protected abstract double ratePerHour();   // protected: visible to subclasses only
    public double calculateFee() { return ((minutes + 59) / 60) * ratePerHour(); }
}
