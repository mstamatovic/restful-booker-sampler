package org.restfulbooker.samplers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RestfulBookerSampler extends AbstractJavaSamplerClient {

    String BASE_URL = "https://restful-booker.herocuapp.com";


    @Override
    public void setupTest(JavaSamplerContext javaSamplerContext) {

    }

    @Override
    public void teardownTest(JavaSamplerContext javaSamplerContext) {

    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("base_url", "https://restful-booker.herocuapp.com");
        arguments.addArgument("endpoint", "/booking");
        arguments.addArgument("method", "POST");
        arguments.addArgument("body", "");
        return arguments;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult result = new SampleResult();
        String baseURL = javaSamplerContext.getParameter("base_url");
        String endpoint = javaSamplerContext.getParameter("endpoint");
        String method = javaSamplerContext.getParameter("method");
        String body = javaSamplerContext.getParameter("body");

        result.sampleStart();

        try {
            URL url = new URL(baseURL);
            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod(method);

            if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method)) {
                httpConnection.setDoOutput(true);
                httpConnection.setRequestProperty("Content-Type", "application/json");
                httpConnection.setRequestProperty("Content-Length", String.valueOf(body.length()));
                httpConnection.setRequestProperty("Accept", "application/json");

                String token = getAuthToken();
                httpConnection.setRequestProperty("Cookie", "token = " + token);
                int responseCode = httpConnection.getResponseCode();
                String responseBody;
                if (responseCode >= 200 && responseCode < 300) {
                    responseBody = readResponse(httpConnection.getInputStream());
                    result.setSuccessful(true);
                } else {
                    responseBody = readResponse(httpConnection.getErrorStream());
                    result.setSuccessful(false);
                }

                result.setResponseCode(String.valueOf(responseCode));
                result.setResponseMessage(httpConnection.getResponseMessage());
                result.setResponseData(responseBody, StandardCharsets.UTF_8.name());

            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            result.sampleEnd();
        }

        return result;
    }

    private String getAuthToken() throws MalformedURLException {
        URL url = new URL(BASE_URL + "/auth");
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            String authPayload = "{\"username\": \"admin\", \"password\": \"password123\"}";
            connection.getOutputStream().write(authPayload.getBytes(StandardCharsets.UTF_8));

            if (connection.getResponseCode() == 200) {
                String response = readResponse(connection.getInputStream());
                JsonNode node = new ObjectMapper().readTree(response);
                return node.get("token").asText();
            } else {
                throw new RuntimeException("Failed to authenticate");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readResponse(InputStream inputStream) throws IOException {

        if (inputStream == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString().trim();
        }
    }
}
