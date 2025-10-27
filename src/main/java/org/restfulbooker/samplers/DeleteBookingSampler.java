package org.restfulbooker.samplers;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.config.Arguments;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DeleteBookingSampler extends AbstractJavaSamplerClient {

    private static final String BASE_URL = "https://restful-booker.herokuapp.com";

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = new Arguments();
        args.addArgument("bookingId", "1");
        return args;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext ctx) {
        SampleResult result = new SampleResult();
        String id = ctx.getParameter("bookingId");

        result.sampleStart();
        try {
            String token = getAuthToken();

            URL url = new URL(BASE_URL + "/booking/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Cookie", "token=" + token);

            int code = conn.getResponseCode();
            String response = readResponse(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());

            result.setResponseCode(String.valueOf(code));
            result.setResponseMessage(conn.getResponseMessage());
            result.setResponseData(response, StandardCharsets.UTF_8.name());
            result.setSuccessful(code == 201 || code == 200); // DELETE vraća 201

        } catch (Exception e) {
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e.getMessage());
        } finally {
            result.sampleEnd();
        }
        return result;
    }

    private String getAuthToken() throws Exception {
        URL url = new URL(BASE_URL + "/auth");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        String authPayload = "{\"username\":\"admin\",\"password\":\"password123\"}";
        try (OutputStream os = conn.getOutputStream()) {
            os.write(authPayload.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line = br.readLine();
                int start = line.indexOf("\"token\":\"") + 9;
                int end = line.indexOf("\"", start);
                return line.substring(start, end);
            }
        } else {
            throw new RuntimeException("Auth failed");
        }
    }

    private String readResponse(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return br.lines().reduce("", (a, b) -> a + b + "\n").trim();
        }
    }
}
