package com.example.smartclass;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.chart.ChartData;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;

public class AdminDashboardController {

    @FXML
    private StackPane mainContent;

    private void setMainContent(Node newContent) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), mainContent);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(_ -> {
            mainContent.getChildren().setAll(newContent);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), mainContent);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }


    @FXML
    private void initialize() {
        showDashboard(); // Load dashboard on start
    }

    @FXML
    private void onDashboardClick() {
        showDashboard();
    }

    private void showDashboard() {
        // Get latest term from terms.csv
        String latestTerm = null;
        try (BufferedReader termReader = new BufferedReader(new FileReader("terms.csv"))) {
            String line;
            while ((line = termReader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    latestTerm = line.trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read all students
        Set<String> allStudentIds = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("students.csv"))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 1) continue;
                allStudentIds.add(parts[0].trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Read enrollments.csv and collect enrolled student IDs for the latest term only
        Set<String> enrolledIds = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("enrollments.csv"))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) continue;
                String studentId = parts[0].trim();
                String term = parts[5].trim();
                if (latestTerm != null && term.equals(latestTerm)) {
                    enrolledIds.add(studentId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int totalEnrolled = 0;
        int totalUnenrolled = 0;
        for (String id : allStudentIds) {
            if (enrolledIds.contains(id)) {
                totalEnrolled++;
            } else {
                totalUnenrolled++;
            }
        }
        int totalStudents = allStudentIds.size();

        // --- Minimalist White Card Stat Boxes ---
        String cardStyle = "-fx-background-color: #fff; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e9ecef; -fx-border-width: 1; -fx-padding: 12 18 12 18; -fx-effect: dropshadow(gaussian, #e9ecef, 4, 0.08, 0, 1);";
        // Make stat boxes smaller
        double statBoxWidth = 180; // Smaller width
        double statBoxHeight = 90; // Smaller height

        StackPane enrolledBox = new StackPane();
        enrolledBox.setStyle(cardStyle);
        enrolledBox.setPrefSize(statBoxWidth, statBoxHeight);
        VBox enrolledVBox = new VBox(6);
        enrolledVBox.setAlignment(Pos.CENTER);
        Label enrolledCount = new Label("0");
        enrolledCount.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
        Label enrolledLabel = new Label("Total Enrolled");
        enrolledLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-font-weight: normal;");
        enrolledVBox.getChildren().addAll(enrolledCount, enrolledLabel);
        enrolledBox.getChildren().add(enrolledVBox);

        StackPane unenrolledBox = new StackPane();
        unenrolledBox.setStyle(cardStyle);
        unenrolledBox.setPrefSize(statBoxWidth, statBoxHeight);
        VBox unenrolledVBox = new VBox(6);
        unenrolledVBox.setAlignment(Pos.CENTER);
        Label unenrolledCount = new Label("0");
        unenrolledCount.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
        Label unenrolledLabel = new Label("Unenrolled");
        unenrolledLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-font-weight: normal;");
        unenrolledVBox.getChildren().addAll(unenrolledCount, unenrolledLabel);
        unenrolledBox.getChildren().add(unenrolledVBox);

        StackPane totalBox = new StackPane();
        totalBox.setStyle(cardStyle);
        totalBox.setPrefSize(statBoxWidth, statBoxHeight);
        VBox totalVBox = new VBox(6);
        totalVBox.setAlignment(Pos.CENTER);
        Label totalCount = new Label("0");
        totalCount.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #495057;");
        Label totalLabel = new Label("Total Students");
        totalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d; -fx-font-weight: normal;");
        totalVBox.getChildren().addAll(totalCount, totalLabel);
        totalBox.getChildren().add(totalVBox);

        // Animate the counts
        animateCount(enrolledCount, totalEnrolled);
        animateCount(unenrolledCount, totalUnenrolled);
        animateCount(totalCount, totalStudents);

        // Enrollment by program (Donut Chart, minimalist card) - only for current term
        Map<String, Integer> programCounts = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("enrollments.csv"))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) continue;
                String program = parts[2].trim();
                String status = parts[4].trim();
                String term = parts[5].trim();
                if ("Enrolled".equalsIgnoreCase(status) && latestTerm != null && term.equals(latestTerm)) {
                    programCounts.put(program, programCounts.getOrDefault(program, 0) + 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<ChartData> chartDataList = new ArrayList<>();
        Color[] colors = {Tile.BLUE, Tile.GREEN, Tile.ORANGE, Tile.RED, Tile.GRAY};
        int colorIdx = 0;
        for (var entry : programCounts.entrySet()) {
            chartDataList.add(new ChartData(entry.getKey(), entry.getValue(), colors[colorIdx % colors.length]));
            colorIdx++;
        }
        Tile enrollmentTile = TileBuilder.create()
                .skinType(Tile.SkinType.DONUT_CHART)
                .title("Enrollment by Program")
                .text("")
                .animated(true)
                .chartData(chartDataList.toArray(new ChartData[0]))
                .titleColor(Color.web("#212529"))
                .textColor(Color.web("#212529"))
                .valueColor(Color.web("#212529"))
                .backgroundColor(Color.web("#fff"))
                .roundedCorners(true)
                .prefSize(800, 500) // Medium chart size
                .build();
        StackPane donutCard = new StackPane(enrollmentTile);
        donutCard.setStyle(cardStyle);
        donutCard.setPadding(new Insets(0));
        donutCard.setPrefSize(800, 500);

        // --- Grid Layout: 3 stat boxes on top, chart below ---
        GridPane grid = new GridPane();
        grid.setHgap(16); // Less horizontal gap
        grid.setVgap(16); // Less vertical gap
        grid.setPadding(new Insets(24, 24, 24, 0)); // Less padding
        grid.add(totalBox, 0, 0);
        grid.add(enrolledBox, 1, 0);
        grid.add(unenrolledBox, 2, 0);
        grid.add(donutCard, 0, 1, 3, 1); // chart spans all 3 columns
        GridPane.setHalignment(totalBox, javafx.geometry.HPos.LEFT);
        GridPane.setHalignment(enrolledBox, javafx.geometry.HPos.LEFT);
        GridPane.setHalignment(unenrolledBox, javafx.geometry.HPos.LEFT);
        GridPane.setHalignment(donutCard, javafx.geometry.HPos.LEFT); // Align chart left

        Label statsTitle = new Label("Statistics");
        statsTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #212529; -fx-padding: 0 0 0 0;"); // Increased bottom padding for gap

        VBox dashboardLayout = new VBox(8, statsTitle, grid);
        dashboardLayout.setPadding(new Insets(24, 24, 24, 24));
        dashboardLayout.setAlignment(Pos.TOP_LEFT);
        dashboardLayout.setStyle("-fx-background-color: #f8fafc;");
        dashboardLayout.setPrefHeight(700); // Smaller height
        dashboardLayout.setPrefWidth(1200); // Smaller width
        setMainContent(dashboardLayout);
    }


    // Other nav actions
    @FXML
    private void onManageStudentsClick() {
        loadView("students-table.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            Node node = FXMLLoader.load(getClass().getResource(fxmlFile));
            setMainContent(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onManageProgramsClick() {
        showProgramsTable();
    }

    private void showProgramsTable() {
        // --- Card Layout for Programs List ---
        StackPane rootPane = new StackPane();
        rootPane.setStyle("-fx-background-color: #f4f6f7;");

        VBox outerVBox = new VBox(20);
        outerVBox.setAlignment(Pos.TOP_CENTER);
        outerVBox.setStyle("-fx-padding: 36 32 36 32;");

        VBox card = new VBox(16);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-color: #dcdde1; -fx-border-radius: 12; -fx-padding: 24 24 24 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 12, 0, 0, 4);");

        // Title
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("🎓 Programs List");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleBox.getChildren().add(titleLabel);

        // TableView
        TableView<String> programTable = new TableView<>();
        programTable.setPrefHeight(340);
        programTable.setStyle("-fx-background-radius: 0;");
        TableColumn<String, String> programCol = new TableColumn<>("Program");
        programCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        programCol.setPrefWidth(500);
        programTable.getColumns().add(programCol);
        ObservableList<String> programs = Database.showPrograms();
        programTable.setItems(programs);
        programTable.setRowFactory(tv -> {
            TableRow<String> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    String selectedProgram = row.getItem();
                    openCoursesWindow(selectedProgram);
                }
            });
            return row;
        });

        // Button design (copy exact design from Enroll Student button in onEnrollmentsClick)
        Button createProgramButton = new Button("➕ Create Program");
        createProgramButton.setPrefWidth(140);
        createProgramButton.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
        createProgramButton.setOnAction(e -> {
            Stage dialog = new Stage();
            dialog.setTitle("");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(javafx.stage.StageStyle.UNDECORATED);

            // --- Custom Header (copied from enrollStudent) ---
            HBox customHeader = new HBox();
            customHeader.setStyle("-fx-background-color: #1e3d59; -fx-padding: 0; -fx-border-color: #b0b0b0; -fx-border-width: 0 0 1 0;");
            customHeader.setAlignment(Pos.CENTER_LEFT);
            customHeader.setMinHeight(48);
            customHeader.setPrefHeight(48);
            customHeader.setMaxWidth(Double.MAX_VALUE);
            Label customTitle = new Label("Add Program");
            customTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 0 24; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Button closeBtn = new Button("✕");
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 18 0 18;");
            closeBtn.setOnAction(event -> dialog.close());
            customHeader.getChildren().addAll(customTitle, spacer, closeBtn);
            // Enable window dragging using the custom header
            final double[] dragOffset = new double[2];
            customHeader.setOnMousePressed(event -> {
                dragOffset[0] = event.getScreenX() - dialog.getX();
                dragOffset[1] = event.getScreenY() - dialog.getY();
            });
            customHeader.setOnMouseDragged(event -> {
                dialog.setX(event.getScreenX() - dragOffset[0]);
                dialog.setY(event.getScreenY() - dragOffset[1]);
            });

            VBox root = new VBox(0);
            root.setStyle("-fx-background-color: white; -fx-border-color: #b0b0b0; -fx-border-width: 2;");
            root.getChildren().add(customHeader);
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setStyle("");

            Label label = new Label("Enter new program name:");
            TextField nameField = new TextField();
            nameField.setPromptText("Program Name");
            nameField.setPrefWidth(300);
            nameField.setStyle("-fx-font-size: 14px;");
            Button addBtn = new Button("Add Program");
            addBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 24 8 24;");
            addBtn.setOnAction(ev -> {
                String name = nameField.getText().trim();
                if (!name.isEmpty()) {
                    Database.addProgram(name);
                    programTable.setItems(Database.showPrograms());
                    dialog.close();
                }
            });
            content.getChildren().addAll(label, nameField, addBtn);
            root.getChildren().add(content);
            Scene scene = new Scene(root, 420, 200);
            dialog.setScene(scene);
            dialog.setOnShown(ev -> dialog.centerOnScreen());
            dialog.showAndWait();
        });
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        buttonBox.getChildren().addAll(buttonSpacer, createProgramButton);

        Button deleteBtn = new Button("Delete Program");
        deleteBtn.setPrefWidth(140);
        deleteBtn.setStyle("-fx-background-color: #636e72; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
        deleteBtn.setDisable(true);
        programTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            deleteBtn.setDisable(newSel == null);
        });
        deleteBtn.setOnAction(e -> {
            String selected = programTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this program? This will also delete all its courses.", ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText("Delete Program");
                confirm.showAndWait().ifPresent(type -> {
                    if (type == ButtonType.YES) {
                        Database.deleteProgram(selected);
                        programTable.setItems(Database.showPrograms());
                    }
                });
            }
        });
        buttonBox.getChildren().add(deleteBtn);

        card.getChildren().addAll(titleBox, programTable, buttonBox);
        outerVBox.getChildren().add(card);
        rootPane.getChildren().add(outerVBox);

        setMainContent(rootPane);
    }

    private <T> TableColumn<Course, T> createEditableColumn(String title,
                                                            Function<Course, ObservableValue<T>> prop, double width,
                                                            Callback<TableColumn<Course, T>, TableCell<Course, T>> cellFactory) {
        TableColumn<Course, T> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> prop.apply(cellData.getValue()));
        col.setCellFactory(cellFactory);
        col.setOnEditCommit(event -> {
            Course course = event.getRowValue();
            if (col.getText().equals("Course Code")) {
                course.courseCodeProperty().set(event.getNewValue().toString());
            } else if (col.getText().equals("Course Name")) {
                course.courseNameProperty().set(event.getNewValue().toString());
            } else if (col.getText().equals("Prerequisites")) {
                course.prerequisitesProperty().set(event.getNewValue().toString());
            }
        });
        col.setPrefWidth(width);
        return col;
    }


    private void openCoursesWindow(String program) {
        Stage courseStage = new Stage();
        courseStage.setTitle("");
        courseStage.initModality(Modality.APPLICATION_MODAL);
        courseStage.initStyle(javafx.stage.StageStyle.UNDECORATED); // Remove window decorations

        // --- Custom Header (copied from enrollStudent) ---
        HBox customHeader = new HBox();
        customHeader.setStyle("-fx-background-color: #1e3d59; -fx-padding: 0; -fx-border-color: #b0b0b0; -fx-border-width: 0 0 1 0;");
        customHeader.setAlignment(Pos.CENTER_LEFT);
        customHeader.setMinHeight(48);
        customHeader.setPrefHeight(48);
        customHeader.setMaxWidth(Double.MAX_VALUE);
        Label customTitle = new Label(program + " Courses");
        customTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 0 24; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 18 0 18;");
        closeBtn.setOnAction(event -> courseStage.close());
        customHeader.getChildren().addAll(customTitle, spacer, closeBtn);
        // Enable window dragging using the custom header
        final double[] dragOffset = new double[2];
        customHeader.setOnMousePressed(event -> {
            dragOffset[0] = event.getScreenX() - courseStage.getX();
            dragOffset[1] = event.getScreenY() - courseStage.getY();
        });
        customHeader.setOnMouseDragged(event -> {
            courseStage.setX(event.getScreenX() - dragOffset[0]);
            courseStage.setY(event.getScreenY() - dragOffset[1]);
        });

        VBox root = new VBox(0); // No gap between header and content
        root.setStyle("-fx-background-color: white; -fx-border-color: #b0b0b0; -fx-border-width: 2;");
        root.getChildren().add(customHeader);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("");

        // TableView
        TableView<Course> courseTable = new TableView<>();
        courseTable.setEditable(true);

        TableColumn<Course, Integer> unitsCol = createEditableColumn("Units", c -> c.unitsProperty().asObject(), 80,
                TextFieldTableCell.<Course, Integer>forTableColumn(new IntegerStringConverter()));
        unitsCol.setOnEditCommit(event -> {
            Course course = event.getRowValue();
            course.setUnits(event.getNewValue());
            Database.saveCoursesForProgram(program, courseTable.getItems());
        });
        courseTable.getColumns().addAll(
                createEditableColumn("Course Code", Course::courseCodeProperty, 120,
                        TextFieldTableCell.forTableColumn()),
                createEditableColumn("Course Name", Course::courseNameProperty, 200,
                        TextFieldTableCell.forTableColumn()),
                unitsCol
        );


        ObservableList<Course> courseList = Database.getCoursesByProgram(program);
        courseTable.setItems(courseList);

        Button addCourseBtn = new Button("+ Add Course");
        addCourseBtn.setOnAction(e -> {
            Dialog<Course> dialog = new Dialog<>();
            dialog.setTitle("Add New Course");

            GridPane grid = new GridPane();
            TextField codeField = new TextField();
            TextField nameField = new TextField();
            TextField unitsField = new TextField();

            grid.add(new Label("Code:"), 0, 0);
            grid.add(codeField, 1, 0);
            grid.add(new Label("Name:"), 0, 1);
            grid.add(nameField, 1, 1);
            grid.add(new Label("Units:"), 0, 2);
            unitsField.setPromptText("0");
            grid.add(unitsField, 1, 2);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    int units = 0;
                    try { units = Integer.parseInt(unitsField.getText().trim()); } catch (Exception ignored) {}
                    Course course = new Course(codeField.getText(), nameField.getText(), "", units, "N/A");
                    Database.addCourseToProgram(program, course);
                    return course;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(course -> {
                courseList.add(course);
            });
        });

        Button deleteCourseBtn = new Button("Delete Course");
        deleteCourseBtn.setDisable(true);
        deleteCourseBtn.setOnAction(e -> {
            Course selected = courseTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this course?", ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText("Delete Course");
                confirm.showAndWait().ifPresent(type -> {
                    if (type == ButtonType.YES) {
                        courseList.remove(selected);
                        Database.saveCoursesForProgram(program, courseList);
                    }
                });
            }
        });
        courseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            deleteCourseBtn.setDisable(newSel == null);
        });

        Button uploadPdfBtn = new Button("Upload from PDF (AI)");
        uploadPdfBtn.setOnAction(e -> {
            Stage uploadStage = new Stage();
            uploadStage.setTitle("Upload PDF and Extract Courses");
            uploadStage.initModality(Modality.APPLICATION_MODAL);

            GridPane grid = new GridPane();
            grid.setPadding(new Insets(20));
            grid.setHgap(12);
            grid.setVgap(10);
            grid.setStyle("-fx-background-color: white;");

            Label uploadLabel = new Label("Select a PDF file to extract courses:");
            Button selectFileBtn = new Button("Choose File");
            Label fileNameLabel = new Label("");
            TableView<String[]> extractedTable = new TableView<>();
            extractedTable.setPrefHeight(220);
            extractedTable.setPrefWidth(500);
            String[] pdfHeaders = {"Code", "Name", "Units"};
            for (int i = 0; i < pdfHeaders.length; i++) {
                final int idx = i;
                TableColumn<String[], String> col = new TableColumn<>(pdfHeaders[i]);
                col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[idx]));
                col.setPrefWidth(150);
                col.setCellFactory(TextFieldTableCell.forTableColumn());
                col.setOnEditCommit(ev -> {
                    String[] row = ev.getRowValue();
                    row[idx] = ev.getNewValue();
                    extractedTable.refresh();
                });
                extractedTable.getColumns().add(col);
            }
            extractedTable.setEditable(true);
            ObservableList<String[]> extractedData = FXCollections.observableArrayList();
            extractedTable.setItems(extractedData);

            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            final File[] selectedFile = {null};

            Button generateBtn = new Button("Generate");
            generateBtn.setDisable(true);
            ProgressIndicator loading = new ProgressIndicator();
            loading.setVisible(false);
            loading.setPrefSize(32, 32);
            Button addBtn = new Button("Add");
            addBtn.setDisable(true);

            selectFileBtn.setOnAction(ev -> {
                File file = fileChooser.showOpenDialog(uploadStage);
                if (file != null) {
                    selectedFile[0] = file;
                    fileNameLabel.setText(file.getName());
                    generateBtn.setDisable(false);
                }
            });

            generateBtn.setOnAction(ev -> {
                if (selectedFile[0] != null) {
                    loading.setVisible(true);
                    generateBtn.setDisable(true);
                    addBtn.setDisable(true);
                    new Thread(() -> {
                        try {
                            String response = HttpUtils.uploadPdfAndGetCourses(selectedFile[0], "http://127.0.0.1:3000/analyze-program-pdf");
                            // Parse JSON response
                            org.json.JSONObject obj = new org.json.JSONObject(response);
                            org.json.JSONArray arr = obj.getJSONArray("courses");
                            ObservableList<String[]> parsed = FXCollections.observableArrayList();
                            for (int i = 0; i < arr.length(); i++) {
                                org.json.JSONObject c = arr.getJSONObject(i);
                                String code = c.optString("courseCode", "");
                                String name = c.optString("courseName", "").replace("\n", " ").replace("\r", " ").trim();
                                String units = String.valueOf(c.optInt("creditUnits", 0));
                                parsed.add(new String[]{code, name, units});
                            }
                            javafx.application.Platform.runLater(() -> {
                                extractedData.setAll(parsed);
                                loading.setVisible(false);
                                generateBtn.setDisable(false);
                                addBtn.setDisable(false);
                            });
                        } catch (Exception ex) {
                            javafx.application.Platform.runLater(() -> {
                                loading.setVisible(false);
                                generateBtn.setDisable(false);
                                addBtn.setDisable(true);
                                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to analyze PDF: " + ex.getMessage());
                                alert.showAndWait();
                            });
                        }
                    }).start();
                }
            });

            addBtn.setOnAction(ev -> {
                for (String[] row : extractedData) {
                    boolean exists = courseList.stream().anyMatch(c -> c.courseCodeProperty().get().equals(row[0]));
                    if (!exists) {
                        int units = 0;
                        try { units = Integer.parseInt(row[2]); } catch (Exception ignored) {}
                        courseList.add(new Course(row[0], row[1], "", units, "N/A"));
                    }
                }
                uploadStage.close();
            });

            grid.add(uploadLabel, 0, 0, 2, 1);
            grid.add(selectFileBtn, 0, 1);
            grid.add(fileNameLabel, 1, 1);
            grid.add(generateBtn, 0, 2);
            grid.add(loading, 1, 2);
            grid.add(extractedTable, 0, 3, 2, 1);
            grid.add(addBtn, 1, 4);

            Scene uploadScene = new Scene(grid, 540, 420);
            uploadStage.setScene(uploadScene);
            uploadStage.showAndWait();
        });

        HBox buttonBox = new HBox(10, addCourseBtn, deleteCourseBtn, uploadPdfBtn);
        content.getChildren().addAll(new Label("Courses in " + program), courseTable, buttonBox);
        root.getChildren().add(content);

        courseStage.setScene(new Scene(root, 700, 450));
        courseStage.initModality(Modality.APPLICATION_MODAL);
        courseStage.show();

        courseStage.setOnCloseRequest(event -> {
            Database.saveCoursesForProgram(program, courseList);
        });
    }

    private <T> TableColumn<Course, T> createColumn(String title, Function<Course, ObservableValue<T>> prop, double width) {
        TableColumn<Course, T> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> prop.apply(data.getValue()));
        col.setPrefWidth(width);
        return col;
    }

    @FXML
    private void onEnrollmentsClick() {
        // --- Card Layout for Enrollment List ---
        StackPane rootPane = new StackPane();
        rootPane.setStyle("-fx-background-color: #f4f6f7;");

        VBox outerVBox = new VBox(20);
        outerVBox.setAlignment(Pos.TOP_CENTER);
        outerVBox.setStyle("-fx-padding: 36 32 36 32;");

        VBox card = new VBox(16);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-color: #dcdde1; -fx-border-radius: 12; -fx-padding: 24 24 24 24; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 12, 0, 0, 4);");

        // Title
        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLabel = new Label("📋 Enrollment List");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleBox.getChildren().add(titleLabel);

        // Filters (Term ComboBox + Search)
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        Label filterLabel = new Label("Filter by:");
        filterLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #636e72;");
        ComboBox<String> termBox = new ComboBox<>();
        termBox.setPromptText("Term");
        termBox.setPrefWidth(140);
        termBox.setStyle("-fx-background-radius: 8; -fx-font-size: 13px;");
        File termsFile = new File("terms.csv");
        if (termsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(termsFile))) {
                java.util.List<String> terms = new java.util.ArrayList<>();
                br.lines().forEach(terms::add);
                termBox.getItems().addAll(terms);
                if (!terms.isEmpty()) {
                    termBox.setValue(terms.get(terms.size() - 1));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        TextField searchField = new TextField();
        searchField.setPromptText("Search by ID or Name...");
        searchField.setPrefWidth(220);
        searchField.setStyle("-fx-background-radius: 8; -fx-padding: 6 10;");
        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        filterBox.getChildren().addAll(filterLabel, termBox, searchField, filterSpacer);

        // TableView
        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(340);
        table.setStyle("-fx-background-radius: 0;");
        String[] headers = {"Student ID", "Student Name", "Program", "Year", "Status"};
        for (int i = 0; i < headers.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[idx]));
            col.setPrefWidth(150);
            table.getColumns().add(col);
        }
        ObservableList<String[]> data = FXCollections.observableArrayList();
        FilteredList<String[]> filtered = new FilteredList<>(data, s -> true);
        table.setItems(filtered);
        // Double-click opens showStudentEnrollmentDialog
        table.setRowFactory(tv -> {
            TableRow<String[]> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    String[] studentRow = row.getItem();
                    String term = termBox.getValue();
                    // Compose a student array for showStudentEnrollmentDialog
                    // studentRow: [Student ID, Student Name, Program, Year, Status]
                    // Find full student info from students.csv
                    String[] studentInfo = null;
                    File studentsFile = new File("students.csv");
                    if (studentsFile.exists()) {
                        try (BufferedReader br = new BufferedReader(new FileReader(studentsFile))) {
                            br.readLine(); // skip header
                            String line;
                            while ((line = br.readLine()) != null) {
                                String[] arr = line.split(",", -1);
                                if (arr.length > 0 && arr[0].equals(studentRow[0])) {
                                    studentInfo = arr;
                                    break;
                                }
                            }
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                    if (studentInfo != null) {
                        // Compose the expected array for showStudentEnrollmentDialog
                        // [ID, Name, Suffix, Email, Gender, Address, Contact, Program, Year, DateEnrolled]
                        String[] dialogArr = new String[10];
                        dialogArr[0] = studentInfo[0];
                        dialogArr[1] = studentInfo.length > 2 ? studentInfo[2] : studentRow[1];
                        dialogArr[2] = studentInfo.length > 3 ? studentInfo[3] : "";
                        dialogArr[3] = studentInfo.length > 4 ? studentInfo[4] : "";
                        dialogArr[4] = studentInfo.length > 5 ? studentInfo[5] : "";
                        dialogArr[5] = studentInfo.length > 6 ? studentInfo[6] : "";
                        dialogArr[6] = studentInfo.length > 7 ? studentInfo[7] : "";
                        dialogArr[7] = studentInfo.length > 8 ? studentInfo[8] : studentRow[2];
                        dialogArr[8] = studentInfo.length > 9 ? studentInfo[9] : studentRow[3];
                        dialogArr[9] = studentInfo.length > 10 ? studentInfo[10] : "";
                        showStudentEnrollmentDialog(dialogArr, term, null);
                    } else {
                        // Fallback: use what we have
                        String[] fallbackArr = new String[10];
                        fallbackArr[0] = studentRow[0];
                        fallbackArr[1] = studentRow[1];
                        fallbackArr[7] = studentRow[2];
                        fallbackArr[8] = studentRow[3];
                        showStudentEnrollmentDialog(fallbackArr, term, null);
                    }
                }
            });
            return row;
        });

        // Load enrollments for selected term
        Runnable refreshTable = () -> {
            data.clear();
            File enrollmentsFile = new File("enrollments.csv");
            String selectedTerm = termBox.getValue();
            if (enrollmentsFile.exists() && selectedTerm != null) {
                try (BufferedReader br = new BufferedReader(new FileReader(enrollmentsFile))) {
                    String header = br.readLine(); // skip header
                    br.lines()
                            .map(line -> line.split(",", -1))
                            .filter(arr -> arr.length >= 6 && arr[5].equals(selectedTerm))
                            .forEach(arr -> data.add(new String[]{arr[0], arr[1], arr[2], arr[3], arr[4]}));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        // Filter by search
        searchField.textProperty().addListener((obs, old, val) -> {
            String search = val.toLowerCase();
            filtered.setPredicate(arr -> arr[0].toLowerCase().contains(search) || arr[1].toLowerCase().contains(search));
        });
        // Refresh table when term changes
        termBox.valueProperty().addListener((obs, old, val) -> refreshTable.run());
        refreshTable.run();

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        Button enrollBtn = new Button("➕ Enroll Student");
        enrollBtn.setPrefWidth(140);
        enrollBtn.setStyle("-fx-background-color: #0984e3; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
        enrollBtn.setOnAction(e -> enrollStudent(termBox.getValue(), refreshTable));
        Button deleteBtn = new Button("Delete Student");
        deleteBtn.setPrefWidth(140);
        deleteBtn.setStyle("-fx-background-color: #636e72; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold;");
        deleteBtn.setDisable(true);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            deleteBtn.setDisable(newSel == null);
        });
        deleteBtn.setOnAction(e -> {
            String[] selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this student from enrollments? This will also delete their data for this term.", ButtonType.YES, ButtonType.NO);
                confirm.setHeaderText("Delete Student Enrollment");
                confirm.showAndWait().ifPresent(type -> {
                    if (type == ButtonType.YES) {
                        String studentId = selected[0];
                        String term = termBox.getValue();
                        // Remove from enrollments.csv
                        File enrollmentsFile = new File("enrollments.csv");
                        java.util.List<String> allLines = new java.util.ArrayList<>();
                        if (enrollmentsFile.exists()) {
                            try (BufferedReader br = new BufferedReader(new FileReader(enrollmentsFile))) {
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
                            if (arr.length >= 6) {
                                if (!(arr[0].equals(studentId) && arr[5].equals(term))) {
                                    filteredLines.add(line);
                                }
                            } else if (!line.trim().isEmpty()) {
                                filteredLines.add(line);
                            }
                        }
                        try (java.io.FileWriter fw = new java.io.FileWriter(enrollmentsFile, false)) {
                            for (String line : filteredLines) {
                                fw.write(line + "\n");
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        // Delete student/{studentid}/{term} folder
                        File termDir = new File("students/" + studentId + "/" + term);
                        if (termDir.exists() && termDir.isDirectory()) {
                            deleteDirectory(termDir);
                        }
                        refreshTable.run();
                    }
                });
            }
        });
        buttonBox.getChildren().addAll(buttonSpacer, enrollBtn, deleteBtn);

        // Add all to card
        card.getChildren().addAll(titleBox, filterBox, table, buttonBox);
        outerVBox.getChildren().add(card);
        rootPane.getChildren().add(outerVBox);
        setMainContent(rootPane);
    }

    // Helper to delete a directory recursively
    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteDirectory(child);
                }
            }
        }
        dir.delete();
    }

    private void enrollStudent(String selectedTerm, Runnable refreshTable) {
        Stage dialog = new Stage();
        dialog.setTitle("");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(javafx.stage.StageStyle.UNDECORATED); // Remove window decorations

        // --- Custom Header (copied and adapted from StudentsController) ---
        HBox customHeader = new HBox();
        customHeader.setStyle("-fx-background-color: #1e3d59; -fx-padding: 0; -fx-border-color: #b0b0b0; -fx-border-width: 0 0 1 0;");
        customHeader.setAlignment(Pos.CENTER_LEFT);
        customHeader.setMinHeight(48);
        customHeader.setPrefHeight(48);
        customHeader.setMaxWidth(Double.MAX_VALUE);
        Label customTitle = new Label("Search Student to Enroll");
        customTitle.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 0 0 0 24; -fx-font-family: 'Segoe UI', Arial, sans-serif;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 18 0 18;");
        closeBtn.setOnAction(event -> dialog.close());
        customHeader.getChildren().addAll(customTitle, spacer, closeBtn);
        // Enable window dragging using the custom header
        final double[] dragOffset = new double[2];
        customHeader.setOnMousePressed(event -> {
            dragOffset[0] = event.getScreenX() - dialog.getX();
            dragOffset[1] = event.getScreenY() - dialog.getY();
        });
        customHeader.setOnMouseDragged(event -> {
            dialog.setX(event.getScreenX() - dragOffset[0]);
            dialog.setY(event.getScreenY() - dragOffset[1]);
        });

        VBox root = new VBox(0); // No gap between header and content
        root.setStyle("-fx-background-color: white; -fx-border-color: #b0b0b0; -fx-border-width: 2;");
        root.getChildren().add(customHeader);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by ID or Name...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-font-size: 14px;");

        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(200);
        table.setPrefWidth(700);
        String[] headers = {"ID", "Name", "Suffix", "Email", "Gender", "Address", "Contact", "Program", "Year", "DateEnrolled"};
        for (int i = 0; i < headers.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[idx]));
            col.setPrefWidth(90);
            table.getColumns().add(col);
        }

        ObservableList<String[]> students = FXCollections.observableArrayList();
        File studentsFile = new File("students.csv");
        if (studentsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(studentsFile))) {
                br.readLine(); // skip header
                br.lines()
                        .map(line -> line.split(",", -1))
                        .forEach(arr -> {
                            // Ignore index 1 (LRN)
                            String[] filtered = new String[arr.length - 1];
                            filtered[0] = arr[0]; // ID
                            for (int i = 2; i < arr.length; i++) {
                                filtered[i - 1] = arr[i];
                            }
                            students.add(filtered);
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FilteredList<String[]> filtered = new FilteredList<>(students, s -> true);
        table.setItems(filtered);

        searchField.textProperty().addListener((obs, old, val) -> {
            String search = val.toLowerCase();
            filtered.setPredicate(arr -> arr[0].toLowerCase().contains(search) || arr[1].toLowerCase().contains(search));
        });

        Button enrollBtn = new Button("Enroll Student");
        enrollBtn.setStyle("-fx-font-size: 14px; -fx-padding: 8 24 8 24;");
        enrollBtn.setDisable(true);

        final String[] selectedStudent = {null};

        table.setRowFactory(tv -> {
            TableRow<String[]> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    selectedStudent[0] = row.getItem()[0];
                    enrollBtn.setDisable(false);
                }
            });
            return row;
        });

        enrollBtn.setOnAction(e -> {
            if (selectedStudent[0] != null && selectedTerm != null) {
                String[] student = students.stream().filter(s -> s[0].equals(selectedStudent[0])).findFirst().orElse(null);
                if (student != null) {
                    dialog.close();
                    javafx.application.Platform.runLater(() -> showStudentEnrollmentDialog(student, selectedTerm, refreshTable));
                }
            }
        });

        content.getChildren().addAll(
                new Label("Search and Select Student"),
                searchField,
                table,
                enrollBtn
        );
        root.getChildren().add(content);

        Scene scene = new Scene(root, 820, 400);
        dialog.setScene(scene);
        // Center the dialog on the screen
        dialog.setOnShown(e -> {
            dialog.centerOnScreen();
        });
        dialog.showAndWait();
    }

    private void showStudentEnrollmentDialog(String[] student, String selectedTerm, Runnable refreshTable) {
        Stage dialog = new Stage();
        dialog.setTitle("Manage Enrollment for " + student[1]);
        dialog.initModality(Modality.APPLICATION_MODAL);

        dialog.setMaximized(true);

        VBox root = new VBox(18);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #f8fafc;");

        Label titleLabel = new Label("Student Enrollment");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label studentLabel = new Label("Student: " + student[1] + " (" + student[0] + ")");
        Label programLabel = new Label("Program: " + student[7]);

        ComboBox<String> termBox = new ComboBox<>();
        termBox.setPromptText("Select Term");
        termBox.setPrefWidth(260);

        File termsFile = new File("terms.csv");
        ObservableList<String[]> availableCourses = FXCollections.observableArrayList();
        ObservableList<String[]> enrolledCourses = FXCollections.observableArrayList();
        FilteredList<String[]> filteredAvailableCourses = new FilteredList<>(availableCourses, ac -> true);

        Set<String> allEnrolledCourseCodes = new HashSet<>();
        if (student[0] != null) {
            File studentDir = new File("students/" + student[0]);
            if (studentDir.exists() && studentDir.isDirectory()) {
                File[] termDirs = studentDir.listFiles(File::isDirectory);
                if (termDirs != null) {
                    for (File termDir : termDirs) {
                        File coursesFile = new File(termDir, "courses.csv");
                        if (coursesFile.exists()) {
                            try (BufferedReader br = new BufferedReader(new FileReader(coursesFile))) {
                                br.readLine(); // skip header
                                br.lines().forEach(line -> {
                                    String[] parts = line.split(",", -1);
                                    if (parts.length > 1) {
                                        allEnrolledCourseCodes.add(parts[0]);
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        // --- Collect all enrollments from enrollments.csv for duplicate check ---
        Set<String> allEnrollmentKeys = new HashSet<>();
        File enrollmentsFile = new File("enrollments.csv");
        if (enrollmentsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(enrollmentsFile))) {
                br.readLine(); // skip header
                br.lines().forEach(line -> {
                    String[] arr = line.split(",", -1);
                    if (arr.length >= 7) {
                        allEnrollmentKeys.add(arr[0] + "," + arr[5] + "," + arr[6]);
                    }
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // Helper to save enrollments for a term
        Runnable saveEnrollments = () -> {
            String term = termBox.getValue();
            if (student != null && term != null) {
                File enrolledFile = new File("students/" + student[0] + "/" + term + "/courses.csv");
                enrolledFile.getParentFile().mkdirs();
                try (FileWriter fw = new FileWriter(enrolledFile, false)) {
                    fw.write("Code,Name,Units,Section,Schedule\n");
                    for (String[] course : enrolledCourses) {
                        fw.write(course[0] + "," + course[1] + "," + (course.length > 2 ? course[2] : "") + "," + (course.length > 3 ? course[3] : "") + "," + (course.length > 4 ? course[4] : "") + "\n");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                List<String> allLines = new ArrayList<>();
                // Read all lines, skip header
                if (enrollmentsFile.exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(enrollmentsFile))) {
                        String header = br.readLine();
                        if (header != null) allLines.add(header);
                        br.lines().forEach(allLines::add);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                // Remove all lines for this student and this term only
                String sid = student[0];
                List<String> filtered = new ArrayList<>();
                String header = allLines.isEmpty() ? "Student ID,Student Name,Program,Year,Status,Term" : allLines.get(0);
                filtered.add(header);
                for (int i = 1; i < allLines.size(); i++) {
                    String line = allLines.get(i);
                    String[] arr = line.split(",", -1);
                    if (arr.length >= 6) {
                        if (!(arr[0].equals(sid) && arr[5].equals(term))) {
                            filtered.add(line);
                        }
                    } else if (!line.trim().isEmpty()) {
                        filtered.add(line);
                    }
                }
                // Add new enrollment for this student for the current term only
                filtered.add(sid + "," + student[1] + "," + student[7] + "," + student[8] + ",Enrolled," + term);
                // Write back to enrollments.csv
                try (FileWriter fw = new FileWriter(enrollmentsFile, false)) {
                    for (String line : filtered) {
                        fw.write(line + "\n");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };

        // Helper to reload available/enrolled courses for a term
        Runnable reloadCoursesForTerm = () -> {
            String newTerm = termBox.getValue();
            availableCourses.clear();
            enrolledCourses.clear();
            Set<String> allEnrolledCodes = new HashSet<>();
            if (student[0] != null) {
                File studentDir = new File("students/" + student[0]);
                if (studentDir.exists() && studentDir.isDirectory()) {
                    File[] termDirs = studentDir.listFiles(File::isDirectory);
                    if (termDirs != null) {
                        for (File termDir : termDirs) {
                            File coursesFile = new File(termDir, "courses.csv");
                            if (coursesFile.exists()) {
                                try (BufferedReader br = new BufferedReader(new FileReader(coursesFile))) {
                                    br.readLine(); // skip header
                                    br.lines().forEach(line -> {
                                        String[] parts = line.split(",", -1);
                                        if (parts.length > 1) {
                                            // Use code:section as unique key
                                            allEnrolledCodes.add(parts[0] + (parts.length > 3 ? (":" + parts[3]) : ""));
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
            // Load available courses from program file, but only show those with sections in courses.csv for this term
            if (student[7] != null && newTerm != null) {
                File progFile = new File("programs/" + student[7] + ".csv");
                File mainCoursesFile = new File("courses.csv");
                List<String[]> allSections = new ArrayList<>();
                if (mainCoursesFile.exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(mainCoursesFile))) {
                        br.readLine(); // skip header
                        br.lines().forEach(line -> {
                            String[] parts = line.split(",", -1);
                            // Program, Section, CourseCode, CourseName, Units, Term, Schedule
                            if (parts.length >= 7 && parts[5].equals(newTerm) && student[7].equals(parts[0])) {
                                allSections.add(parts);
                            }
                        });
                    } catch (IOException e) { e.printStackTrace(); }
                }
                // Collect all enrolled course codes in any term for this student
                Set<String> allEnrolledCourseCodesAnyTerm = new HashSet<>();
                if (student[0] != null) {
                    File studentDir = new File("students/" + student[0]);
                    if (studentDir.exists() && studentDir.isDirectory()) {
                        File[] termDirs = studentDir.listFiles(File::isDirectory);
                        if (termDirs != null) {
                            for (File termDir : termDirs) {
                                File coursesFile = new File(termDir, "courses.csv");
                                if (coursesFile.exists()) {
                                    try (BufferedReader br = new BufferedReader(new FileReader(coursesFile))) {
                                        br.readLine(); // skip header
                                        br.lines().forEach(line -> {
                                            String[] parts = line.split(",", -1);
                                            if (parts.length > 0) {
                                                allEnrolledCourseCodesAnyTerm.add(parts[0]);
                                            }
                                        });
                                    } catch (IOException e) { e.printStackTrace(); }
                                }
                            }
                        }
                    }
                }
                if (progFile.exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(progFile))) {
                        br.lines().forEach(line -> {
                            String[] progParts = line.split(",", -1);
                            if (progParts.length > 0) {
                                String code = progParts[0];
                                // If already enrolled in any term, skip all sections of this course
                                if (allEnrolledCourseCodesAnyTerm.contains(code)) return;
                                // For each section in allSections, if code matches exactly, add as available if not already enrolled in this section
                                for (String[] section : allSections) {
                                    // Only match exact course codes (CS101 and CS101L are different)
                                    if (section[2].equals(code)) {
                                        String sectionKey = section[2] + ":" + section[1];
                                        if (!allEnrolledCodes.contains(sectionKey)) {
                                            availableCourses.add(new String[]{section[2], section[3], section[4], section[1], section[6]});
                                        }
                                    }
                                }
                            }
                        });
                    } catch (IOException e) { e.printStackTrace(); }
                }
            }
            // Load enrolled courses for the selected term from students/{studentid}/{term}/courses.csv
            if (student[0] != null && newTerm != null) {
                File enrolledFile = new File("students/" + student[0] + "/" + newTerm + "/courses.csv");
                if (enrolledFile.exists()) {
                    try (BufferedReader br = new BufferedReader(new FileReader(enrolledFile))) {
                        br.readLine(); // skip header
                        br.lines().forEach(line -> {
                            String[] parts = line.split(",", -1);
                            if (parts.length > 4) {
                                enrolledCourses.add(new String[]{parts[0], parts[1], parts[2], parts[3], parts[4]});
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        // Load terms and set up initial state
        if (termsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(termsFile))) {
                List<String> terms = new ArrayList<>();
                br.lines().forEach(terms::add);
                termBox.getItems().addAll(terms);
                if (selectedTerm != null && terms.contains(selectedTerm)) {
                    termBox.setValue(selectedTerm);
                } else if (!terms.isEmpty()) {
                    termBox.setValue(terms.get(terms.size() - 1)); // set latest as default
                }
                reloadCoursesForTerm.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TableView<String[]> availableCoursesTable = new TableView<>();
        availableCoursesTable.setPrefHeight(260);
        availableCoursesTable.setPrefWidth(500);
        TableColumn<String[], String> acCodeCol = new TableColumn<>("Code");
        acCodeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        acCodeCol.setPrefWidth(80);
        TableColumn<String[], String> acNameCol = new TableColumn<>("Name");
        acNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        acNameCol.setPrefWidth(160);
        TableColumn<String[], String> acUnitsCol = new TableColumn<>("Units");
        acUnitsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().length > 2 ? data.getValue()[2] : ""));
        acUnitsCol.setPrefWidth(60);
        TableColumn<String[], String> acSectionCol = new TableColumn<>("Section");
        acSectionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().length > 3 ? data.getValue()[3] : ""));
        acSectionCol.setPrefWidth(80);
        TableColumn<String[], String> acScheduleCol = new TableColumn<>("Schedule");
        acScheduleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().length > 4 ? data.getValue()[4] : ""));
        acScheduleCol.setMinWidth(120);
        // Distribute columns normally, only last column expands
        availableCoursesTable.getColumns().setAll(acCodeCol, acNameCol, acUnitsCol, acSectionCol, acScheduleCol);
        availableCoursesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // --- Add search field for available courses ---
        TextField availableCourseSearch = new TextField();
        availableCourseSearch.setPromptText("Search available course code, name, or section...");
        availableCourseSearch.setPrefWidth(300);
        availableCourseSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String search = newVal.toLowerCase();
            filteredAvailableCourses.setPredicate(arr ->
                    (arr.length > 0 && arr[0].toLowerCase().contains(search)) ||
                            (arr.length > 1 && arr[1].toLowerCase().contains(search)) ||
                            (arr.length > 3 && arr[3].toLowerCase().contains(search))
            );
        });

        TableView<String[]> enrolledCoursesTable = new TableView<>();
        enrolledCoursesTable.setPrefHeight(260);
        enrolledCoursesTable.setPrefWidth(500);
        TableColumn<String[], String> ecCodeCol = new TableColumn<>("Code");
        ecCodeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        ecCodeCol.setPrefWidth(80);
        TableColumn<String[], String> ecNameCol = new TableColumn<>("Name");
        ecNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        ecNameCol.setPrefWidth(160);
        TableColumn<String[], String> ecUnitsCol = new TableColumn<>("Units");
        ecUnitsCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().length > 2 ? data.getValue()[2] : ""));
        ecUnitsCol.setPrefWidth(60);
        TableColumn<String[], String> ecSectionCol = new TableColumn<>("Section");
        ecSectionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().length > 3 ? data.getValue()[3] : ""));
        ecSectionCol.setPrefWidth(80);
        TableColumn<String[], String> ecScheduleCol = new TableColumn<>("Schedule");
        ecScheduleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().length > 4 ? data.getValue()[4] : ""));
        ecScheduleCol.setMinWidth(120);
        // Distribute columns normally, only last column expands
        enrolledCoursesTable.getColumns().setAll(ecCodeCol, ecNameCol, ecUnitsCol, ecSectionCol, ecScheduleCol);
        enrolledCoursesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        availableCoursesTable.setItems(filteredAvailableCourses);
        enrolledCoursesTable.setItems(enrolledCourses);


        availableCoursesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        enrolledCoursesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // When term changes, just reload, do not save
        termBox.valueProperty().addListener((obs, oldTerm, newTerm) -> {
            reloadCoursesForTerm.run();
            enrolledCoursesTable.refresh(); // Ensure UI updates when term changes
        });

        Button addCourseBtn = new Button("Add →");
        addCourseBtn.setOnAction(e -> {
            ObservableList<String[]> selected = availableCoursesTable.getSelectionModel().getSelectedItems();
            for (String[] course : selected) {
                boolean exists = enrolledCourses.stream().anyMatch(c -> c[0].equals(course[0]) && c.length > 3 && c[3].equals(course[3]));
                if (!exists) {
                    // Add with section and schedule
                    enrolledCourses.add(new String[]{course[0], course[1], course.length > 2 ? course[2] : "", course.length > 3 ? course[3] : "", course.length > 4 ? course[4] : ""});
                }
            }
            // Remove from availableCourses if now enrolled in this term
            availableCourses.removeIf(ac -> enrolledCourses.stream().anyMatch(ec -> ec[0].equals(ac[0]) && ec.length > 3 && ec[3].equals(ac[3])));
            saveEnrollments.run();
            // Reload available courses after adding
            reloadCoursesForTerm.run();
            availableCoursesTable.refresh();
        });

        Button removeCourseBtn = new Button("← Remove");
        removeCourseBtn.setOnAction(e -> {
            ObservableList<String[]> selected = FXCollections.observableArrayList(enrolledCoursesTable.getSelectionModel().getSelectedItems());
            for (String[] course : selected) {
                // Only add back to availableCourses if not enrolled in any other term
                boolean enrolledInOtherTerm = false;
                for (String[] ec : enrolledCourses) {
                    if (ec[0].equals(course[0]) && !ec[2].isEmpty()) {
                        enrolledInOtherTerm = true;
                        break;
                    }
                }
                if (!enrolledInOtherTerm) {
                    // Make 'other' 10x smaller and not longer for schedule visualization
                    String code = course[0];
                    String name = course[1];
                    String units = course.length > 2 ? course[2] : "";
                    // If the name is 'other', shrink it
                    if (name.trim().equalsIgnoreCase("other")) {
                        name = "other";
                        // Make it 10x smaller by using a very short string
                        name = "o";
                    }
                    availableCourses.add(new String[]{code, name, units});
                }
                enrolledCourses.remove(course);
            }
            saveEnrollments.run();
            // Update available courses after removal
            reloadCoursesForTerm.run();
            availableCoursesTable.refresh();
        });

        Button enrollBtn = new Button("Done");
        enrollBtn.setOnAction(e -> {
            saveEnrollments.run();
            dialog.close();
            onEnrollmentsClick();
        });

        Button generateGsaBtn = new Button("Generate GSA");
        generateGsaBtn.setOnAction(e -> {
            try {
                String pdfPath = "students/" + student[0] + "/" + termBox.getValue() + "/GSA.pdf";
                new File("students/" + student[0] + "/" + termBox.getValue()).mkdirs();
                GsaPdfGenerator.generateGsaPdf(
                        pdfPath,
                        getClass().getResourceAsStream("images/logo.png"),
                        "SmartClass University",
                        student,
                        termBox.getValue(),
                        new ArrayList<>(enrolledCourses)
                );
                Desktop.getDesktop().open(new File(pdfPath));
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to generate or preview GSA PDF: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        // --- Schedule Visualization (always visible, not on button) ---
        VBox scheduleBox = new VBox(12); // increased spacing
        scheduleBox.setAlignment(Pos.TOP_CENTER);
        scheduleBox.setPadding(new Insets(16)); // increased padding
        scheduleBox.setStyle("-fx-background-color: #fff; -fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, #e9ecef, 4, 0.08, 0, 1);");
        Label schedTitle = new Label("Schedule Visualization");
        schedTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        scheduleBox.getChildren().add(schedTitle);

        availableCoursesTable.setStyle("");
        enrolledCoursesTable.setStyle("");

        // Hide horizontal scroll bars for both tables after layout
        Runnable hideHorizontalScrollBars = () -> {
            for (TableView<?> table : new TableView[]{availableCoursesTable, enrolledCoursesTable}) {
                table.lookupAll(".scroll-bar").forEach(node -> {
                    if (node instanceof ScrollBar) {
                        ScrollBar sb = (ScrollBar) node;
                        if (sb.getOrientation() == Orientation.HORIZONTAL) {
                            sb.setVisible(false);
                            sb.setPrefHeight(0);
                            sb.setMaxHeight(0);
                        }
                    }
                });
            }
        };
        // Run after scene is set
        javafx.application.Platform.runLater(hideHorizontalScrollBars);

        // --- Total Units Label ---
        Label totalUnitsLabel = new Label();
        totalUnitsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333; -fx-padding: 8 0 0 0;");
        // Helper to update total units
        Runnable updateTotalUnits = () -> {
            int totalUnits = 0;
            for (String[] course : enrolledCourses) {
                if (course.length > 2) {
                    try {
                        totalUnits += Integer.parseInt(course[2].trim());
                    } catch (Exception ignored) {}
                }
            }
            totalUnitsLabel.setText("Total Units Taken: " + totalUnits);
        };
        // Listen for changes in enrolledCourses
        enrolledCourses.addListener((ListChangeListener<String[]>) c -> updateTotalUnits.run());
        // Initial update
        updateTotalUnits.run();

        // Remove ScrollPane and add grid directly
        // Make the schedule grid content fit without scrolling
        Runnable updateScheduleGrid = () -> {
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            int startHour = 7, endHour = 21;
            int numRows = endHour - startHour;
            GridPane grid = new GridPane();
            grid.setPadding(new Insets(8));
            grid.setHgap(4);
            grid.setVgap(2);
            grid.setStyle("-fx-background-color: white;");
            grid.add(new Label("Time/Day"), 0, 0);
            for (int d = 0; d < days.length; d++) {
                Label dayLabel = new Label(days[d]);
                dayLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
                grid.add(dayLabel, d + 1, 0);
            }
            for (int h = 0; h < numRows; h++) {
                int hour = startHour + h;
                String timeLabel = String.format("%02d:00-%02d:00", hour, hour + 1);
                Label timeLbl = new Label(timeLabel);
                timeLbl.setStyle("-fx-font-size: 10px;");
                grid.add(timeLbl, 0, h + 1);
            }
            Label[][] cellMatrix = new Label[numRows][days.length];
            int[][] cellCount = new int[numRows][days.length];
            // Count conflicts
            for (String[] course : enrolledCourses) {
                if (course.length < 5) continue;
                String code = course[0];
                String section = course[3];
                String sched = course[4];
                if (sched == null || sched.isEmpty()) continue;
                String[] parts = sched.split(" ", 3);
                if (parts.length < 3) continue;
                String timeRange = parts[0];
                String daysStr = parts[2];
                String[] schedDays = daysStr.split("/");
                String[] times = timeRange.split("-");
                if (times.length != 2) continue;
                int start = Integer.parseInt(times[0].split(":")[0]);
                int end = Integer.parseInt(times[1].split(":")[0]);
                for (String d : schedDays) {
                    d = d.trim();
                    for (int col = 0; col < days.length; col++) {
                        if (days[col].equalsIgnoreCase(d)) {
                            for (int row = start - startHour; row < end - startHour; row++) {
                                if (row >= 0 && row < numRows) {
                                    cellCount[row][col]++;
                                }
                            }
                        }
                    }
                }
            }
            // Place cells
            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < days.length; col++) {
                    boolean hasCourse = false;
                    String labelText = "";
                    // Remove border-radius for sharp box edges
                    String cellStyle = "-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-padding: 2; -fx-font-size: 10px; -fx-border-width: 1; -fx-background-radius: 0; -fx-border-radius: 0;";
                    for (String[] course : enrolledCourses) {
                        if (course.length < 5) continue;
                        String code = course[0];
                        String section = course[3];
                        String sched = course[4];
                        if (sched == null || sched.isEmpty()) continue;
                        String[] parts = sched.split(" ", 3);
                        if (parts.length < 3) continue;
                        String timeRange = parts[0];
                        String room = parts[1].replaceAll("[()]", "");
                        String daysStr = parts[2];
                        String[] schedDays = daysStr.split("/");
                        String[] times = timeRange.split("-");
                        if (times.length != 2) continue;
                        int start = Integer.parseInt(times[0].split(":")[0]);
                        int end = Integer.parseInt(times[1].split(":")[0]);
                        for (String d : schedDays) {
                            d = d.trim();
                            if (days[col].equalsIgnoreCase(d)) {
                                if (row >= start - startHour && row < end - startHour) {
                                    hasCourse = true;
                                    labelText = code + " (" + section + ")\n" + room;
                                    if (cellCount[row][col] > 1) {
                                        cellStyle = "-fx-background-color: #ffcccc; -fx-border-color: #e57373; -fx-padding: 2; -fx-font-size: 10px; -fx-border-width: 1; -fx-background-radius: 0; -fx-border-radius: 0;";
                                    } else {
                                        cellStyle = "-fx-background-color: #e0f7fa; -fx-border-color: #b2ebf2; -fx-padding: 2; -fx-font-size: 10px; -fx-border-width: 1; -fx-background-radius: 0; -fx-border-radius: 0;";
                                    }
                                }
                            }
                        }
                    }
                    Label cell = new Label(labelText);
                    cell.setWrapText(true);
                    cell.setPrefWidth(70);
                    cell.setMinWidth(70);
                    cell.setMaxWidth(70);
                    cell.setPrefHeight(22);
                    cell.setMinHeight(22);
                    cell.setMaxHeight(22);
                    cell.setAlignment(Pos.CENTER);
                    cell.setStyle(cellStyle);
                    grid.add(cell, col + 1, row + 1);
                }
            }
            // Remove previous grid if any, then add new one
            if (scheduleBox.getChildren().size() > 1) {
                scheduleBox.getChildren().remove(1, scheduleBox.getChildren().size());
            }
            scheduleBox.getChildren().add(grid);
            VBox.setVgrow(grid, Priority.ALWAYS);
        };
        // Update schedule grid whenever enrolledCourses changes or term changes
        enrolledCourses.addListener((ListChangeListener<String[]>) c -> updateScheduleGrid.run());
        termBox.valueProperty().addListener((obs, oldVal, newVal) -> updateScheduleGrid.run());
        // Initial schedule grid
        updateScheduleGrid.run();

        // --- Info Card (Student, Program, Term) ---
        VBox infoBox = new VBox(10,
                titleLabel,
                studentLabel,
                programLabel,
                new Label("Term:"),
                termBox
        );
        infoBox.setPadding(new Insets(16));
        infoBox.setStyle("-fx-background-color: #fff; -fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, #e9ecef, 4, 0.08, 0, 1);");
        infoBox.setPrefWidth(420);

        // --- Available Courses Card ---
        VBox availableCard = new VBox(10,
                new Label("Available Courses"),
                availableCourseSearch,
                availableCoursesTable,
                addCourseBtn
        );
        availableCard.setPadding(new Insets(16));
        availableCard.setStyle("-fx-background-color: #fff; -fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, #e9ecef, 4, 0.08, 0, 1);");
        availableCoursesTable.setPrefHeight(260);
        availableCoursesTable.setPrefWidth(900);
        availableCard.setPrefWidth(900);

        // --- Enrolled Courses Card ---
        VBox enrolledCard = new VBox(10,
                new Label("Enrolled Courses"),
                enrolledCoursesTable,
                removeCourseBtn
        );
        enrolledCard.setPadding(new Insets(16));
        enrolledCard.setStyle("-fx-background-color: #fff; -fx-border-color: #e0e0e0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, #e9ecef, 4, 0.08, 0, 1);");
        enrolledCoursesTable.setPrefHeight(260);
        enrolledCoursesTable.setPrefWidth(900);
        enrolledCard.setPrefWidth(900);

        VBox bottomCards = new VBox(18, availableCard, enrolledCard);
        bottomCards.setAlignment(Pos.CENTER);
        bottomCards.setPrefWidth(900);

        VBox leftColumn = new VBox(18, infoBox, bottomCards, new HBox(12, enrollBtn, generateGsaBtn));
        leftColumn.setAlignment(Pos.TOP_CENTER);
        leftColumn.setPrefWidth(700); // slightly smaller left column
        leftColumn.setMaxWidth(700);

        VBox scheduleBoxWrapper = new VBox(scheduleBox, totalUnitsLabel);
        scheduleBoxWrapper.setAlignment(Pos.TOP_CENTER);
        scheduleBoxWrapper.setPrefWidth(600);
        scheduleBoxWrapper.setMaxWidth(600);
        // Also update the scheduleBox and schedScroll size
        scheduleBox.setPrefWidth(600);

        HBox mainBox = new HBox(24, leftColumn, scheduleBoxWrapper); // more space between columns
        mainBox.setAlignment(Pos.TOP_CENTER); // Center the schedule visualization
        mainBox.setPadding(new Insets(16, 16, 16, 16));
        mainBox.setPrefHeight(800);
        mainBox.setPrefWidth(1100);

        root.getChildren().clear();
        root.getChildren().add(mainBox);

        // Set the root VBox as the Scene's root before showing dialog
        Scene scene = new Scene(root, 1400, 800); // wider window
        dialog.setScene(scene);
        // Set dialog size to be a bit larger than admin but not full screen
        dialog.setMaximized(false);
        dialog.setWidth(1450); // wider dialog
        dialog.setHeight(850);
        dialog.setResizable(true);
        dialog.centerOnScreen();
        dialog.showAndWait();
    }

    @FXML
    private void onTermsClick() {
        setMainContent(Settings.OpenSettings());
    }

    public void onCoursesClick(ActionEvent actionEvent) {
        setMainContent(Sections.SectionsOpen());
    }

    @FXML private void onLogoutClick() { System.exit(0); }

    private void animateCount(Label label, int target) {
        if (target <= 0) {
            label.setText("0");
            return;
        }
        Timeline timeline = new Timeline();
        int durationMillis = 800; // total animation duration
        int frames = Math.max(1, Math.min(target, 60)); // at least 1 frame
        for (int i = 0; i <= frames; i++) {
            int value = (int) Math.round(i * (target / (double) frames));
            KeyFrame kf = new KeyFrame(Duration.millis(i * (durationMillis / (double) frames)),
                    e -> label.setText(String.valueOf(value)));
            timeline.getKeyFrames().add(kf);
        }
        timeline.play();
    }
}
