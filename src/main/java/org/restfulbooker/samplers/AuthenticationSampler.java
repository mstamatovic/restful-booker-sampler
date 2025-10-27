package org.restfulbooker.samplers;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AuthenticationSampler extends AbstractJavaSamplerClient {

    private static final String BASE_URL = "https://restful-booker.herokuapp.com";

    @Override
    public Arguments getDefaultParameters() {
        Arguments args = new Arguments();
        args.addArgument("endpoint", "/booking");
        args.addArgument("method", "GET");
        args.addArgument("bookingId", "");
        args.addArgument("username", "admin");
        args.addArgument("password", "password123");
        args.addArgument("payload", "");
        return args;
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        result.sampleStart();

        String endpoint = context.getParameter("endpoint");
        String method = context.getParameter("method").toUpperCase();
        String bookingId = context.getParameter("bookingId");
        String payload = context.getParameter("payload");
        String username = context.getParameter("username");
        String password = context.getParameter("password");

        // Resolve full URL
        String url = BASE_URL + endpoint;
        if (!bookingId.isEmpty() && endpoint.equals("/booking")) {
            url += "/" + bookingId;
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpRequestBase request;

            switch (method) {
                case "GET":
                    request = new HttpGet(url);
                    break;
                case "POST":
                    request = new HttpPost(url);
                    ((HttpPost) request).setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
                    break;
                case "PUT":
                    request = new HttpPut(url);
                    ((HttpPut) request).setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
                    break;
                case "PATCH":
                    request = new HttpPatch(url);
                    ((HttpPatch) request).setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
                    break;
                case "DELETE":
                    request = new HttpDelete(url);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP method: " + method);
            }

            // Set headers
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            // Auth token if needed (for PUT, PATCH, DELETE)
            if (method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE")) {
                String token = getAuthToken(client, username, password);
                request.setHeader("Cookie", "token=" + token);
            }

            HttpResponse response = client.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            int statusCode = response.getStatusLine().getStatusCode();

            result.setResponseData(responseBody, StandardCharsets.UTF_8.name());
            result.setResponseCode(String.valueOf(statusCode));
            result.setSuccessful(statusCode >= 200 && statusCode < 300);

        } catch (Exception e) {
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e.getMessage());
            result.setResponseData(new byte[0]);
        } finally {
            result.sampleEnd();
        }

        return result;
    }

    private String getAuthToken(CloseableHttpClient client, String username, String password) throws IOException {
        HttpPost authRequest = new HttpPost(BASE_URL + "/auth");
        JSONObject authJson = new JSONObject();
        authJson.put("username", username);
        authJson.put("password", password);
        authRequest.setEntity(new StringEntity(authJson.toString(), StandardCharsets.UTF_8));
        authRequest.setHeader("Content-Type", "application/json");

        HttpResponse authResponse = client.execute(authRequest);
        String authBody = EntityUtils.toString(authResponse.getEntity(), StandardCharsets.UTF_8);
        JSONObject authObj = new JSONObject(authBody);
        return authObj.getString("token");
    }
}