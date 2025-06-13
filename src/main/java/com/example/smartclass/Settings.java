package com.example.smartclass;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.*;

public class Settings {
    public static HBox OpenSettings() {
        // --- Terms Section ---
        ObservableList<String> terms = FXCollections.observableArrayList();
        File termsFile = new File("terms.csv");
        if (termsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(termsFile))) {
                br.lines().forEach(terms::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ListView<String> termsList = new ListView<>(terms);
        termsList.setPrefHeight(240);

        TextField newTermField = new TextField();
        newTermField.setPromptText("New term");
        newTermField.setPrefWidth(130);
        newTermField.setTooltip(new Tooltip("Enter new academic term (e.g. 2024-2025)"));

        Button addTermBtn = new Button("➕");
        addTermBtn.setTooltip(new Tooltip("Add term"));
        Button delTermBtn = new Button("🗑");
        delTermBtn.setTooltip(new Tooltip("Delete selected term"));
        Button saveTermBtn = new Button("💾 Save");
        saveTermBtn.setTooltip(new Tooltip("Save terms to file"));

        HBox termControls = new HBox(5, newTermField, addTermBtn, delTermBtn);
        termControls.setAlignment(Pos.CENTER_LEFT);

        addTermBtn.setOnAction(e -> {
            String t = newTermField.getText().trim();
            if (!t.isEmpty() && !terms.contains(t)) {
                terms.add(t);
                newTermField.clear();
            }
        });

        delTermBtn.setOnAction(e -> {
            String sel = termsList.getSelectionModel().getSelectedItem();
            if (sel != null) terms.remove(sel);
        });

        saveTermBtn.setOnAction(e -> {
            try (FileWriter fw = new FileWriter(termsFile, false)) {
                for (String t : terms) fw.write(t + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        VBox termsBox = new VBox(10,
                new Label("📘 Terms"),
                termsList,
                termControls,
                saveTermBtn
        );
        termsBox.setPrefWidth(220);
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
        Button saveFeeBtn = new Button("💾 Save");
        saveFeeBtn.setTooltip(new Tooltip("Save fees to file"));

        addFeeBtn.setOnAction(e -> {
            String fee = feeField.getText().trim();
            String amt = amtField.getText().trim();
            if (!fee.isEmpty() && !amt.isEmpty()) {
                misc.add(new String[]{fee, amt});
                feeField.clear();
                amtField.clear();
            }
        });

        delFeeBtn.setOnAction(e -> {
            String[] sel = miscTable.getSelectionModel().getSelectedItem();
            if (sel != null) misc.remove(sel);
        });

        saveFeeBtn.setOnAction(e -> {
            try (FileWriter fw = new FileWriter(miscFile, false)) {
                fw.write("Fee Name,Amount\n");
                for (String[] arr : misc) fw.write(arr[0] + "," + arr[1] + "\n");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        HBox miscControls = new HBox(5, feeField, amtField, addFeeBtn, delFeeBtn);
        miscControls.setAlignment(Pos.CENTER_LEFT);

        VBox miscBox = new VBox(10,
                new Label("💰 Miscellaneous Fees"),
                miscTable,
                miscControls,
                saveFeeBtn
        );
        miscBox.setPrefWidth(320);
        miscBox.setAlignment(Pos.TOP_LEFT);

        // --- Layout ---
        HBox main = new HBox(24, termsBox, miscBox);
        main.setPadding(new Insets(24));
        main.setAlignment(Pos.TOP_LEFT);

        HBox.setHgrow(termsBox, Priority.ALWAYS);
        HBox.setHgrow(miscBox, Priority.ALWAYS);
        VBox.setVgrow(termsList, Priority.ALWAYS);
        VBox.setVgrow(miscTable, Priority.ALWAYS);

        termsBox.setMinWidth(300);
        miscBox.setMinWidth(400);
        main.setPrefWidth(Double.MAX_VALUE);
        main.setPrefHeight(Double.MAX_VALUE);

        return main;
    }
}
