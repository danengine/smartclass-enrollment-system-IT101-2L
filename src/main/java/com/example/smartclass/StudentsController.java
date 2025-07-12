package com.example.smartclass;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.*;
import java.lang.reflect.Array;
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
    @FXML private ComboBox<String> filterProgramCombo;
    @FXML private ComboBox<String> filterYearCombo;
    @FXML private DatePicker filterEnrolledDatePicker;
    @FXML private CheckBox showArchivedCheckBox;
    private FilteredList<Student> filteredList;

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
        filteredList = new FilteredList<>(masterList, p -> true);

        // Initialize filter controls
        filterProgramCombo.getItems().add(0, "All Programs");
        filterProgramCombo.getItems().addAll(Database.showPrograms());
        filterProgramCombo.setValue("All Programs");
        filterYearCombo.getItems().addAll("All Years", "Freshmen", "2nd Year", "3rd Year", "4th Year");
        filterYearCombo.setValue("All Years");
        filterEnrolledDatePicker.setValue(null);

        // Add showArchivedCheckBox for filtering archived students
        if (showArchivedCheckBox == null) {
            showArchivedCheckBox = new CheckBox("Show Archived");
            ((VBox) studentsTable.getParent()).getChildren().add(showArchivedCheckBox);
        }
        showArchivedCheckBox.setSelected(false);
        showArchivedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> updateStudentFilter());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateStudentFilter());

        filterProgramCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateStudentFilter());
        filterYearCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateStudentFilter());
        filterEnrolledDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> updateStudentFilter());

        studentsTable.setItems(filteredList);
        addActionButtons();
        updateStudentFilter(); // Ensure filter is applied on first load
    }

    private void updateStudentFilter() {
        String selectedProgram = filterProgramCombo.getValue();
        String selectedYear = filterYearCombo.getValue();
        LocalDate selectedDate = filterEnrolledDatePicker.getValue();
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        boolean showArchived = showArchivedCheckBox != null && showArchivedCheckBox.isSelected();
        filteredList.setPredicate(student -> {
            boolean matchesSearch = search.isEmpty() ||
                    student.getStudentId().toLowerCase().contains(search) ||
                    student.getName().toLowerCase().contains(search) ||
                    student.getCourse().toLowerCase().contains(search);
            boolean matchesProgram = selectedProgram == null || selectedProgram.equals("All Programs") || student.getCourse().equals(selectedProgram);
            boolean matchesYear = selectedYear == null || selectedYear.equals("All Years") || student.getYear().equals(selectedYear);
            boolean matchesDate = selectedDate == null || student.getDateEnrolled().equals(selectedDate.toString());
            boolean matchesArchived = showArchived || !student.isArchived();
            return matchesSearch && matchesProgram && matchesYear && matchesDate && matchesArchived;
        });
    }

    private void addActionButtons() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button archiveBtn = new Button();
            {
                editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11;");
                archiveBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-size: 11;");

                editBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    openStudentDialog(student);
                });

                archiveBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    boolean isArchived = student.isArchived();
                    String action = isArchived ? "unarchive" : "archive";
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to " + action + " this student?", ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText((isArchived ? "Unarchive" : "Archive") + " Student");
                    confirm.showAndWait().ifPresent(type -> {
                        if (type == ButtonType.YES) {
                            // Update in students.csv
                            File studentsFile = new File("students.csv");
                            java.util.List<String> allLines = new java.util.ArrayList<>();
                            if (studentsFile.exists()) {
                                try (BufferedReader br = new BufferedReader(new FileReader(studentsFile))) {
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        allLines.add(line);
                                    }
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            for (int i = 0; i < allLines.size(); i++) {
                                String line = allLines.get(i);
                                String[] arr = line.split(",", -1);
                                if (arr.length > 0 && arr[0].equals(student.getStudentId())) {
                                    // Set archived to true/false (last column)
                                    if (arr.length < 22) {
                                        // Add archived column if missing
                                        StringBuilder sb = new StringBuilder(line);
                                        sb.append(",").append(isArchived ? "false" : "true");
                                        allLines.set(i, sb.toString());
                                    } else {
                                        arr[21] = isArchived ? "false" : "true";
                                        allLines.set(i, String.join(",", arr));
                                    }
                                    break;
                                }
                            }
                            try (java.io.FileWriter fw = new java.io.FileWriter(studentsFile, false)) {
                                for (String line : allLines) {
                                    fw.write(line + "\n");
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            // Set archived in memory
                            student.setArchived(!isArchived);
                            updateStudentFilter(); // Refresh filter after archiving/unarchiving
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
                    Student student = getTableView().getItems().get(getIndex());
                    archiveBtn.setText(student.isArchived() ? "Unarchive" : "Archive");
                    HBox box = new HBox(5, editBtn, archiveBtn);
                    setGraphic(box);
                }
            }
        });
    }

    @FXML
    private void onExportCSV() {
        // File chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Students to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("students.csv");
        File file = fileChooser.showSaveDialog(studentsTable.getScene().getWindow());
        if (file == null) return; // Only continue if user selected a file

        // Column selection dialog
        List<String> allColumns = List.of("Student ID", "Name", "Course", "Year", "Date Enrolled");
        List<String> allFields = List.of("studentId", "name", "course", "year", "dateEnrolled");
        List<CheckBox> checkBoxes = new ArrayList<>();
        for (String col : allColumns) checkBoxes.add(new CheckBox(col));
        checkBoxes.forEach(cb -> cb.setSelected(true));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select Columns to Export");
        dialog.setHeaderText("Choose which columns to include in the CSV file:");
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(checkBoxes);
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResizable(false);
        dialog.showAndWait();
        if (dialog.getResult() != ButtonType.OK) return;
        List<Integer> selectedIndexes = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) selectedIndexes.add(i);
        }
        if (selectedIndexes.isEmpty()) {
            showLogoAlert("Please select at least one column to export.", Alert.AlertType.ERROR);
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            for (int i = 0; i < selectedIndexes.size(); i++) {
                writer.write(allColumns.get(selectedIndexes.get(i)));
                if (i < selectedIndexes.size() - 1) writer.write(",");
            }
            writer.write("\n");
            // Write data
            for (Student student : studentsTable.getItems()) {
                for (int i = 0; i < selectedIndexes.size(); i++) {
                    String value = switch (allFields.get(selectedIndexes.get(i))) {
                        case "studentId" -> student.getStudentId();
                        case "name" -> student.getName();
                        case "course" -> student.getCourse();
                        case "year" -> student.getYear();
                        case "dateEnrolled" -> student.getDateEnrolled();
                        default -> "";
                    };
                    writer.write(value);
                    if (i < selectedIndexes.size() - 1) writer.write(",");
                }
                writer.write("\n");
            }
            showLogoAlert("Students exported successfully to " + file.getAbsolutePath(), Alert.AlertType.INFORMATION);
        } catch (IOException e) {
            showLogoAlert("Error saving CSV file.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public static void showLogoAlert(String message, Alert.AlertType type) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("");
        dialog.setHeaderText(null);
        dialog.setResizable(false);
        dialog.initStyle(javafx.stage.StageStyle.UNDECORATED);
        DialogPane pane = dialog.getDialogPane();
        pane.setPrefSize(420, 180);
        pane.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        HBox customHeader = new HBox();
        customHeader.setStyle("-fx-background-color: #1e3d59; -fx-padding: 0; -fx-border-radius: 0; -fx-background-insets: 0;");
        customHeader.setAlignment(Pos.CENTER_LEFT);
        customHeader.setMinHeight(48);
        customHeader.setPrefHeight(48);
        customHeader.setMaxWidth(Double.MAX_VALUE);
        customHeader.setMaxHeight(48);
        customHeader.setPadding(Insets.EMPTY);
        customHeader.setBorder(Border.EMPTY);
        customHeader.setSpacing(0);
        HBox.setHgrow(customHeader, Priority.ALWAYS);
        Label customTitle = new Label("Alert");
        customTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 0 24; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 18 0 18;");
        closeBtn.setOnAction(event -> {
            dialog.setResult(ButtonType.CLOSE);
            dialog.close();
        });
        customHeader.getChildren().addAll(customTitle, spacer, closeBtn);

        // Make header moveable (drag window)
        final double[] dragOffset = new double[2];
        customHeader.setOnMousePressed(event -> {
            dragOffset[0] = event.getScreenX() - dialog.getDialogPane().getScene().getWindow().getX();
            dragOffset[1] = event.getScreenY() - dialog.getDialogPane().getScene().getWindow().getY();
        });
        customHeader.setOnMouseDragged(event -> {
            dialog.getDialogPane().getScene().getWindow().setX(event.getScreenX() - dragOffset[0]);
            dialog.getDialogPane().getScene().getWindow().setY(event.getScreenY() - dragOffset[1]);
        });

        // Message (allow wrapping for long text)
        Label msg = new Label(message);
        msg.setStyle("-fx-font-size: 15px; -fx-padding: 18 24 0 24; -fx-text-fill: #2c3e50;");
        msg.setWrapText(true);
        msg.setMaxWidth(372); // 420 - 2*24 padding
        msg.setMinHeight(Region.USE_PREF_SIZE);

        // OK button
        Button okBtn = new Button("OK");
        okBtn.setStyle("-fx-background-color: #1e3d59; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 32 6 32; -fx-cursor: hand;");
        okBtn.setDefaultButton(true);
        okBtn.setOnAction(e -> dialog.setResult(ButtonType.OK));
        HBox buttonRow = new HBox(okBtn);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        buttonRow.setPadding(new Insets(18, 24, 18, 24));

        VBox root = new VBox(customHeader, msg, buttonRow);
        root.setStyle("-fx-background-color: white; -fx-border-color: #b0b0b0; -fx-border-width: 2px; -fx-padding: 0; -fx-background-insets: 0;");
        root.setSpacing(0);
        root.setPadding(Insets.EMPTY);
        root.setMinHeight(Region.USE_COMPUTED_SIZE);
        root.setPrefHeight(Region.USE_COMPUTED_SIZE);
        root.setMaxHeight(Region.USE_PREF_SIZE);
        pane.setContent(root);
        pane.setPadding(Insets.EMPTY);
        pane.setBackground(Background.EMPTY);
        pane.setBorder(Border.EMPTY);
        pane.setPrefHeight(Region.USE_COMPUTED_SIZE);
        pane.setMinHeight(Region.USE_COMPUTED_SIZE);
        pane.setMaxHeight(Region.USE_PREF_SIZE);
        dialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialog.getDialogPane().setMinHeight(Region.USE_COMPUTED_SIZE);
        dialog.getDialogPane().setMaxHeight(Region.USE_PREF_SIZE);
        dialog.getDialogPane().setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-background-insets: 0; -fx-border-width: 0; -fx-effect: null;");
        dialog.getDialogPane().setEffect(null);
        dialog.getDialogPane().getScene().setFill(null);
        dialog.getDialogPane().getButtonTypes().clear();

        // Center the dialog on the screen after it is shown and sized
        dialog.setOnShown(e -> {
            javafx.stage.Window window = dialog.getDialogPane().getScene().getWindow();
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            // Wait for layout pass to get correct width/height
            window.sizeToScene();
            double width = window.getWidth();
            double height = window.getHeight();
            window.setX(screenBounds.getMinX() + (screenBounds.getWidth() - width) / 2);
            window.setY(screenBounds.getMinY() + (screenBounds.getHeight() - height) / 2);
        });

        dialog.show();
        // Center the dialog on the screen (same as openStudentDialog)
        javafx.stage.Window window = dialog.getDialogPane().getScene().getWindow();
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        window.setX(screenBounds.getMinX() + (screenBounds.getWidth() - window.getWidth()) / 2);
        window.setY(screenBounds.getMinY() + (screenBounds.getHeight() - window.getHeight()) / 2);
    }

    @FXML
    private void onEnrollUser() {
        openStudentDialog(null);
    }

    public void openStudentDialog(Student student) {
        Dialog<ButtonType> dialog = new Dialog<>();
        // Remove default title and header
        dialog.setTitle("");
        dialog.setHeaderText(null);
        dialog.setResizable(false);
        dialog.getDialogPane().setPrefSize(820, 400);
        DialogPane pane = dialog.getDialogPane();

        dialog.initStyle(javafx.stage.StageStyle.UNDECORATED); // Remove window decorations

        // Custom blue header bar with title and close button (no rounded corners)
        HBox customHeader = new HBox();
        customHeader.setStyle("-fx-background-color: #1e3d59; -fx-padding: 0;");
        customHeader.setAlignment(Pos.CENTER_LEFT);
        customHeader.setMinHeight(48);
        customHeader.setPrefHeight(48);
        customHeader.setMaxWidth(Double.MAX_VALUE);
        Label customTitle = new Label(student == null ? "Enroll New Student" : "Edit Student");
        customTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 0 24; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 18 0 18;");
        closeBtn.setOnAction(event -> dialog.setResult(ButtonType.CLOSE)); // Use dialog.close() to close the dialog
        customHeader.getChildren().addAll(customTitle, spacer, closeBtn);

        // Enable window dragging using the custom header
        final double[] dragOffset = new double[2];
        customHeader.setOnMousePressed(event -> {
            dragOffset[0] = event.getScreenX() - dialog.getDialogPane().getScene().getWindow().getX();
            dragOffset[1] = event.getScreenY() - dialog.getDialogPane().getScene().getWindow().getY();
        });
        customHeader.setOnMouseDragged(event -> {
            dialog.getDialogPane().getScene().getWindow().setX(event.getScreenX() - dragOffset[0]);
            dialog.getDialogPane().getScene().getWindow().setY(event.getScreenY() - dragOffset[1]);
        });

        // --- Personal Information Grid (2 columns, inside card) ---
        VBox personalCard = new VBox();
        personalCard.getStyleClass().addAll("enroll-card-no-radius", "enroll-tab-content-bg");
        personalCard.setPadding(new Insets(10, 10, 10, 10));
        personalCard.setSpacing(4);
        GridPane personalGrid = new GridPane();
        personalGrid.setHgap(18);
        personalGrid.setVgap(8);
        personalGrid.setAlignment(Pos.TOP_LEFT); // Align grid content to the left

        TextField idField = new TextField(student == null ? generateStudentId() : student.getStudentId());
        idField.setDisable(true);
        idField.setPromptText("Student ID");
        // Add LRN field after Student ID
        TextField lrnField = new TextField(student != null ? student.getLrn() : "");
        lrnField.setPromptText("LRN");
        if (student != null) lrnField.setDisable(true); // Make LRN not editable when editing
        TextField nameField = new TextField(student != null ? student.getName() : "");
        nameField.setPromptText("Full Name");
        TextField suffixField = new TextField(student != null ? student.getSuffix() : "");
        suffixField.setPromptText("Suffix (optional)");
        TextField emailField = new TextField(student != null ? student.getEmail() : "");
        emailField.setPromptText("Email");
        // Gender as ComboBox (Sex: Male, Female)
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female");
        genderCombo.setPromptText("Sex");
        if (student != null) genderCombo.setValue(student.getGender());
        TextField addressField = new TextField(student != null ? student.getAddress() : "");
        addressField.setPromptText("Address");
        TextField contactField = new TextField(student != null ? student.getContactNumber() : "");
        contactField.setPromptText("Contact No.");
        ComboBox<String> programCombo = new ComboBox<>();
        programCombo.getItems().addAll(Database.showPrograms());
        if (student != null) programCombo.setDisable(true);
        programCombo.setValue(student != null ? student.getCourse() : (programCombo.getItems().isEmpty() ? null : programCombo.getItems().get(0)));
        programCombo.setPromptText("Program");
        ComboBox<String> yearCombo = new ComboBox<>();
        yearCombo.getItems().addAll("Freshmen", "2nd Year", "3rd Year", "4th Year");
        yearCombo.setValue(student != null ? student.getYear() : "Freshmen");
        yearCombo.setPromptText("Year");
        DatePicker dateEnrolledPicker = new DatePicker(student != null
                ? LocalDate.parse(student.getDateEnrolled())
                : LocalDate.now());
        dateEnrolledPicker.setPromptText("Date Enrolled");
        dateEnrolledPicker.setDisable(student != null);

        // Arrange fields in two columns
        personalGrid.add(new Label("Student ID:"), 0, 0);
        personalGrid.add(idField, 1, 0);
        personalGrid.add(new Label("LRN:"), 2, 0);
        personalGrid.add(lrnField, 3, 0);
        personalGrid.add(new Label("Full Name:"), 0, 1);
        personalGrid.add(nameField, 1, 1);
        personalGrid.add(new Label("Suffix:"), 2, 1);
        personalGrid.add(suffixField, 3, 1);
        personalGrid.add(new Label("Email:"), 0, 2);
        personalGrid.add(emailField, 1, 2);
        personalGrid.add(new Label("Sex:"), 2, 2);
        personalGrid.add(genderCombo, 3, 2);
        personalGrid.add(new Label("Address:"), 0, 3);
        personalGrid.add(addressField, 1, 3);
        personalGrid.add(new Label("Contact No.:", null), 2, 3);
        personalGrid.add(contactField, 3, 3);
        personalGrid.add(new Label("Program:"), 0, 4);
        personalGrid.add(programCombo, 1, 4);
        personalGrid.add(new Label("Year:"), 2, 4);
        personalGrid.add(yearCombo, 3, 4);
        personalGrid.add(new Label("Date Enrolled:"), 0, 5);
        personalGrid.add(dateEnrolledPicker, 1, 5);
        // Add requirements checkboxes on the right side (column 4, spanning rows)
        VBox requirementsBox = new VBox(6);
        requirementsBox.setPadding(new Insets(0, 0, 0, 12));
        requirementsBox.getChildren().add(new Label("Requirements Submitted:"));
        CheckBox birthCertBox = new CheckBox("Birth Certificate");
        CheckBox form137Box = new CheckBox("Form 137");
        CheckBox goodMoralBox = new CheckBox("Good Moral");
        CheckBox medCertBox = new CheckBox("Medical Certificate");
        // Load checkbox states if editing
        if (student != null) {
            birthCertBox.setSelected(student.isBirthCertSubmitted());
            form137Box.setSelected(student.isForm137Submitted());
            goodMoralBox.setSelected(student.isGoodMoralSubmitted());
            medCertBox.setSelected(student.isMedCertSubmitted());
        }
        requirementsBox.getChildren().addAll(birthCertBox, form137Box, goodMoralBox, medCertBox);
        personalGrid.add(requirementsBox, 4, 0, 1, 6); // column 4, row 0, span 6 rows
        personalCard.getChildren().add(personalGrid);

        // --- Family Information Grid (2 columns, inside card) ---
        VBox familyCard = new VBox();
        familyCard.getStyleClass().addAll("enroll-card-no-radius", "enroll-tab-content-bg");
        familyCard.setPadding(new Insets(10, 10, 10, 10));
        familyCard.setSpacing(4);
        GridPane familyGrid = new GridPane();
        familyGrid.setHgap(18);
        familyGrid.setVgap(8);
        familyGrid.setAlignment(Pos.TOP_LEFT);
        TextField fatherField = new TextField(student != null ? student.getFatherName() : "");
        fatherField.setPromptText("Father's Name");
        TextField fatherContactField = new TextField(student != null ? student.getFatherContact() : "");
        fatherContactField.setPromptText("Father's Contact No.");
        TextField motherField = new TextField(student != null ? student.getMotherName() : "");
        motherField.setPromptText("Mother's Name");
        TextField motherContactField = new TextField(student != null ? student.getMotherContact() : "");
        motherContactField.setPromptText("Mother's Contact No.");
        TextField guardianField = new TextField(student != null ? student.getGuardianName() : "");
        guardianField.setPromptText("Guardian");
        TextField guardianContactField = new TextField(student != null ? student.getGuardianContact() : "");
        guardianContactField.setPromptText("Guardian's Contact No.");
        familyGrid.add(new Label("Father's Name:"), 0, 0);
        familyGrid.add(fatherField, 1, 0);
        familyGrid.add(new Label("Father's Contact No.:", null), 2, 0);
        familyGrid.add(fatherContactField, 3, 0);
        familyGrid.add(new Label("Mother's Name:"), 0, 1);
        familyGrid.add(motherField, 1, 1);
        familyGrid.add(new Label("Mother's Contact No.:", null), 2, 1);
        familyGrid.add(motherContactField, 3, 1);
        familyGrid.add(new Label("Guardian:"), 0, 2);
        familyGrid.add(guardianField, 1, 2);
        familyGrid.add(new Label("Guardian's Contact No.:", null), 2, 2);
        familyGrid.add(guardianContactField, 3, 2);
        familyCard.getChildren().add(familyGrid);

        // --- TabPane ---
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("enroll-tabpane");
        tabPane.setTabMinWidth(180);
        tabPane.setTabMaxWidth(180);
        Tab personalTab = new Tab("Personal Information", personalCard);
        Tab familyTab = new Tab("Family Information", familyCard);
        personalTab.setClosable(false);
        familyTab.setClosable(false);
        tabPane.getTabs().addAll(personalTab, familyTab);

        // Add errorLabel for validation feedback
        Label errorLabel = new Label("");
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px; -fx-padding: 0 0 8 0;");
        errorLabel.setVisible(false);

        // Save button
        Button saveBtn = new Button(student == null ? "Add Student" : "Save Changes");
        saveBtn.setDefaultButton(true);
        saveBtn.setStyle("-fx-background-color: #1e3d59; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 32 6 32; -fx-cursor: hand;");
        saveBtn.setOnAction(ev -> {
            errorLabel.setVisible(false);
            String id = idField.getText().trim();
            String lrn = lrnField.getText().trim();
            String name = nameField.getText().trim();
            String suffix = suffixField.getText().trim();
            String email = emailField.getText().trim();
            String gender = genderCombo.getValue();
            String address = addressField.getText().trim();
            String contact = contactField.getText().trim();
            String program = programCombo.getValue();
            String year = yearCombo.getValue();
            LocalDate date = dateEnrolledPicker.getValue();
            String father = fatherField.getText().trim();
            String fatherContact = fatherContactField.getText().trim();
            String mother = motherField.getText().trim();
            String motherContact = motherContactField.getText().trim();
            String guardian = guardianField.getText().trim();
            String guardianContact = guardianContactField.getText().trim();
            boolean birthCert = birthCertBox.isSelected();
            boolean form137 = form137Box.isSelected();
            boolean goodMoral = goodMoralBox.isSelected();
            boolean medCert = medCertBox.isSelected();
            // --- Validation ---
            if (lrn.isEmpty()) {
                showLogoAlert("LRN must not be empty.", Alert.AlertType.ERROR);
                return;
            }
            if (!name.matches("^[A-Za-z .,'-]+$") || name.matches(".*\\d.*")) {
                showLogoAlert("Name must not contain numbers or special characters (except . , ' -).", Alert.AlertType.ERROR);
                return;
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
                showLogoAlert("Please enter a valid email address.", Alert.AlertType.ERROR);
                return;
            }
            if (contact.length() != 11 || !contact.matches("\\d{11}")) {
                showLogoAlert("Contact No. must be exactly 11 digits.", Alert.AlertType.ERROR);
                return;
            }
            if (!fatherContact.isEmpty() && (!fatherContact.matches("\\d{11}"))) {
                showLogoAlert("Father's Contact No. must be exactly 11 digits and contain only numbers.", Alert.AlertType.ERROR);
                return;
            }
            if (!motherContact.isEmpty() && (!motherContact.matches("\\d{11}"))) {
                showLogoAlert("Mother's Contact No. must be exactly 11 digits and contain only numbers.", Alert.AlertType.ERROR);
                return;
            }
            if (!guardianContact.isEmpty() && (!guardianContact.matches("\\d{11}"))) {
                showLogoAlert("Guardian's Contact No. must be exactly 11 digits and contain only numbers.", Alert.AlertType.ERROR);
                return;
            }
            if (gender == null || (!gender.equals("Male") && !gender.equals("Female"))) {
                showLogoAlert("Please select Sex (Male or Female).", Alert.AlertType.ERROR);
                return;
            }
            if (name.isEmpty() || email.isEmpty() || program == null || year == null || date == null ||
                    gender.isEmpty() || address.isEmpty() || contact.isEmpty()) {
                showLogoAlert("Please fill out all fields.", Alert.AlertType.ERROR);
                return;
            }
            File csvFile = new File("students.csv");
            List<String> lines = new ArrayList<>();
            try {
                if (csvFile.exists()) {
                    lines = Files.readAllLines(csvFile.toPath());
                }
                String newLine = String.join(",",
                        id, lrn, name, suffix, email, gender, address, contact, program, year, date.toString(),
                        father, fatherContact, mother, motherContact, guardian, guardianContact,
                        String.valueOf(birthCert), String.valueOf(form137), String.valueOf(goodMoral), String.valueOf(medCert)
                );
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
                dialog.setResult(ButtonType.CLOSE);
                showLogoAlert("Student " + (student == null ? "added" : "updated") + " successfully!", Alert.AlertType.INFORMATION);
                initialize();
            } catch (IOException ex) {
                showLogoAlert("Failed to save student data.", Alert.AlertType.ERROR);
            }
        });

        // Remove overlay. Just use root as the dialog content.
        HBox buttonRow = new HBox(saveBtn);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);
        buttonRow.setPadding(new Insets(0, 24, 10, 0));
        saveBtn.setStyle("-fx-background-color: #1e3d59; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 32 6 32; -fx-cursor: hand; -fx-border-width: 0;");
        saveBtn.setBorder(Border.EMPTY); // Remove any border
        VBox root = new VBox(0, customHeader, tabPane, errorLabel, buttonRow);
        root.setPadding(new Insets(0, 0, 0, 0));
        root.setSpacing(6);
        root.setStyle("-fx-background-color: white; -fx-border-color: #b0b0b0; -fx-border-width: 2px;");
        root.setAlignment(Pos.TOP_LEFT); // Align content to the left
        pane.setContent(root);
        pane.setPadding(Insets.EMPTY); // Ensure no extra background/padding from DialogPane
        dialog.getDialogPane().setPrefSize(820, 440);
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-padding: 0;"); // Remove any default background/padding
        // Ensure closeBtn and saveBtn both close the dialog properly
        closeBtn.setOnAction(event -> {
            dialog.setResult(ButtonType.CLOSE);
            dialog.close();
        });

        dialog.show();
        // Center the dialog on the screen
        javafx.stage.Window window = dialog.getDialogPane().getScene().getWindow();
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        window.setX(screenBounds.getMinX() + (screenBounds.getWidth() - window.getWidth()) / 2);
        window.setY(screenBounds.getMinY() + (screenBounds.getHeight() - window.getHeight()) / 2);
    }

    private String generateStudentId() {
        String year = String.valueOf(LocalDate.now().getYear());
        int randomNum = 100000 + new Random().nextInt(900000); // 6-digit number
        return year + randomNum;
    }
}

