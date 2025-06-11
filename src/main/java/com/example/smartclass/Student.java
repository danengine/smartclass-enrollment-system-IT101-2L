package com.example.smartclass;

public class Student {
    private String studentId;
    private String name;
    private String suffix;
    private String email;
    private String gender;
    private String address;
    private String contactNumber;
    private String course;
    private String year;
    private String dateEnrolled;

    public Student(String studentId, String name, String suffix, String email,
                   String gender, String address, String contactNumber,
                   String course, String year, String dateEnrolled) {
        this.studentId = studentId;
        this.name = name;
        this.suffix = suffix;
        this.email = email;
        this.gender = gender;
        this.address = address;
        this.contactNumber = contactNumber;
        this.course = course;
        this.year = year;
        this.dateEnrolled = dateEnrolled;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getCourse() {
        return course;
    }

    public String getYear() {
        return year;
    }

    public String getDateEnrolled() {
        return dateEnrolled;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setDateEnrolled(String dateEnrolled) {
        this.dateEnrolled = dateEnrolled;
    }
}