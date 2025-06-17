package com.example.smartclass;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Sections {

    public static VBox SectionsOpen() {
        VBox layout = new VBox(16);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: white;");

        // --- Term Dropdown for Filtering ---
        ComboBox<String> termBox = new ComboBox<>();
        termBox.setPromptText("Select Term");
        File termsFile = new File("terms.csv");
        ObservableList<String> terms = FXCollections.observableArrayList();
        int defaultIndex = 0;
        if (termsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(termsFile))) {
                br.lines().forEach(terms::add);
                if (!terms.isEmpty()) {
                    defaultIndex = terms.size() - 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        termBox.setItems(terms);
        if (!terms.isEmpty()) {
            termBox.setValue(terms.get(defaultIndex)); // Set latest term as default
        }

        // --- Search Field for Filtering ---
        TextField searchField = new TextField();
        searchField.setPromptText("Search section, course, program, or schedule...");
        searchField.setPrefWidth(220);

        // --- Table of Sections ---
        TableView<String[]> sectionTable = new TableView<>();
        sectionTable.setPrefHeight(320);
        sectionTable.setPrefWidth(700);
        sectionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        sectionTable.setStyle("-fx-background-color: #f0f0f0; -fx-table-cell-border-color: #e0e0e0;");
        // --- Loading overlay setup ---
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setMaxSize(40, 40);
        loadingIndicator.setStyle("-fx-progress-color: #2196f3;"); // blue
        StackPane loadingOverlay = new StackPane();
        loadingOverlay.getChildren().add(loadingIndicator);
        loadingOverlay.setStyle("-fx-background-color: rgba(255,255,255,0.4); -fx-backdrop-filter: blur(4px);");
        loadingOverlay.setVisible(false);
        loadingOverlay.setMouseTransparent(false); // Block mouse events
        StackPane tableStack = new StackPane(sectionTable, loadingOverlay);
        // --- Table columns setup ---
        String[] headers = {"Term", "Section", "Program", "Course Code", "Course Name", "Units", "Schedule"};
        for (int i = 0; i < headers.length; i++) {
            final int idx = i;
            TableColumn<String[], String> col = new TableColumn<>(headers[i]);
            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[idx]));
            col.setPrefWidth(idx == 4 ? 220 : 90);
            sectionTable.getColumns().add(col);
        }
        ObservableList<String[]> sectionData = FXCollections.observableArrayList();
        FilteredList<String[]> filteredSectionData = new FilteredList<>(sectionData, s -> true);
        sectionTable.setItems(filteredSectionData);

        // --- Highlight conflicts in sectionTable ---
        sectionTable.setRowFactory(tv -> new TableRow<String[]>() {
            @Override
            protected void updateItem(String[] item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    boolean conflict = false;
                    String itemSchedule = item[6];
                    for (String[] other : sectionTable.getItems()) {
                        if (other == item) continue;
                        // Only check for same section and same term
                        if (item[0].equals(other[0]) && item[1].equals(other[1])) {
                            String otherSchedule = other[6];
                            if (itemSchedule != null && otherSchedule != null && !itemSchedule.equals("N/A") && !otherSchedule.equals("N/A") && !itemSchedule.isEmpty() && !otherSchedule.isEmpty()) {
                                String daysA = extractDays(itemSchedule);
                                String daysB = extractDays(otherSchedule);
                                if (daysA != null && daysB != null) {
                                    String[] arrA = daysA.split("/");
                                    String[] arrB = daysB.split("/");
                                    outer: for (String d1 : arrA) for (String d2 : arrB) if (d1.trim().equalsIgnoreCase(d2.trim())) {
                                        int[] timeA = extractTimeRange(itemSchedule);
                                        int[] timeB = extractTimeRange(otherSchedule);
                                        if (timeA != null && timeB != null && timeA[0] < timeB[1] && timeA[1] > timeB[0]) {
                                            conflict = true;
                                            break outer;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (conflict) {
                        setStyle("-fx-background-color: #ffcccc;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        Runnable refreshSections = () -> {
            sectionData.clear();
            File coursesFile = new File("courses.csv");
            String selectedTerm = termBox.getValue();
            if (coursesFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(coursesFile))) {
                    String header = br.readLine(); // skip header
                    br.lines().forEach(line -> {
                        String[] arr = line.split(",", -1);
                        // arr: Program,Section,CourseCode,CourseName,Units,Term,Schedule
                        if (arr.length >= 7 && (selectedTerm == null || arr[5].equals(selectedTerm))) {
                            sectionData.add(new String[]{arr[5], arr[1], arr[0], arr[2], arr[3], arr[4], arr[6]});
                        }
                    });
                } catch (IOException e) { e.printStackTrace(); }
            }
        };
        refreshSections.run();
        termBox.valueProperty().addListener((obs, old, val) -> refreshSections.run());

        // --- Search Filtering ---
        searchField.textProperty().addListener((obs, old, val) -> {
            String search = val.toLowerCase();
            filteredSectionData.setPredicate(arr ->
                    arr[1].toLowerCase().contains(search) || // Section
                            arr[2].toLowerCase().contains(search) || // Program
                            arr[3].toLowerCase().contains(search) || // Course Code
                            arr[4].toLowerCase().contains(search) || // Course Name
                            arr[6].toLowerCase().contains(search)    // Schedule
            );
        });

        // --- Create Section Button ---
        Button createSectionBtn = new Button("+ Create Section");
        int finalDefaultIndex = defaultIndex;
        createSectionBtn.setOnAction(e -> {
            // Popup dialog for section creation
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Create Section");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            VBox dialogLayout = new VBox(12);
            dialogLayout.setPadding(new Insets(10));

            ComboBox<String> termSelect = new ComboBox<>(terms);
            termSelect.setPromptText("Select Term");
            if (!terms.isEmpty()) termSelect.setValue(terms.get(finalDefaultIndex));

            ComboBox<String> programBox = new ComboBox<>();
            programBox.setPromptText("Select Program");
            File programsDir = new File("programs");
            if (programsDir.exists() && programsDir.isDirectory()) {
                File[] files = programsDir.listFiles((dir, name) -> name.endsWith(".csv"));
                if (files != null) {
                    for (File f : files) {
                        String name = f.getName().replaceFirst("\\.csv$", "");
                        programBox.getItems().add(name);
                    }
                }
            }

            TextField sectionField = new TextField();
            sectionField.setPromptText("Section Name (e.g. Section A)");
            sectionField.setPrefWidth(180);

            TableView<String[]> coursesTable = new TableView<>();
            coursesTable.setPrefHeight(200);
            coursesTable.setPrefWidth(420);
            TableColumn<String[], String> codeCol = new TableColumn<>("Code");
            codeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
            codeCol.setPrefWidth(100);
            TableColumn<String[], String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
            nameCol.setPrefWidth(220);
            coursesTable.getColumns().addAll(codeCol, nameCol);
            coursesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            ObservableList<String[]> courseList = FXCollections.observableArrayList();
            coursesTable.setItems(courseList);

            programBox.valueProperty().addListener((obs, old, val) -> {
                courseList.clear();
                if (val != null) {
                    File progFile = new File("programs/" + val + ".csv");
                    if (progFile.exists()) {
                        try (BufferedReader br = new BufferedReader(new FileReader(progFile))) {
                            br.lines().forEach(line -> {
                                String[] arr = line.split(",", -1);
                                if (arr.length > 1) courseList.add(new String[]{arr[0], arr[1], arr[2]});
                            });
                        } catch (IOException ex) { ex.printStackTrace(); }
                    }
                }
            });

            dialogLayout.getChildren().addAll(
                    new Label("Term:"), termSelect,
                    new Label("Program:"), programBox,
                    new Label("Section Name:"), sectionField,
                    new Label("Select Courses:"), coursesTable
            );
            dialog.getDialogPane().setContent(dialogLayout);

            dialog.setResultConverter(btn -> {
                if (btn == ButtonType.OK) {
                    String program = programBox.getValue();
                    String sectionInput = sectionField.getText().trim();
                    String term = termSelect.getValue();
                    ObservableList<String[]> selected = coursesTable.getSelectionModel().getSelectedItems();
                    if (program == null || sectionInput.isEmpty() || selected.isEmpty() || term == null) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Please select a term, program, enter a section name, select at least one course.");
                        alert.showAndWait();
                        return null;
                    }
                    File coursesFile = new File("courses.csv");
                    boolean fileExists = coursesFile.exists();
                    java.util.Set<String> existing = new java.util.HashSet();
                    if (fileExists) {
                        try (BufferedReader br = new BufferedReader(new FileReader(coursesFile))) {
                            br.readLine();
                            br.lines().forEach(line -> {
                                String[] arr = line.split(",", -1);
                                if (arr.length >= 6) {
                                    existing.add(arr[5] + "," + arr[1] + "," + arr[2]);
                                }
                            });
                        } catch (IOException ex) { ex.printStackTrace(); }
                    }
                    // Split section names by comma, trim, and create for each
                    String[] sectionNames = sectionInput.split(",");
                    try (FileWriter fw = new FileWriter(coursesFile, true)) {
                        if (!fileExists) fw.write("Program,Section,CourseCode,CourseName,Units,Term,Schedule\n");
                        for (String section : sectionNames) {
                            section = section.trim();
                            if (section.isEmpty()) continue;
                            for (String[] c : selected) {
                                String key = term + "," + section + "," + c[0];
                                if (existing.contains(key)) continue; // skip duplicate
                                fw.write(program + "," + section + "," + c[0] + "," + c[1] + "," + c[2] + "," + term + ",N/A\n");
                            }
                        }
                    } catch (IOException ex) { ex.printStackTrace(); }
                }
                return null;
            });
            dialog.showAndWait();
            refreshSections.run();
        });

        sectionTable.setRowFactory(tv -> {
            TableRow<String[]> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    String[] section = row.getItem();
                    Dialog<Void> dialog = new Dialog<>();
                    dialog.setTitle("Set Schedule");
                    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                    VBox dialogLayout = new VBox(12);
                    dialogLayout.setPadding(new Insets(10));

                    Label courseLabel = new Label("Course: " + section[4]);

                    ComboBox<Integer> hoursBox = new ComboBox<>();
                    for (int i = 1; i <= 6; i++) hoursBox.getItems().add(i);
                    hoursBox.setPromptText("Select Hours");

                    ComboBox<String> startTimeBox = new ComboBox<>();
                    for (int h = 7; h <= 19; h++) {
                        String time = String.format("%02d:00", h);
                        startTimeBox.getItems().add(time);
                    }
                    startTimeBox.setPromptText("Select Start Time");

                    Label endTimeLabel = new Label("End Time: ");

                    // Add Start Day ComboBox for editing
                    ComboBox<String> startDayBox = new ComboBox<>();
                    startDayBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
                    startDayBox.setPromptText("Select Start Day");
                    // Try to pre-select based on current schedule if possible
                    String currentSchedule = section.length > 6 ? section[6] : null;
                    if (currentSchedule != null && !currentSchedule.equals("N/A") && !currentSchedule.isEmpty()) {
                        for (String day : startDayBox.getItems()) {
                            if (currentSchedule.startsWith(day)) {
                                startDayBox.setValue(day);
                                break;
                            }
                        }
                    }

                    // Room selection as table (right side)
                    TableView<String> roomTable = new TableView<>();
                    TableColumn<String, String> roomCol = new TableColumn<>("Room");
                    roomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
                    roomCol.setPrefWidth(120);
                    roomTable.getColumns().add(roomCol);
                    ObservableList<String> roomList = FXCollections.observableArrayList();
                    File roomsFile = new File("rooms.csv");
                    if (roomsFile.exists()) {
                        try (BufferedReader br = new BufferedReader(new FileReader(roomsFile))) {
                            String line;
                            boolean first = true;
                            while ((line = br.readLine()) != null) {
                                if (first) { first = false; continue; } // skip header
                                if (!line.trim().isEmpty()) roomList.add(line.trim());
                            }
                        } catch (IOException ex) { ex.printStackTrace(); }
                    }
                    roomTable.setItems(roomList);
                    roomTable.setPrefHeight(150);
                    roomTable.setPrefWidth(120);
                    roomTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

                    // --- Parse current schedule and set initial values ---
                    String schedule = section.length > 6 ? section[6] : null;
                    String parsedStart = null, parsedEnd = null, parsedRoom = null, parsedHours = null, parsedDays = null;
                    if (schedule != null && !schedule.equals("N/A") && !schedule.isEmpty()) {
                        // Try to parse format: "08:00-10:00 (Room) Monday/Wednesday", "1h (Room) Monday/Wednesday", etc.
                        String sched = schedule;
                        // Extract days at the end
                        int lastSpace = sched.lastIndexOf(' ');
                        if (lastSpace > 0) {
                            String possibleDays = sched.substring(lastSpace + 1);
                            if (possibleDays.matches("[A-Za-z/]+")) {
                                parsedDays = possibleDays;
                                sched = sched.substring(0, lastSpace).trim();
                            }
                        }
                        // Extract room if present
                        if (sched.contains("(") && sched.contains(")")) {
                            int start = sched.indexOf('(');
                            int end = sched.indexOf(')');
                            parsedRoom = sched.substring(start + 1, end).trim();
                            sched = (sched.substring(0, start).trim() + sched.substring(end + 1)).trim();
                        }
                        // Extract hours or time range
                        if (sched.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
                            String[] times = sched.split("-");
                            parsedStart = times[0];
                            parsedEnd = times[1];
                        } else if (sched.matches("\\d+h")) {
                            parsedHours = sched;
                        }
                        // Set UI values
                        if (parsedStart != null) startTimeBox.setValue(parsedStart);
                        if (parsedHours != null) {
                            try { hoursBox.setValue(Integer.parseInt(parsedHours.replace("h", "").trim())); } catch (Exception ignored) {}
                        } else if (parsedStart != null && parsedEnd != null) {
                            try {
                                int startHour = Integer.parseInt(parsedStart.split(":")[0]);
                                int endHour = Integer.parseInt(parsedEnd.split(":")[0]);
                                int hours = endHour - startHour;
                                if (hours > 0) hoursBox.setValue(hours);
                            } catch (Exception ignored) {}
                        }
                        if (parsedRoom != null) {
                            int idx = roomList.indexOf(parsedRoom);
                            if (idx >= 0) roomTable.getSelectionModel().select(idx);
                        }
                        if (parsedDays != null) {
                            for (String day : startDayBox.getItems()) {
                                if (parsedDays.startsWith(day)) {
                                    startDayBox.setValue(day);
                                    break;
                                }
                            }
                        }
                    }

                    // Allow setting hours only (start time optional)
                    Runnable updateEndTime = () -> {
                        Integer hours = hoursBox.getValue();
                        String start = startTimeBox.getValue();
                        if (hours != null && start != null) {
                            int startHour = Integer.parseInt(start.split(":")[0]);
                            int endHour = startHour + hours;
                            if (endHour > 24) endHour = 24;
                            String endTime = String.format("%02d:00", endHour);
                            endTimeLabel.setText("End Time: " + endTime);
                        } else {
                            endTimeLabel.setText("End Time: ");
                        }
                    };
                    hoursBox.setOnAction(e -> updateEndTime.run());
                    startTimeBox.setOnAction(e -> updateEndTime.run());

                    // Layout: left (fields), right (room table)
                    VBox leftBox = new VBox(8,
                            courseLabel,
                            new Label("Hours:"), hoursBox,
                            new Label("Start Time (optional):"), startTimeBox,
                            endTimeLabel,
                            new Label("Start Day:"), startDayBox
                    );
                    leftBox.setPrefWidth(200);
                    VBox rightBox = new VBox(8, new Label("Select Room:"), roomTable);
                    rightBox.setPrefWidth(140);
                    HBox dialogContent = new HBox(24, leftBox, rightBox);
                    dialogContent.setAlignment(Pos.CENTER);

                    dialogLayout.getChildren().clear();
                    dialogLayout.getChildren().add(dialogContent);
                    dialog.getDialogPane().setContent(dialogLayout);

                    dialog.setResultConverter(btn -> {
                        if (btn == ButtonType.OK) {
                            Integer hours = hoursBox.getValue();
                            String start = startTimeBox.getValue();
                            String room = roomTable.getSelectionModel().getSelectedItem();
                            String startDay = startDayBox.getValue();
                            String scheduleStr = null;
                            String days = null;
                            if (startDay != null && !startDay.isEmpty()) {
                                int units = 0;
                                try { units = Integer.parseInt(section[5]); } catch (Exception ex) { units = 0; }
                                days = getScheduleDays(startDay, units);
                            }
                            if (hours != null) {
                                if (start != null && !start.isEmpty()) {
                                    int startHour = Integer.parseInt(start.split(":")[0]);
                                    int endHour = startHour + hours;
                                    if (endHour > 24) endHour = 24;
                                    String endTime = String.format("%02d:00", endHour);
                                    if (room != null && !room.isEmpty()) {
                                        scheduleStr = start + "-" + endTime + " (" + room + ")";
                                    } else {
                                        scheduleStr = start + "-" + endTime;
                                    }
                                } else {
                                    if (room != null && !room.isEmpty()) {
                                        scheduleStr = hours + "h (" + room + ")";
                                    } else {
                                        scheduleStr = hours + "h";
                                    }
                                }
                            }
                            // Append days at the end if present
                            if (scheduleStr != null && days != null) {
                                scheduleStr = scheduleStr + " " + days;
                            } else if (days != null) {
                                scheduleStr = days;
                            }
                            if (scheduleStr != null) {
                                // --- Conflict check before saving ---
                                try {
                                    File coursesFile = new File("courses.csv");
                                    List<String> lines = new ArrayList<>();
                                    if (coursesFile.exists()) {
                                        try (BufferedReader br = new BufferedReader(new FileReader(coursesFile))) {
                                            String header = br.readLine();
                                            if (header != null) lines.add(header);
                                            br.lines().forEach(lines::add);
                                        }
                                    }
                                    // Parse new schedule info
                                    String newTerm = section[0];
                                    String newSection = section[1];
                                    String newProgram = section[2];
                                    String newCourseCode = section[3];
                                    String newCourseName = section[4];
                                    // Extract new schedule details
                                    String newRoom = room;
                                    String newStart = start;
                                    String newEnd = null;
                                    if (start != null && hours != null) {
                                        int startHour = Integer.parseInt(start.split(":")[0]);
                                        int endHour = startHour + hours;
                                        if (endHour > 24) endHour = 24;
                                        newEnd = String.format("%02d:00", endHour);
                                    }
                                    String newDays = days;
                                    // If no startDay is selected, keep the old days if present
                                    if ((startDay == null || startDay.isEmpty()) && schedule != null && !schedule.equals("N/A") && !schedule.isEmpty()) {
                                        // Try to extract days from the old schedule
                                        int lastSpace = schedule.lastIndexOf(' ');
                                        if (lastSpace > 0) {
                                            String possibleDays = schedule.substring(lastSpace + 1);
                                            if (possibleDays.matches("[A-Za-z/]+")) {
                                                newDays = possibleDays;
                                            }
                                        }
                                    }
                                    // Check for conflicts
                                    for (int i = 1; i < lines.size(); i++) {
                                        String[] arr = lines.get(i).split(",", -1);
                                        if (arr.length < 7) continue;
                                        // Skip self
                                        if (arr[5].equals(newTerm) && arr[1].equals(newSection) && arr[0].equals(newProgram) && arr[2].equals(newCourseCode)) continue;
                                        // Only check same term
                                        if (!arr[5].equals(newTerm)) continue;
                                        String otherSchedule = arr[6];
                                        if (otherSchedule == null || otherSchedule.equals("N/A") || otherSchedule.isEmpty()) continue;
                                        // Extract room, days, start, end from other schedule
                                        String otherRoom = null, otherStart = null, otherEnd = null, otherDays = null;
                                        String sched = otherSchedule;
                                        int lastSpace = sched.lastIndexOf(' ');
                                        if (lastSpace > 0) {
                                            String possibleDays = sched.substring(lastSpace + 1);
                                            if (possibleDays.matches("[A-Za-z/]+")) {
                                                otherDays = possibleDays;
                                                sched = sched.substring(0, lastSpace).trim();
                                            }
                                        }
                                        if (sched.contains("(") && sched.contains(")")) {
                                            int s = sched.indexOf('(');
                                            int e = sched.indexOf(')');
                                            otherRoom = sched.substring(s + 1, e).trim();
                                            sched = (sched.substring(0, s).trim() + sched.substring(e + 1)).trim();
                                        }
                                        if (sched.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
                                            String[] times = sched.split("-");
                                            otherStart = times[0];
                                            otherEnd = times[1];
                                        }
                                        // Only check for conflict if room is the same
                                        if (newRoom != null && otherRoom != null && newRoom.equals(otherRoom)) {
                                            // Check day overlap (simple string match for any day)
                                            if (newDays != null && otherDays != null) {
                                                String[] newDayArr = newDays.split("/");
                                                String[] otherDayArr = otherDays.split("/");
                                                boolean dayOverlap = false;
                                                for (String d1 : newDayArr) for (String d2 : otherDayArr) if (d1.equals(d2)) dayOverlap = true;
                                                if (dayOverlap) {
                                                    // Check time overlap
                                                    if (newStart != null && newEnd != null && otherStart != null && otherEnd != null) {
                                                        int ns = Integer.parseInt(newStart.split(":")[0]);
                                                        int ne = Integer.parseInt(newEnd.split(":")[0]);
                                                        int os = Integer.parseInt(otherStart.split(":")[0]);
                                                        int oe = Integer.parseInt(otherEnd.split(":")[0]);
                                                        if (ns < oe && ne > os) {
                                                            Alert alert = new Alert(Alert.AlertType.ERROR, "Schedule conflict: Room " + newRoom + " is already booked for " + arr[3] + " (" + arr[4] + ") at this time.");
                                                            alert.showAndWait();
                                                            return null;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    // If no conflict, save
                                    for (int i = 1; i < lines.size(); i++) {
                                        String[] arr = lines.get(i).split(",", -1);
                                        if (arr.length >= 7 &&
                                                arr[5].equals(section[0]) && // Term
                                                arr[1].equals(section[1]) && // Section
                                                arr[0].equals(section[2]) && // Program
                                                arr[2].equals(section[3]) && // CourseCode
                                                arr[3].equals(section[4])) { // CourseName
                                            // Rebuild scheduleStr with newDays if needed
                                            String[] schedParts = scheduleStr.split(" ");
                                            if (newDays != null && !newDays.equals("N/A")) {
                                                if (schedParts.length > 1) {
                                                    schedParts[schedParts.length - 1] = newDays;
                                                    scheduleStr = String.join(" ", schedParts);
                                                } else {
                                                    scheduleStr = scheduleStr + " " + newDays;
                                                }
                                            }
                                            arr[6] = scheduleStr;
                                            lines.set(i, String.join(",", arr));
                                        }
                                    }
                                    try (FileWriter fw = new FileWriter(coursesFile, false)) {
                                        for (String l : lines) fw.write(l + "\n");
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        return null;
                    });
                    dialog.showAndWait();
                    refreshSections.run();
                }
            });
            return row;
        });

        // Remove Delete column from sectionTable
        sectionTable.getColumns().removeIf(col -> "Delete".equals(col.getText()));

        // --- Auto Schedule All Button ---
        Button autoScheduleBtn = new Button("Auto Schedule All");
        autoScheduleBtn.setOnAction(e -> {
            loadingOverlay.setVisible(true);
            autoScheduleBtn.setDisable(true);
            new Thread(() -> {
                try {
                    JSONArray arr = new JSONArray();
                    String selectedTerm = termBox.getValue();
                    for (String[] row : sectionData) {
                        // Only include rows from the selected term
                        if (selectedTerm == null || !row[0].equals(selectedTerm)) continue;
                        JSONObject obj = new JSONObject();
                        obj.put("term", row[0]);
                        obj.put("section", row[1]);
                        obj.put("program", row[2]);
                        obj.put("courseCode", row[3]);
                        obj.put("courseName", row[4]);
                        obj.put("units", row[5]);
                        obj.put("schedule", row[6]);
                        arr.put(obj);
                    }
                    JSONArray roomsArr = new JSONArray();
                    try (BufferedReader br = new BufferedReader(new FileReader("rooms.csv"))) {
                        String headerLine = br.readLine();
                        if (headerLine != null) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                String[] values = line.split(",");
                                JSONObject roomObj = new JSONObject();
                                for (int i = 0; i < headers.length && i < values.length; i++) {
                                    roomObj.put("Room", values[i]);
                                }
                                roomsArr.put(roomObj);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    String result = HttpUtils.AutoSchedule(arr.toString(2), roomsArr.toString(2));
                    JSONArray newSchedules = null;
                    try {
                        newSchedules = new JSONArray(result);
                    } catch (Exception ex) {
                        final String errorMsg = result;
                        javafx.application.Platform.runLater(() -> {
                            loadingIndicator.setVisible(false);
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Auto-schedule failed: " + errorMsg);
                            alert.showAndWait();
                        });
                        return;
                    }
                    JSONArray finalNewSchedules = newSchedules;
                    javafx.application.Platform.runLater(() -> {
                        loadingOverlay.setVisible(false);
                        autoScheduleBtn.setDisable(false);
                        Dialog<ButtonType> dialog = new Dialog<>();
                        dialog.setTitle("Confirm Auto-Schedule Changes");
                        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                        TableView<org.json.JSONObject> table = new TableView<>();
                        table.setEditable(true);
                        table.setPrefHeight(320);
                        table.setPrefWidth(700);
                        // Only show courseCode, courseName, schedule columns
                        String[] cols = {"courseCode", "courseName", "schedule"};
                        for (String colName : cols) {
                            TableColumn<org.json.JSONObject, String> col = new TableColumn<>(colName.substring(0,1).toUpperCase()+colName.substring(1));
                            col.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().optString(colName, "")));
                            if (colName.equals("schedule")) {
                                col.setPrefWidth(350);
                                col.setMinWidth(200);
                                col.setMaxWidth(Double.MAX_VALUE);
                                col.setResizable(true);
                                // Make schedule column editable
                                col.setCellFactory(TextFieldTableCell.forTableColumn());
                                col.setOnEditCommit(event -> {
                                    org.json.JSONObject obj = event.getRowValue();
                                    obj.put("schedule", event.getNewValue());
                                    table.refresh();
                                });
                            } else {
                                col.setPrefWidth(colName.equals("courseName") ? 220 : 150);
                            }
                            table.getColumns().add(col);
                        }
                        ObservableList<org.json.JSONObject> items = FXCollections.observableArrayList();
                        for (int i = 0; i < finalNewSchedules.length(); i++) {
                            items.add(finalNewSchedules.getJSONObject(i));
                        }
                        table.setItems(items);

                        // --- Conflict detection helper ---
                        table.setRowFactory(tv -> new TableRow<org.json.JSONObject>() {
                            @Override
                            protected void updateItem(org.json.JSONObject item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item == null || empty) {
                                    setStyle("");
                                } else {
                                    boolean conflict = false;
                                    for (org.json.JSONObject other : table.getItems()) {
                                        if (other == item) continue;
                                        if (isScheduleConflict(item, other)) {
                                            conflict = true;
                                            break;
                                        }
                                    }
                                    if (conflict) {
                                        setStyle("-fx-background-color: #ffcccc;");
                                    } else {
                                        setStyle("");
                                    }
                                }
                            }
                        });

                        // Re-check conflicts on edit
                        items.addListener((javafx.collections.ListChangeListener<org.json.JSONObject>) c -> table.refresh());
                        // Remove: table.setOnEditCommit(e -> table.refresh());
                        // Instead, add edit commit handler to the schedule column only
                        for (TableColumn<org.json.JSONObject, ?> col : table.getColumns()) {
                            if (col.getText().equalsIgnoreCase("Schedule")) {
                                ((TableColumn<org.json.JSONObject, String>) col).setOnEditCommit(event -> {
                                    org.json.JSONObject obj = event.getRowValue();
                                    obj.put("schedule", event.getNewValue());
                                    table.refresh();
                                });
                            }
                        }

                        VBox vbox = new VBox(new Label("The following schedules will be applied. Proceed? (Conflicts are highlighted in red. You may edit schedules before confirming.)"), table);
                        vbox.setPadding(new Insets(10));
                        dialog.getDialogPane().setContent(vbox);
                        dialog.setResizable(true);
                        dialog.setResultConverter(btn -> btn);
                        dialog.showAndWait().ifPresent(btn -> {
                            if (btn == ButtonType.OK) {
                                // Before saving, check for any remaining conflicts
                                for (org.json.JSONObject obj1 : items) {
                                    for (org.json.JSONObject obj2 : items) {
                                        if (obj1 == obj2) continue;
                                        if (isScheduleConflict(obj1, obj2)) {
                                            Alert alert = new Alert(Alert.AlertType.ERROR, "Cannot save: There are still schedule conflicts highlighted in red.");
                                            alert.showAndWait();
                                            return;
                                        }
                                    }
                                }
                                File coursesFile = new File("courses.csv");
                                List<String> lines = new ArrayList<>();
                                try (BufferedReader br = new BufferedReader(new FileReader(coursesFile))) {
                                    String header = br.readLine();
                                    if (header != null) lines.add(header);
                                    br.lines().forEach(lines::add);
                                } catch (IOException ex) { ex.printStackTrace(); }
                                for (org.json.JSONObject sched : items) {
                                    for (int j = 1; j < lines.size(); j++) {
                                        String[] arrLine = lines.get(j).split(",", -1);
                                        if (arrLine.length >= 7 &&
                                                arrLine[5].equals(sched.optString("term")) &&
                                                arrLine[1].equals(sched.optString("section")) &&
                                                arrLine[0].equals(sched.optString("program")) &&
                                                arrLine[2].equals(sched.optString("courseCode")) &&
                                                arrLine[3].equals(sched.optString("courseName"))) {
                                            arrLine[6] = sched.optString("schedule");
                                            lines.set(j, String.join(",", arrLine));
                                        }
                                    }
                                }
                                try (FileWriter fw = new FileWriter(coursesFile, false)) {
                                    for (String l : lines) fw.write(l + "\n");
                                } catch (IOException ex) { ex.printStackTrace(); }
                                refreshSections.run();
                            }
                        });
                    });
                } finally {
                    // In case of any unexpected error, ensure loading is hidden
                    javafx.application.Platform.runLater(() -> {
                        loadingOverlay.setVisible(false);
                        autoScheduleBtn.setDisable(false);
                    });
                }
            }).start();
        });

        // --- Mass Delete Button ---
        Button massDeleteBtn = new Button("Delete Selected");
        massDeleteBtn.setOnAction(e -> {
            ObservableList<String[]> selected = sectionTable.getSelectionModel().getSelectedItems();
            if (selected.isEmpty()) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected sections?", ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Confirm Mass Delete");
            confirm.showAndWait().ifPresent(type -> {
                if (type == ButtonType.YES) {
                    File coursesFile = new File("courses.csv");
                    List<String> lines = new ArrayList<>();
                    try (BufferedReader br = new BufferedReader(new FileReader(coursesFile))) {
                        String header = br.readLine();
                        lines.add(header);
                        br.lines().forEach(line -> {
                            String[] arr = line.split(",", -1);
                            boolean toDelete = selected.stream().anyMatch(sel ->
                                    arr.length >= 7 &&
                                            arr[5].equals(sel[0]) && // Term
                                            arr[1].equals(sel[1]) && // Section
                                            arr[0].equals(sel[2]) && // Program
                                            arr[2].equals(sel[3])    // CourseCode
                            );
                            if (!toDelete) lines.add(line);
                        });
                    } catch (IOException ex) { ex.printStackTrace(); }
                    try (FileWriter fw = new FileWriter(coursesFile, false)) {
                        for (String l : lines) fw.write(l + "\n");
                    } catch (IOException ex) { ex.printStackTrace(); }
                    refreshSections.run();
                }
            });
        });

        HBox topBar = new HBox(10, new Label("Term:"), termBox, searchField, createSectionBtn, massDeleteBtn, autoScheduleBtn);
        layout.getChildren().addAll(topBar, new Label("Sections List"), tableStack);
        return layout;
    }

    // Helper method for schedule days
    public static String getScheduleDays(String startDay, int units) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        int idx = -1;
        for (int i = 0; i < days.length; i++) {
            if (days[i].equalsIgnoreCase(startDay)) {
                idx = i;
                break;
            }
        }
        if (idx == -1) return "N/A";
        if (units == 3) {
            // 3 units: every other day (e.g., MWF)
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 3; i++) {
                if (idx + i * 2 < days.length) {
                    if (i > 0) sb.append("/");
                    sb.append(days[idx + i * 2]);
                }
            }
            return sb.toString();
        } else if (units == 2) {
            // 2 units: start and two days after (e.g., WF)
            StringBuilder sb = new StringBuilder();
            sb.append(days[idx]);
            if (idx + 2 < days.length) {
                sb.append("/").append(days[idx + 2]);
            }
            return sb.toString();
        }
        return "N/A";
    }

    // --- Helper for schedule conflict detection ---
    private static boolean isScheduleConflict(org.json.JSONObject a, org.json.JSONObject b) {
        // Check same room
        String schedA = a.optString("schedule", "");
        String schedB = b.optString("schedule", "");
        if (schedA == null || schedB == null || schedA.equals("N/A") || schedB.equals("N/A") || schedA.isEmpty() || schedB.isEmpty()) return false;
        String roomA = extractRoom(schedA);
        String roomB = extractRoom(schedB);
        if (roomA == null || roomB == null || !roomA.equals(roomB)) return false;
        // Allow same time/room if section is different
        if (!a.optString("section", "").equals(b.optString("section", ""))) return false;
        // Check day overlap
        String daysA = extractDays(schedA);
        String daysB = extractDays(schedB);
        if (daysA == null || daysB == null) return false;
        String[] arrA = daysA.split("/");
        String[] arrB = daysB.split("/");
        boolean dayOverlap = false;
        for (String d1 : arrA) for (String d2 : arrB) if (d1.equals(d2)) dayOverlap = true;
        if (!dayOverlap) return false;
        // Check time overlap
        int[] timeA = extractTimeRange(schedA);
        int[] timeB = extractTimeRange(schedB);
        if (timeA == null || timeB == null) return false;
        return timeA[0] < timeB[1] && timeA[1] > timeB[0];
    }
    private static String extractRoom(String schedule) {
        if (schedule == null) return null;
        int start = schedule.indexOf('(');
        int end = schedule.indexOf(')');
        if (start >= 0 && end > start) {
            return schedule.substring(start + 1, end).trim();
        }
        return null;
    }

    // --- Helper for schedule conflict detection in sectionTable ---
    private static String extractDays(String sched) {
        int lastSpace = sched.lastIndexOf(' ');
        if (lastSpace > 0) {
            String possibleDays = sched.substring(lastSpace + 1);
            if (possibleDays.matches("[A-Za-z/]+")) {
                return possibleDays;
            }
        }
        return null;
    }
    private static int[] extractTimeRange(String sched) {
        if (sched.matches(".*\\d{2}:\\d{2}-\\d{2}:\\d{2}.*")) {
            String[] parts = sched.split(" ");
            for (String part : parts) {
                if (part.matches("\\d{2}:\\d{2}-\\d{2}:\\d{2}")) {
                    String[] times = part.split("-");
                    int start = Integer.parseInt(times[0].split(":")[0]);
                    int end = Integer.parseInt(times[1].split(":")[0]);
                    return new int[]{start, end};
                }
            }
        } else if (sched.matches(".*\\d+h.*")) {
            int hIdx = sched.indexOf("h");
            if (hIdx > 0) {
                String before = sched.substring(0, hIdx).replaceAll("[^0-9]", "");
                try {
                    int hours = Integer.parseInt(before);
                    return new int[]{7, 7 + hours};
                } catch (Exception ignored) {}
            }
        }
        return null;
    }
}
