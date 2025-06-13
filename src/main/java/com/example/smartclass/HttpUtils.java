package com.example.smartclass;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;

public class HttpUtils {
    public static String uploadPdfAndGetCourses(File pdfFile, String endpoint) throws IOException {
        String boundary = Long.toHexString(System.currentTimeMillis());
        String LINE_FEED = "\r\n";
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setUseCaches(false);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream output = conn.getOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, "UTF-8"), true)) {
            // Send binary file.
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + pdfFile.getName() + "\"").append(LINE_FEED);
            writer.append("Content-Type: application/pdf").append(LINE_FEED);
            writer.append(LINE_FEED).flush();
            Files.copy(pdfFile.toPath(), output);
            output.flush();
            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED).flush();
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 400) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }
        conn.disconnect();
        return response.toString();
    }

    public static String AutoSchedule(String coursesArrayJson, String roomsArrayJson) {
        OkHttpClient client = new OkHttpClient();
        try {
            JSONArray coursesArr = new JSONArray((coursesArrayJson != null && !coursesArrayJson.trim().isEmpty()) ? coursesArrayJson : "[]");
            JSONArray roomsArr = new JSONArray((roomsArrayJson != null && !roomsArrayJson.trim().isEmpty()) ? roomsArrayJson : "[{\"Room\":\"R101\"},{\"Room\":\"R102\"},{\"Room\":\"R103\"},{\"Room\":\"R104\"}]");
            JSONObject body = new JSONObject();
            body.put("courses", coursesArr);
            body.put("rooms", roomsArr);
            String jsonInputString = body.toString();

            RequestBody requestBody = RequestBody.create(jsonInputString, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url("http://127.0.0.1:3000/auto-scheduler")
                    .post(requestBody)
                    .addHeader("Accept", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "ERROR: HTTP " + response.code();
                }
                String respStr = response.body().string().trim();
                System.out.println("Response: " + respStr);
                if (respStr.startsWith("{")) {
                    JSONObject respJson = new JSONObject(respStr);
                    if (respJson.has("scheduled")) {
                        return respJson.getJSONArray("scheduled").toString();
                    } else if (respJson.has("error")) {
                        return "ERROR: " + respJson.getString("error");
                    } else {
                        return respStr;
                    }
                } else if (respStr.startsWith("[")) {
                    return respStr;
                } else {
                    return respStr;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String escapeJson(String value) {
        if (value == null) return "null";
        return '"' + value.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }

}
