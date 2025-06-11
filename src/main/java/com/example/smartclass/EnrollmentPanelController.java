package com.example.smartclass;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class EnrollmentPanelController {

    @FXML private TextField searchField;
    @FXML private Label studentIdLabel, studentNameLabel, programLabel;
    @FXML private ComboBox<String> termComboBox;
    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, String> codeColumn, nameColumn, prereqColumn, enrolledColumn;

    private String studentId, studentName, program;

    @FXML
    public void initialize() {
        codeColumn.setCellValueFactory(data -> data.getValue().courseCodeProperty());
        nameColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());
        prereqColumn.setCellValueFactory(data -> data.getValue().prerequisitesProperty());
        //enrolledColumn.setCellValueFactory(data -> data.getValue().enrolledProperty());

        loadTerms();
    }

    @FXML
    private void onSearchStudent() {
        String input = searchField.getText().trim().toLowerCase();
        if (input.isEmpty()) return;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get("students.csv"))) {
            String line;
            boolean found = false;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    String prog = parts[3].trim();

                    if (id.equalsIgnoreCase(input) || name.toLowerCase().contains(input)) {
                        studentId = id;
                        studentName = name;
                        program = prog;
                        found = true;
                        break;
                    }
                }
            }

            if (found) {
                studentIdLabel.setText(studentId);
                studentNameLabel.setText(studentName);
                programLabel.setText(program);
                loadCourses();
            } else {
                showAlert("Student not found.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error reading students.csv");
        }
    }

    private void loadTerms() {
        termComboBox.getItems().clear();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("terms.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                termComboBox.getItems().add(line.trim());
            }
            if (!termComboBox.getItems().isEmpty()) {
                termComboBox.getSelectionModel().selectLast(); // Select latest
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error loading terms.");
        }
    }

    private void loadCourses() {
        coursesTable.getItems().clear();
        Path programFile = Paths.get("programs", program + ".csv");

        if (!Files.exists(programFile)) {
            showAlert("Program file not found: " + programFile);
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(programFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    String code = parts[0].trim();
                    String name = parts[1].trim();
                    String prereq = parts[2].trim();
                    Course course = new Course(code, name, prereq);
                    coursesTable.getItems().add(course);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Failed to load courses.");
        }
    }

    @FXML
    private void onSaveEnrollment() {
        if (studentId == null || termComboBox.getValue() == null) {
            showAlert("Student and term must be selected.");
            return;
        }

        List<Course> selectedCourses = new ArrayList<>(coursesTable.getItems());

        if (selectedCourses.isEmpty()) {
            showAlert("No courses to save.");
            return;
        }

        Path dir = Paths.get("students", studentId, termComboBox.getValue());
        Path file = dir.resolve("courses.csv");

        try {
            Files.createDirectories(dir);
            try (BufferedWriter writer = Files.newBufferedWriter(file)) {
                for (Course c : selectedCourses) {
                    writer.write(String.join(",", c.courseCodeProperty().get(), c.courseNameProperty().get(), c.prerequisitesProperty().get()));
                    writer.newLine();
                }
            }
            showAlert("Enrollment saved.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error saving enrollment.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notice");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}