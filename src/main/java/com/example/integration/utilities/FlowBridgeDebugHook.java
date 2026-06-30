package com.example.integration.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Calls back to FlowBridge IDE before/after each step during debug mode.
 * Disabled unless flowbridge.debug.enabled=true.
 */
public final class FlowBridgeDebugHook {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final boolean ENABLED =
            "true".equalsIgnoreCase(System.getProperty("flowbridge.debug.enabled", "false"));
    private static final String PROJECT_ID =
            System.getProperty("flowbridge.debug.projectId", "");
    private static final String SERVER =
            System.getProperty("flowbridge.debug.server", "http://localhost:8080");
    private static final String TOKEN =
            System.getProperty("flowbridge.debug.token", "");

    private FlowBridgeDebugHook() {}

    /** Called BEFORE a step executes. Sends payload snapshot and blocks until IDE resumes. */
    public static void before(String stepId, String stepName, ExecutionContext context) {
        if (!ENABLED || PROJECT_ID.isBlank()) return;
        callAndBlock("before", stepId, stepName, context);
    }

    /** Called AFTER a step executes. Sends updated payload snapshot (non-blocking). */
    public static void after(String stepId, String stepName, ExecutionContext context) {
        if (!ENABLED || PROJECT_ID.isBlank()) return;
        callAndBlock("after", stepId, stepName, context);
    }

    /** Called when a step throws — sends a structured error event to the IDE (fire-and-forget). */
    public static void error(String stepId, String stepName, Throwable error, ExecutionContext context) {
        if (!ENABLED || PROJECT_ID.isBlank()) return;
        try {
            String errorType = classifyError(error);
            String[] parts   = errorType.split(":", 2);
            String ns        = parts[0];
            String code      = parts.length > 1 ? parts[1] : "UNKNOWN";

            java.util.List<Map<String, Object>> chain = new java.util.ArrayList<>();
            Throwable t = error;
            while (t != null) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("type",    t.getClass().getSimpleName());
                entry.put("message", t.getMessage() != null ? t.getMessage() : "");
                chain.add(entry);
                t = t.getCause();
            }

            java.io.StringWriter sw = new java.io.StringWriter();
            error.printStackTrace(new java.io.PrintWriter(sw));

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("phase",          "error");
            body.put("stepId",         stepId);
            body.put("stepName",       stepName);
            body.put("error",          error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName());
            body.put("errorType",      errorType);
            body.put("errorNamespace", ns);
            body.put("errorCode",      code);
            body.put("componentName",  stepName);
            body.put("causeChain",     chain);
            body.put("stackTrace",     sw.toString());
            body.put("payload",        context.getPayload());
            body.put("variables",      context.getVariables());

            String json = MAPPER.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER + "/api/v1/debug/" + PROJECT_ID + "/step"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TOKEN)
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            // Async — don't block; the caller is about to re-throw
            HTTP.sendAsync(req, HttpResponse.BodyHandlers.discarding());
        } catch (Exception ignored) {
            System.err.println("[FlowBridgeDebugHook] Failed to send error event: " + ignored.getMessage());
        }
    }

    /** Classifies a Throwable into a FlowBridge NAMESPACE:CODE error type. */
    private static String classifyError(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) root = root.getCause();
        String cn  = e.getClass().getName().toLowerCase();
        String rcn = root.getClass().getName().toLowerCase();
        String msg = (e.getMessage() != null ? e.getMessage() : "").toLowerCase();
        // SFTP / JSch
        if (cn.contains("jsch") || cn.contains("sftp") || rcn.contains("jsch")) {
            if (rcn.contains("connectexception") || msg.contains("connection refused") || msg.contains("no route to host"))
                return "SFTP:CONNECTIVITY";
            if (msg.contains("auth") || msg.contains("credential") || msg.contains("invalid user"))
                return "SFTP:INVALID_CREDENTIALS";
            if (msg.contains("no such file") || msg.contains("file not found"))
                return "SFTP:FILE_NOT_FOUND";
            if (msg.contains("permission") || msg.contains("denied"))
                return "SFTP:PERMISSION_DENIED";
            if (msg.contains("timeout"))
                return "SFTP:TIMEOUT";
            return "SFTP:UNKNOWN";
        }
        // HTTP / network
        if (rcn.contains("connectexception") || rcn.contains("unknownhostexception"))
            return "HTTP:CONNECTIVITY";
        if (rcn.contains("sockettimeoutexception") || rcn.contains("httptimeoutexception"))
            return "HTTP:TIMEOUT";
        // Database
        if (cn.contains("sqlexception") || cn.contains("datasource") || cn.contains("jdbc"))
            return "DB:QUERY_EXECUTION";
        // Transform / expression
        if (cn.contains("jsonata") || cn.contains("evaluateexception") || cn.contains("parseexception"))
            return "TRANSFORM:EXPRESSION_EVALUATION";
        // Salesforce
        if (cn.contains("salesforceexception"))
            return "SALESFORCE:QUERY_EXECUTION";
        return "CORE:UNKNOWN";
    }

    @SuppressWarnings("unchecked")
    private static void callAndBlock(String phase, String stepId, String stepName, ExecutionContext ctx) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("phase",     phase);
            body.put("stepId",    stepId);
            body.put("stepName",  stepName);
            body.put("payload",   ctx.getPayload());
            body.put("variables", ctx.getVariables());
            body.put("headers",   ctx.getHeaders());

            String json = MAPPER.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER + "/api/v1/debug/" + PROJECT_ID + "/step"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TOKEN)
                    // Long timeout — this blocks until the user clicks Step/Resume
                    .timeout(Duration.ofMinutes(30))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            // Synchronous — blocks until FlowBridge IDE responds
            HTTP.send(req, HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Non-fatal — if debug server is unreachable, just continue
            System.err.println("[FlowBridgeDebugHook] Warning: " + e.getMessage());
        }
    }
}
