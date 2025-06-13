package com.example.smartclass;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Course {
    private final SimpleStringProperty courseCode, courseName, prerequisites;
    private final IntegerProperty units = new SimpleIntegerProperty(0);
    private final SimpleStringProperty schedule = new SimpleStringProperty("N/A");

    public Course(String code, String name, String prereq, int units, String schedule) {
        this.courseCode = new SimpleStringProperty(code);
        this.courseName = new SimpleStringProperty(name);
        this.prerequisites = new SimpleStringProperty(prereq);
        this.units.set(units);
        this.schedule.set(schedule);
    }
    public Course(String code, String name, String prereq, int units) {
        this(code, name, prereq, units, "N/A");
    }
    public Course(String code, String name, String prereq) {
        this(code, name, prereq, 0, "N/A");
    }

    public StringProperty courseCodeProperty() { return courseCode; }
    public StringProperty courseNameProperty() { return courseName; }
    public StringProperty prerequisitesProperty() { return prerequisites; }
    public IntegerProperty unitsProperty() { return units; }
    public StringProperty scheduleProperty() { return schedule; }
    public int getUnits() { return units.get(); }
    public void setUnits(int value) { units.set(value); }
    public String getSchedule() { return schedule.get(); }
    public void setSchedule(String value) { schedule.set(value); }


    @Override
    public String toString() {
        return courseCode.get() + " - " + courseName.get() + " (" + schedule.get() + ")";
    }
}