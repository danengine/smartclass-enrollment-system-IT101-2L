package com.example.smartclass;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

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
}

