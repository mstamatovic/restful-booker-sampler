package org.restfulbooker.samplers;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApacheHttpAuthSampler extends AbstractJavaSamplerClient {

    private static final String BASE_URL = "https://restful-booker.herokuapp.com";

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = new Arguments();
        args.addArgument("username", "admin");
        args.addArgument("password", "password123");
        return args;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext ctx) {
        SampleResult result = new SampleResult();
        String username = ctx.getParameter("username");
        String password = ctx.getParameter("password");
        String payload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

        result.sampleStart();

        try {

            CloseableHttpClient httpClient = HttpClients.createDefault();

            HttpPost postRequest;
            postRequest = new HttpPost(BASE_URL + "/auth");
            postRequest.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
            postRequest.setHeader("Content-Type", "application/json");
            postRequest.setHeader("Accept", "application/json");


            URL url = new URL(BASE_URL + "/auth");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            String response = readResponse(code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream());

            result.setResponseCode(String.valueOf(code));
            result.setResponseMessage(conn.getResponseMessage());
            result.setResponseData(response, StandardCharsets.UTF_8.name());
            result.setSuccessful(code == 200);

        } catch (Exception e) {
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e.getMessage());
        } finally {
            result.sampleEnd();
        }
        return result;
    }

    private String readResponse(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return br.lines().reduce("", (a, b) -> a + b + "\n").trim();
        }
    }
}