package org.restfulbooker.samplers;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.config.Arguments;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class GetAllBookingsSampler extends AbstractJavaSamplerClient {

    private static final String BASE_URL = "https://restful-booker.herokuapp.com";

    @Override
    public Arguments getDefaultParameters() {
        return new Arguments(); // nema parametara
    }

    @Override
    public SampleResult runTest(JavaSamplerContext ctx) {
        SampleResult result = new SampleResult();
        result.sampleStart();
        try {
            URL url = new URL(BASE_URL + "/booking");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

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