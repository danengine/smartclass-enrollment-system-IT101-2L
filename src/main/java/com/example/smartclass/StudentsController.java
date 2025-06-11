package com.example.smartclass;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudentsController {

    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> idColumn;
    @FXML private TableColumn<Student, String> nameColumn;
    @FXML private TableColumn<Student, String> courseColumn;
    @FXML private TableColumn<Student, String> yearColumn;
    @FXML private TableColumn<Student, String> enrolledDateColumn;
    @FXML private TableColumn<Student, Void> actionColumn;
    @FXML private TextField searchField;

    private ObservableList<Student> masterList;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        courseColumn.setCellValueFactory(new PropertyValueFactory<>("course"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        enrolledDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateEnrolled"));
        studentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        masterList = Database.loadStudentsFromCSV("students.csv");

        FilteredList<Student> filteredList = new FilteredList<>(masterList, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(student -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return student.getStudentId().toLowerCase().contains(filter) ||
                        student.getName().toLowerCase().contains(filter) ||
                        student.getCourse().toLowerCase().contains(filter);
            });
        });

        studentsTable.setItems(filteredList);
        addActionButtons();
    }

    private void addActionButtons() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");

                editBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    openStudentDialog(student);
                });

                deleteBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this student?", ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText("Delete Student");
                    confirm.showAndWait().ifPresent(type -> {
                        if (type == ButtonType.YES) {
                            // Remove from students.csv
                            File studentsFile = new File("students.csv");
                            java.util.List<String> allLines = new java.util.ArrayList<>();
                            if (studentsFile.exists()) {
                                try (BufferedReader br = new BufferedReader(new FileReader(studentsFile))) {
                                    String header = br.readLine();
                                    if (header != null) allLines.add(header);
                                    br.lines().forEach(allLines::add);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            java.util.List<String> filteredLines = new java.util.ArrayList<>();
                            if (!allLines.isEmpty()) filteredLines.add(allLines.get(0));
                            for (int i = 1; i < allLines.size(); i++) {
                                String line = allLines.get(i);
                                String[] arr = line.split(",", -1);
                                if (arr.length > 0 && !arr[0].equals(student.getStudentId())) {
                                    filteredLines.add(line);
                                }
                            }
                            try (java.io.FileWriter fw = new java.io.FileWriter(studentsFile, false)) {
                                for (String line : filteredLines) {
                                    fw.write(line + "\n");
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            // Refresh the table instead of removing from filtered list
                            initialize();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, editBtn, deleteBtn);
                    setGraphic(box);
                }
            }
        });
    }

    @FXML
    private void onExportCSV() {
        String filePath = "students.csv";

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Student ID,Name,Course,Year,Date Enrolled\n");
            for (Student student : studentsTable.getItems()) {
                writer.write(student.getStudentId() + "," + student.getName() + "," + student.getCourse() + "," + student.getYear() + "," + student.getDateEnrolled() + "\n");
            }
            new Alert(Alert.AlertType.INFORMATION, "Students exported successfully to " + filePath).showAndWait();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Error saving CSV file.").showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void onEnrollUser() {
        openStudentDialog(null);
    }

    public void openStudentDialog(Student student) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(student == null ? "Enroll New Student" : "Edit Student");
        dialog.setResizable(false);
        dialog.getDialogPane().setPrefSize(380, 420);
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(24, 28, 24, 28));
        grid.setAlignment(Pos.CENTER);
        grid.setStyle(""); // No background, minimalist

        String generatedId = student == null ? generateStudentId() : student.getStudentId();
        TextField idField = new TextField(generatedId);
        idField.setDisable(true);
        idField.setMaxWidth(160);
        idField.setStyle("");

        TextField nameField = new TextField(student != null ? student.getName() : "");
        TextField suffixField = new TextField(student != null ? student.getSuffix() : "");
        nameField.setMaxWidth(160);
        suffixField.setMaxWidth(60);
        nameField.setStyle("");
        suffixField.setStyle("");

        TextField emailField = new TextField(student != null ? student.getEmail() : "");
        emailField.setMaxWidth(180);
        emailField.setStyle("");

        TextField genderField = new TextField(student != null ? student.getGender() : "");
        genderField.setMaxWidth(80);
        genderField.setStyle("");

        TextField addressField = new TextField(student != null ? student.getAddress() : "");
        addressField.setMaxWidth(180);
        addressField.setStyle("");

        TextField contactField = new TextField(student != null ? student.getContactNumber() : "");
        contactField.setMaxWidth(120);
        contactField.setStyle("");

        ComboBox<String> programCombo = new ComboBox<>();
        programCombo.getItems().addAll(Database.showPrograms());
        if (student != null) programCombo.setDisable(true);
        programCombo.setValue(student != null ? student.getCourse() : (programCombo.getItems().isEmpty() ? null : programCombo.getItems().get(0)));
        programCombo.setMaxWidth(140);
        programCombo.setStyle("");

        ComboBox<String> yearCombo = new ComboBox<>();
        yearCombo.getItems().addAll("Freshmen", "2nd Year", "3rd Year", "4th Year");
        yearCombo.setValue(student != null ? student.getYear() : "Freshmen");
        yearCombo.setMaxWidth(90);
        yearCombo.setStyle("");

        DatePicker dateEnrolledPicker = new DatePicker(student != null
                ? LocalDate.parse(student.getDateEnrolled())
                : LocalDate.now());
        dateEnrolledPicker.setMaxWidth(120);
        dateEnrolledPicker.setStyle("");

        Label titleLabel = new Label(student == null ? "Enroll New Student" : "Edit Student");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 0 0 8 0;");
        grid.add(titleLabel, 0, 0, 2, 1);

        grid.add(new Label("Student ID:"), 0, 1);
        grid.add(idField, 1, 1);

        grid.add(new Label("Name:"), 0, 2);
        grid.add(nameField, 1, 2);
        grid.add(new Label("Suffix:"), 0, 3);
        grid.add(suffixField, 1, 3);

        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);

        grid.add(new Label("Gender:"), 0, 5);
        grid.add(genderField, 1, 5);

        grid.add(new Label("Address:"), 0, 6);
        grid.add(addressField, 1, 6);

        grid.add(new Label("Contact No.:", null), 0, 7);
        grid.add(contactField, 1, 7);

        grid.add(new Label("Program:"), 0, 8);
        grid.add(programCombo, 1, 8);

        grid.add(new Label("Year:"), 0, 9);
        grid.add(yearCombo, 1, 9);

        grid.add(new Label("Date Enrolled:"), 0, 10);
        grid.add(dateEnrolledPicker, 1, 10);

        ButtonType saveButtonType = new ButtonType(student == null ? "Add Student" : "Save Changes", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().add(saveButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String suffix = suffixField.getText().trim();
                String email = emailField.getText().trim();
                String gender = genderField.getText().trim();
                String address = addressField.getText().trim();
                String contact = contactField.getText().trim();
                String program = programCombo.getValue();
                String year = yearCombo.getValue();
                LocalDate date = dateEnrolledPicker.getValue();

                if (name.isEmpty() || email.isEmpty() || program == null || year == null || date == null ||
                        gender.isEmpty() || address.isEmpty() || contact.isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Please fill out all fields.").showAndWait();
                    return null;
                }

                File csvFile = new File("students.csv");
                List<String> lines = new ArrayList<>();

                try {
                    if (csvFile.exists()) {
                        lines = Files.readAllLines(csvFile.toPath());
                    }

                    String newLine = String.join(",", id, name, suffix, email, gender, address, contact, program, year, date.toString());

                    if (student == null) {
                        lines.add(newLine);
                    } else {
                        for (int i = 0; i < lines.size(); i++) {
                            if (lines.get(i).startsWith(id + ",")) {
                                lines.set(i, newLine);
                                break;
                            }
                        }
                    }

                    Files.write(csvFile.toPath(), lines);
                    new Alert(Alert.AlertType.INFORMATION, "Student " + (student == null ? "added" : "updated") + " successfully!").showAndWait();
                    initialize();

                } catch (IOException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to save student data.").showAndWait();
                }
            }
            return null;
        });

        pane.setContent(grid);
        dialog.showAndWait();
    }

    private String generateStudentId() {
        String year = String.valueOf(LocalDate.now().getYear());
        int randomNum = 100000 + new Random().nextInt(900000); // 6-digit number
        return year + randomNum;
    }

    // Helper method to load courses for a term, show enroll button, and add delete buttons
    private void loadCoursesForTerm(String term, String studentId, TableView<Course> courseTable, Button enrollButton) {
        courseTable.getItems().clear();

        // Remove previous Action column if exists to avoid duplicates
        courseTable.getColumns().removeIf(col -> "Action".equals(col.getText()));

        if (term != null && !term.isEmpty()) {
            File courseFile = new File("students/" + studentId + "/" + term + "/courses.csv");

            if (courseFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(courseFile))) {
                    String line;
                    boolean hasCourses = false;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",", -1);
                        if (parts.length >= 2) {
                            String code = parts[0];
                            String name = parts[1];
                            courseTable.getItems().add(new Course(code, name, ""));
                            hasCourses = true;
                        }
                    }
                    enrollButton.setVisible(true); // Always visible if term selected

                    // Add Delete column with buttons
                    TableColumn<Course, Void> deleteCol = new TableColumn<>("Delete");
                    deleteCol.setCellFactory(col -> new TableCell<>() {
                        private final Button delBtn = new Button("Delete");
                        {
                            delBtn.setOnAction(e -> {
                                int row = getIndex();
                                Course course = courseTable.getItems().get(row);
                                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this course?", ButtonType.YES, ButtonType.NO);
                                confirm.setHeaderText("Delete Course");
                                confirm.showAndWait().ifPresent(type -> {
                                    if (type == ButtonType.YES) {
                                        // Remove from students.csv
                                        File studentsFile = new File("students.csv");
                                        java.util.List<String> allLines = new java.util.ArrayList<>();
                                        if (studentsFile.exists()) {
                                            try (BufferedReader br = new BufferedReader(new FileReader(studentsFile))) {
                                                String header = br.readLine();
                                                if (header != null) allLines.add(header);
                                                br.lines().forEach(allLines::add);
                                            } catch (IOException ex) {
                                                ex.printStackTrace();
                                            }
                                        }
                                        java.util.List<String> filteredLines = new java.util.ArrayList<>();
                                        if (!allLines.isEmpty()) filteredLines.add(allLines.get(0));
                                        for (int i = 1; i < allLines.size(); i++) {
                                            String line = allLines.get(i);
                                            String[] arr = line.split(",", -1);
                                            if (arr.length > 0 && !arr[0].equals(course.courseCodeProperty().get())) {
                                                filteredLines.add(line);
                                            }
                                        }
                                        try (java.io.FileWriter fw = new java.io.FileWriter(studentsFile, false)) {
                                            for (String line : filteredLines) {
                                                fw.write(line + "\n");
                                            }
                                        } catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                        // Remove from table view
                                        courseTable.getItems().remove(row);
                                    }
                                });
                            });
                        }
                        @Override
                        protected void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(delBtn);
                            }
                        }
                    });
                    courseTable.getColumns().add(deleteCol);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // No courses file yet, but show enroll button anyway
                enrollButton.setVisible(true);
            }
        } else {
            enrollButton.setVisible(false);
        }
    }

    // Update courses.csv after deletion or modification
    private void updateCoursesCSV(String studentId, String term, List<Course> courses) {
        File courseFile = new File("students/" + studentId + "/" + term + "/courses.csv");
        try (FileWriter writer = new FileWriter(courseFile, false)) {
            for (Course c : courses) {
                writer.write(c.courseCodeProperty().get() + "," + c.courseNameProperty().get() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
