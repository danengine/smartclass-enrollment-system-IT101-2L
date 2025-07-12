package com.example.smartclass;

public class Student {
    private String studentId;
    private String lrn;
    private String name;
    private String suffix;
    private String email;
    private String gender;
    private String address;
    private String contactNumber;
    private String course;
    private String year;
    private String dateEnrolled;
    private String fatherName;
    private String motherName;
    private String guardianName;
    private String familyContact;
    private boolean birthCertSubmitted;
    private boolean form137Submitted;
    private boolean goodMoralSubmitted;
    private boolean medCertSubmitted;
    private String fatherContact;
    private String motherContact;
    private String guardianContact;
    private boolean archived;

    public Student(String studentId, String lrn, String name, String suffix, String email,
                   String gender, String address, String contactNumber,
                   String course, String year, String dateEnrolled,
                   String fatherName, String fatherContact,
                   String motherName, String motherContact,
                   String guardianName, String guardianContact,
                   boolean birthCertSubmitted, boolean form137Submitted, boolean goodMoralSubmitted, boolean medCertSubmitted,
                   boolean archived) {
        this.studentId = studentId;
        this.lrn = lrn;
        this.name = name;
        this.suffix = suffix;
        this.email = email;
        this.gender = gender;
        this.address = address;
        this.contactNumber = contactNumber;
        this.course = course;
        this.year = year;
        this.dateEnrolled = dateEnrolled;
        this.fatherName = fatherName;
        this.fatherContact = fatherContact;
        this.motherName = motherName;
        this.motherContact = motherContact;
        this.guardianName = guardianName;
        this.guardianContact = guardianContact;
        this.birthCertSubmitted = birthCertSubmitted;
        this.form137Submitted = form137Submitted;
        this.goodMoralSubmitted = goodMoralSubmitted;
        this.medCertSubmitted = medCertSubmitted;
        this.archived = archived;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getLrn() {
        return lrn;
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

    public String getFatherName() {
        return fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public String getFamilyContact() {
        return familyContact;
    }

    public boolean isBirthCertSubmitted() {
        return birthCertSubmitted;
    }

    public boolean isForm137Submitted() {
        return form137Submitted;
    }

    public boolean isGoodMoralSubmitted() {
        return goodMoralSubmitted;
    }

    public boolean isMedCertSubmitted() {
        return medCertSubmitted;
    }

    public String getFatherContact() {
        return fatherContact;
    }

    public String getMotherContact() {
        return motherContact;
    }

    public String getGuardianContact() {
        return guardianContact;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setLrn(String lrn) {
        this.lrn = lrn;
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

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public void setFamilyContact(String familyContact) {
        this.familyContact = familyContact;
    }

    public void setBirthCertSubmitted(boolean birthCertSubmitted) {
        this.birthCertSubmitted = birthCertSubmitted;
    }

    public void setForm137Submitted(boolean form137Submitted) {
        this.form137Submitted = form137Submitted;
    }

    public void setGoodMoralSubmitted(boolean goodMoralSubmitted) {
        this.goodMoralSubmitted = goodMoralSubmitted;
    }

    public void setMedCertSubmitted(boolean medCertSubmitted) {
        this.medCertSubmitted = medCertSubmitted;
    }

    public void setFatherContact(String fatherContact) {
        this.fatherContact = fatherContact;
    }

    public void setMotherContact(String motherContact) {
        this.motherContact = motherContact;
    }

    public void setGuardianContact(String guardianContact) {
        this.guardianContact = guardianContact;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}