package com.example.smartclass;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.*;

public class Settings {
    public static HBox OpenSettings() {
        // --- Terms Section (as Table) ---
        ObservableList<String> terms = FXCollections.observableArrayList();
        File termsFile = new File("terms.csv");
        if (termsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(termsFile))) {
                br.lines().forEach(terms::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Do NOT remove first for terms (no header)

        TableView<String> termsTable = new TableView<>(terms);
        termsTable.setPrefHeight(120);
        termsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<String, String> termCol = new TableColumn<>("Term");
        termCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        termCol.setCellFactory(TextFieldTableCell.forTableColumn());
        termCol.setOnEditCommit(event -> {
            terms.set(event.getTablePosition().getRow(), event.getNewValue());
            saveTerms(terms, termsFile);
        });
        termsTable.getColumns().setAll(termCol);
        termsTable.setEditable(true);

        TextField newTermField = new TextField();
        newTermField.setPromptText("New term");
        newTermField.setPrefWidth(90);
        Button addTermBtn = new Button("Add");
        Button delTermBtn = new Button("Delete");
        HBox termControls = new HBox(5, newTermField, addTermBtn, delTermBtn);
        termControls.setAlignment(Pos.CENTER_LEFT);
        addTermBtn.setOnAction(e -> {
            String t = newTermField.getText().trim();
            if (!t.isEmpty() && !terms.contains(t)) {
                terms.add(t);
                newTermField.clear();
                saveTerms(terms, termsFile);
            }
        });
        delTermBtn.setOnAction(e -> {
            String sel = termsTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                terms.remove(sel);
                saveTerms(terms, termsFile);
            }
        });
        VBox termsBox = new VBox(10,
                new Label("Terms"),
                termsTable,
                termControls
        );
        termsBox.setPrefWidth(110);
        termsBox.setAlignment(Pos.TOP_LEFT);

        // --- Misc Section ---
        ObservableList<String[]> misc = FXCollections.observableArrayList();
        File miscFile = new File("miscellaneous.csv");
        if (miscFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(miscFile))) {
                br.readLine(); // skip header
                br.lines().forEach(line -> {
                    String[] arr = line.split(",", 2);
                    if (arr.length == 2) misc.add(arr);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        TableView<String[]> miscTable = new TableView<>(misc);
        miscTable.setPrefHeight(240);
        miscTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<String[], String> feeCol = new TableColumn<>("Fee Name");
        feeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));

        TableColumn<String[], String> amtCol = new TableColumn<>("Amount");
        amtCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));

        miscTable.getColumns().setAll(feeCol, amtCol);

        TextField feeField = new TextField();
        feeField.setPromptText("Fee Name");
        feeField.setTooltip(new Tooltip("Enter fee type (e.g. Library Fee)"));
        feeField.setPrefWidth(130);

        TextField amtField = new TextField();
        amtField.setPromptText("Amount");
        amtField.setTooltip(new Tooltip("Enter amount (e.g. 500)"));
        amtField.setPrefWidth(80);

        Button addFeeBtn = new Button("➕");
        addFeeBtn.setTooltip(new Tooltip("Add fee"));
        Button delFeeBtn = new Button("🗑");
        delFeeBtn.setTooltip(new Tooltip("Delete selected fee"));

        addFeeBtn.setOnAction(e -> {
            String fee = feeField.getText().trim();
            String amt = amtField.getText().trim();
            if (!fee.isEmpty() && !amt.isEmpty()) {
                misc.add(new String[]{fee, amt});
                feeField.clear();
                amtField.clear();
                saveMisc(misc, miscFile);
            }
        });

        delFeeBtn.setOnAction(e -> {
            String[] sel = miscTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                misc.remove(sel);
                saveMisc(misc, miscFile);
            }
        });

        HBox miscControls = new HBox(5, feeField, amtField, addFeeBtn, delFeeBtn);
        miscControls.setAlignment(Pos.CENTER_LEFT);

        VBox miscBox = new VBox(10,
                new Label("Miscellaneous Fees"),
                miscTable,
                miscControls
        );
        miscBox.setPrefWidth(320);
        miscBox.setAlignment(Pos.TOP_LEFT);

        // --- Rooms Section (as Table) ---
        ObservableList<String> rooms = FXCollections.observableArrayList();
        File roomsFile = new File("rooms.csv");
        if (roomsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(roomsFile))) {
                String header = br.readLine(); // skip header
                br.lines().forEach(line -> {
                    for (String room : line.split(" ")) {
                        if (!room.trim().isEmpty()) rooms.add(room.trim());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        TableView<String> roomsTable = new TableView<>(rooms);
        roomsTable.setPrefHeight(120);
        roomsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<String, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
        roomCol.setCellFactory(TextFieldTableCell.forTableColumn());
        roomCol.setOnEditCommit(event -> {
            rooms.set(event.getTablePosition().getRow(), event.getNewValue());
            saveRooms(rooms, roomsFile);
        });
        roomsTable.getColumns().setAll(roomCol);
        roomsTable.setEditable(true);

        TextField newRoomField = new TextField();
        newRoomField.setPromptText("New room");
        newRoomField.setPrefWidth(90);
        Button addRoomBtn = new Button("Add");
        Button delRoomBtn = new Button("Delete");
        HBox roomControls = new HBox(5, newRoomField, addRoomBtn, delRoomBtn);
        roomControls.setAlignment(Pos.CENTER_LEFT);
        addRoomBtn.setOnAction(e -> {
            String room = newRoomField.getText().trim();
            if (!room.isEmpty() && !rooms.contains(room)) {
                rooms.add(room);
                newRoomField.clear();
                saveRooms(rooms, roomsFile);
            }
        });
        delRoomBtn.setOnAction(e -> {
            String sel = roomsTable.getSelectionModel().getSelectedItem();
            if (sel != null) {
                rooms.remove(sel);
                saveRooms(rooms, roomsFile);
            }
        });
        VBox roomsBox = new VBox(10,
                new Label("Rooms"),
                roomsTable,
                roomControls
        );
        roomsBox.setPrefWidth(110);
        roomsBox.setAlignment(Pos.TOP_LEFT);

        // --- Layout ---
        HBox main = new HBox(32, termsBox, miscBox, roomsBox);
        main.setPadding(new Insets(32));
        main.setAlignment(Pos.TOP_CENTER);
        main.setStyle("-fx-background-color: #f8f8f8;");

        HBox.setHgrow(termsBox, Priority.ALWAYS);
        HBox.setHgrow(miscBox, Priority.ALWAYS);
        HBox.setHgrow(roomsBox, Priority.ALWAYS);
        VBox.setVgrow(termsTable, Priority.ALWAYS);
        VBox.setVgrow(miscTable, Priority.ALWAYS);
        VBox.setVgrow(roomsTable, Priority.ALWAYS);

        termsBox.setMinWidth(150);
        miscBox.setMinWidth(400);
        roomsBox.setMinWidth(150);
        main.setPrefWidth(Double.MAX_VALUE);
        main.setPrefHeight(Double.MAX_VALUE);

        return main;
    }

    private static void saveTerms(ObservableList<String> terms, File termsFile) {
        try (FileWriter fw = new FileWriter(termsFile, false)) {
            for (String t : terms) fw.write(t + System.lineSeparator());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void saveRooms(ObservableList<String> rooms, File roomsFile) {
        try (FileWriter fw = new FileWriter(roomsFile, false)) {
            fw.write("Room\n"); // Write header
            for (String r : rooms) fw.write(r + "\n"); // Write each room on a new line
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void saveMisc(ObservableList<String[]> misc, File miscFile) {
        try (FileWriter fw = new FileWriter(miscFile, false)) {
            fw.write("Fee Name,Amount\n");
            for (String[] arr : misc) fw.write(arr[0] + "," + arr[1] + "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
