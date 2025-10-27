package org.restfulbooker.samplers;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CreateBookingSampler extends AbstractJavaSamplerClient {

    private static final String BASE_URL = "https://restful-booker.herokuapp.com";

    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();
        arguments.addArgument("payload", "{\n" +
                "  \"firstname\" : \"Isidora\",\n" +
                "  \"lastname\" : \"Stamatovic\",\n" +
                "  \"totalprice\" : 1234,\n" +
                "  \"depositpaid\" : true,\n" +
                "  \"bookingdates\" : {\n" +
                "    \"checkin\" : \"2025-11-11\",\n" +
                "    \"checkout\" : \"2025-12-12\"\n" +
                "  },\n" +
                "  \"additionalneeds\" : \"Breakfast\"\n" +
                "}");
        return arguments;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        SampleResult result = new SampleResult();
        String payload = javaSamplerContext.getParameter("payload");
        result.sampleStart();

        try {
            URL url = new URL(BASE_URL + "/booking");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes());
            }

            int responseCode = connection.getResponseCode();
            String response = readResponse(responseCode >= 200 && responseCode < 300 ?
                    connection.getInputStream() : connection.getErrorStream());

            result.setResponseCode(String.valueOf(responseCode));
            result.setResponseMessage(connection.getResponseMessage());
            result.setResponseData(response, StandardCharsets.UTF_8.name());
            result.setSuccessful(responseCode == 200);

        } catch (Exception e) {
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e.getMessage());
        } finally {
            result.sampleEnd();
        }

        return result;
    }

    public String readResponse(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return br.lines().reduce("", (a, b) -> a + b + "\n").trim();
        }
    }
}
