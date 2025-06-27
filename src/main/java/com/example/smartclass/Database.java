package com.example.smartclass;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Database {

    public static boolean validateCredentials(String username, String password) {
        String hashedInput = hashPassword(password);
        try (BufferedReader reader = new BufferedReader(new FileReader("accounts.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(",");
                if (parts.length == 2) {
                    String fileUsername = parts[0].trim();
                    String filePasswordHash = parts[1].trim();
                    if (fileUsername.equals(username) && filePasswordHash.equals(hashedInput)) {
                        return true;
                    }
                    System.out.println(hashedInput + " " + filePasswordHash);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading accounts.txt: " + e.getMessage());
        }
        return false;
    }

    public static ObservableList<Student> loadStudentsFromCSV(String filePath) {
        ObservableList<Student> students = FXCollections.observableArrayList();

        try (FileReader fr = new FileReader(filePath)) {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = fr.read()) != -1) {
                sb.append((char) c);
            }

            String content = sb.toString();
            String[] lines = content.split("\n");

            boolean firstLine = true;
            for (String line : lines) {
                if (firstLine) {
                    firstLine = false;
                    continue; // skip header
                }

                String[] data = line.trim().split(",");
                if (data.length >= 19) {
                    Student student = new Student(
                        data[0].trim(), // studentId
                        data[1].trim(), // lrn
                        data[2].trim(), // name
                        data[3].trim(), // suffix
                        data[4].trim(), // email
                        data[5].trim(), // gender
                        data[6].trim(), // address
                        data[7].trim(), // contactNumber
                        data[8].trim(), // course
                        data[9].trim(), // year
                        data[10].trim(), // dateEnrolled
                        data[11].trim(), // fatherName
                        data[12].trim(), // fatherContact
                        data[13].trim(), // motherName
                        data[14].trim(), // motherContact
                        data[15].trim(), // guardianName
                        data[16].trim(), // guardianContact
                        Boolean.parseBoolean(data[17].trim()), // birthCertSubmitted
                        Boolean.parseBoolean(data[18].trim()), // form137Submitted
                        Boolean.parseBoolean(data[19].trim()), // goodMoralSubmitted
                        Boolean.parseBoolean(data[20].trim())  // medCertSubmitted
                    );
                    students.add(student);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return students;
    }

    public static ObservableList<Course> getCoursesByProgram(String program) {
        ObservableList<Course> courseList = FXCollections.observableArrayList();
        File file = new File("programs/" + program + ".csv");

        if (!file.exists()) {
            System.out.println("CSV file for program not found: " + file.getPath());
            return courseList;
        }

        try {
            FileReader fr = new FileReader(file);
            StringBuilder sb = new StringBuilder();
            int i;
            while ((i = fr.read()) != -1) {
                sb.append((char) i);
            }
            fr.close();

            String[] lines = sb.toString().split("\n");
            for (String line : lines) {
                String[] parts = line.split(",", -1); // Allow empty fields
                if (parts.length >= 3) {
                    String code = parts[0].trim();
                    String name = parts[1].trim();
                    int units = 0;
                    try { units = Integer.parseInt(parts[2].trim()); } catch (Exception ignored) {}
                    courseList.add(new Course(code, name, "", units));
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading CSV for program " + program + ": " + e.getMessage());
        }

        return courseList;
    }

    public static ObservableList<String> showPrograms() {
        ObservableList<String> programs = FXCollections.observableArrayList();
        File folder = new File("programs");

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".csv"));
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    String programCode = fileName.replace(".csv", "");
                    programs.add(programCode);
                }
            }
        } else {
            System.out.println("The 'programs' folder does not exist.");
        }

        return programs;
    }

    public static void addProgram(String programName) {
        try {
            File dir = new File("programs");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File programFile = new File(dir, programName + ".csv");

            if (programFile.exists()) {
                System.out.println("Program already exists: " + programName);
                return;
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(programFile))) {

            }

            System.out.println("Program added: " + programName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addCourseToProgram(String program, Course course) {
        String folderPath = "programs";
        String filePath = folderPath + "/" + program + ".csv";

        try {
            // Ensure the folder exists
            if (!Files.exists(Paths.get(folderPath))) {
                Files.createDirectories(Paths.get(folderPath));
            }

            // Append course to CSV using FileWriter only
            FileWriter writer = new FileWriter(filePath, true);
            String line = course.courseCodeProperty().get() + "," +
                    course.courseNameProperty().get() + "," +
                    course.getUnits() + "\n";
            writer.write(line);
            writer.close();

            System.out.println("Added course: " + course.courseCodeProperty().get() + " to " + program);

        } catch (IOException e) {
            System.err.println("Error writing to " + filePath);
            e.printStackTrace();
        }
    }

    public static void saveCoursesForProgram(String program, ObservableList<Course> courses) {
        String folderPath = "programs";
        String filePath = folderPath + "/" + program + ".csv";

        try {
            if (!Files.exists(Paths.get(folderPath))) {
                Files.createDirectories(Paths.get(folderPath));
            }

            try (FileWriter writer = new FileWriter(filePath, false)) { // overwrite mode
                for (Course c : courses) {
                    String line = c.courseCodeProperty().get() + "," +
                            c.courseNameProperty().get() + "," +
                            c.getUnits() + "\n";
                    writer.write(line);
                }
            }
            System.out.println("Courses saved for program: " + program);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteProgram(String programName) {
        File programFile = new File("programs/" + programName + ".csv");
        if (programFile.exists()) {
            if (programFile.delete()) {
                System.out.println("Program deleted: " + programName);
            } else {
                System.out.println("Failed to delete program: " + programName);
            }
        } else {
            System.out.println("Program file not found: " + programName);
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}