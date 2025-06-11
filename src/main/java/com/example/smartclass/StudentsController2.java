package com.example.smartclass;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudentsController2 {

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
                    getTableView().getItems().remove(student);
                    // TODO: Delete student from CSV file (implement if needed)
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
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(900, 700);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        String generatedId = student == null ? generateStudentId() : student.getStudentId();
        TextField idField = new TextField(generatedId);
        idField.setDisable(true);

        TextField nameField = new TextField(student != null ? student.getName() : "");
        TextField emailField = new TextField(student != null ? student.getEmail() : "");

        ComboBox<String> programCombo = new ComboBox<>();
        programCombo.getItems().addAll(Database.showPrograms());
        if (student != null) programCombo.setDisable(true);
        if (student != null) programCombo.setValue(student.getCourse());
        else if (!programCombo.getItems().isEmpty()) programCombo.setValue(programCombo.getItems().get(0));

        TextField yearField = new TextField(student != null ? student.getYear() : "");

        DatePicker dateEnrolledPicker = new DatePicker();
        if (student != null) {
            dateEnrolledPicker.setValue(LocalDate.parse(student.getDateEnrolled()));
        } else {
            dateEnrolledPicker.setValue(LocalDate.now());
        }

        grid.add(new Label("Student ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Program:"), 0, 3);
        grid.add(programCombo, 1, 3);
        grid.add(new Label("Year:"), 0, 4);
        grid.add(yearField, 1, 4);
        grid.add(new Label("Date Enrolled:"), 0, 5);
        grid.add(dateEnrolledPicker, 1, 5);

        // --- COURSE ENROLLMENT ---

        ComboBox<String> termCombo = new ComboBox<>();
        String lastTerm = null;
        try (BufferedReader br = new BufferedReader(new FileReader("terms.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                termCombo.getItems().add(trimmed);
                lastTerm = trimmed;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lastTerm != null) termCombo.setValue(lastTerm);

        TableView<Course> courseTable = new TableView<>();
        courseTable.setPlaceholder(new Label("Select a term to view courses"));
        courseTable.setPrefHeight(300);
        courseTable.setPrefWidth(700);

        TableColumn<Course, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(data -> data.getValue().courseCodeProperty());

        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(data -> data.getValue().courseNameProperty());

        courseTable.getColumns().addAll(codeCol, nameCol);
        HBox.setHgrow(courseTable, Priority.ALWAYS);

        Button enrollButton = new Button("Enroll Courses");
        enrollButton.setVisible(false);

        termCombo.valueProperty().addListener((obs, oldTerm, newTerm) -> {
            if (student != null && newTerm != null) {
                loadCoursesForTerm(newTerm, idField.getText(), courseTable, enrollButton);
            } else {
                courseTable.getItems().clear();
                enrollButton.setVisible(false);
            }
        });

        if (student != null && termCombo.getValue() != null) {
            loadCoursesForTerm(termCombo.getValue(), student.getStudentId(), courseTable, enrollButton);
        }

        enrollButton.setOnAction(e -> {
            String selectedTerm = termCombo.getValue();
            if (selectedTerm == null) {
                new Alert(Alert.AlertType.WARNING, "Please select a term first.").showAndWait();
                return;
            }
            if (student == null) {
                new Alert(Alert.AlertType.WARNING, "Student data is missing.").showAndWait();
                return;
            }

            ObservableList<Course> baseCourses = Database.getCoursesByProgram(student.getCourse());

            ListView<Course> courseList = new ListView<>(baseCourses);
            courseList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            courseList.setPrefHeight(200);

            Dialog<List<Course>> enrollDialog = new Dialog<>();
            enrollDialog.setTitle("Enroll Courses");
            enrollDialog.getDialogPane().setContent(courseList);
            enrollDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            enrollDialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    return new ArrayList<>(courseList.getSelectionModel().getSelectedItems());
                }
                return null;
            });

            enrollDialog.showAndWait().ifPresent(selectedCourses -> {
                try {
                    File dir = new File("students/" + student.getStudentId() + "/" + selectedTerm);
                    dir.mkdirs();
                    File courseFile = new File(dir, "courses.csv");
                    List<String> existingCourses = new ArrayList<>();
                    if (courseFile.exists()) {
                        try (BufferedReader br = new BufferedReader(new FileReader(courseFile))) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                existingCourses.add(line);
                            }
                        }
                    }
                    try (FileWriter writer = new FileWriter(courseFile, true)) {
                        for (Course c : selectedCourses) {
                            String line = c.courseCodeProperty().get() + "," + c.courseNameProperty().get() + ",";
                            if (!existingCourses.contains(line)) {
                                writer.write(line + "\n");
                            }
                        }
                    }
                    loadCoursesForTerm(selectedTerm, student.getStudentId(), courseTable, enrollButton);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        });

        grid.add(new Label("Select Term:"), 0, 6);
        grid.add(termCombo, 1, 6);
        grid.add(new Label("Courses:"), 0, 7);
        grid.add(courseTable, 1, 7);
        grid.add(enrollButton, 1, 8);

        // --- SAVE / ADD BUTTON ---

        ButtonType saveButtonType = new ButtonType(student == null ? "Add Student" : "Save Changes", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().add(saveButtonType);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String program = programCombo.getValue();
                String year = yearField.getText().trim();
                LocalDate date = dateEnrolledPicker.getValue();

                if (name.isEmpty() || email.isEmpty() || program == null || year.isEmpty() || date == null) {
                    new Alert(Alert.AlertType.ERROR, "Please fill out all fields.").showAndWait();
                    return null;
                }

                File csvFile = new File("students.csv");
                List<String> lines = new ArrayList<>();

                try {
                    if (csvFile.exists()) {
                        lines = Files.readAllLines(csvFile.toPath());
                    }

                    String newLine = id + "," + name + "," + email + "," + program + "," + year + "," + date;

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
                    TableColumn<Course, Void> deleteCol = new TableColumn<>("Action");
                    deleteCol.setCellFactory(col -> new TableCell<>() {
                        private final Button delBtn = new Button("Delete");

                        {
                            delBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11;");
                            delBtn.setOnAction(e -> {
                                Course course = getTableView().getItems().get(getIndex());
                                getTableView().getItems().remove(course);
                                updateCoursesCSV(studentId, term, courseTable.getItems());
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