package com.example.integration.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carries all mutable state through a FlowBridge flow execution:
 * payload, variables (vars.*), headers, attributes, properties, and env.
 *
 * Every connector in a generated flow receives the same ExecutionContext instance
 * so that expressions such as {@code vars.orderId} or {@code headers.authorization}
 * resolve naturally via JSONata.
 */
public final class ExecutionContext {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Object                   payload;
    private final Map<String, Object> variables;
    private final Map<String, String> headers;
    private final Map<String, Object> attributes;
    private final Map<String, String> properties;
    private final Map<String, String> env;

    public ExecutionContext() {
        this.variables   = new LinkedHashMap<>();
        this.headers     = new LinkedHashMap<>();
        this.attributes  = new LinkedHashMap<>();
        this.properties  = new LinkedHashMap<>();
        this.env         = new LinkedHashMap<>(System.getenv());
    }

    public ExecutionContext(Object initialPayload) {
        this();
        this.payload = initialPayload;
    }

    // ── payload ───────────────────────────────────────────────────

    public Object getPayload() { return payload; }

    public void setPayload(Object payload) { this.payload = payload; }

    /** Returns the payload serialised as a Jackson {@link JsonNode}. */
    public JsonNode getPayloadAsNode() {
        return MAPPER.valueToTree(payload);
    }

    // ── variables (vars.*) ────────────────────────────────────────

    public Object getVariable(String name) { return variables.get(name); }

    public void setVariable(String name, Object value) { variables.put(name, value); }

    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    // ── headers ───────────────────────────────────────────────────

    public String getHeader(String name) { return headers.get(name); }

    public void setHeader(String name, String value) { headers.put(name, value); }

    public void setHeaders(Map<String, String> incoming) {
        if (incoming != null) headers.putAll(incoming);
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    // ── attributes ────────────────────────────────────────────────

    public Object getAttribute(String name) { return attributes.get(name); }

    public void setAttribute(String name, Object value) { attributes.put(name, value); }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    // ── properties ────────────────────────────────────────────────

    public String getProperty(String name) { return properties.get(name); }

    public void setProperty(String name, String value) { properties.put(name, value); }

    // ── environment ───────────────────────────────────────────────

    public String getEnv(String name) { return env.get(name); }

    // ── JSONata evaluation root ───────────────────────────────────

    /**
     * Serialises this context into the combined root object passed to JSONata:
     * <pre>
     * {
     *   "payload":    { ... },
     *   "vars":       { "orderId": 123, ... },
     *   "headers":    { "authorization": "Bearer ..." },
     *   "attributes": { ... },
     *   "properties": { ... },
     *   "env":        { "BASE_URL": "..." }
     * }
     * </pre>
     * Expressions written by FlowBridge users (e.g. {@code vars.orderId},
     * {@code headers.authorization}) resolve naturally against this root.
     */
    public JsonNode toEvaluationNode() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("payload",    payload);
        root.put("vars",       variables);
        root.put("headers",    headers);
        root.put("attributes", attributes);
        root.put("properties", properties);
        root.put("env",        env);
        return MAPPER.valueToTree(root);
    }

    // ── ForEach support ───────────────────────────────────────────

    /**
     * Creates a child context for a single ForEach iteration.
     * The child starts with {@code item} as its payload and inherits
     * vars, headers, attributes, and properties from the parent so that
     * expressions referencing {@code vars.*} still resolve inside the loop.
     */
    public ExecutionContext forItem(Object item) {
        ExecutionContext child = new ExecutionContext(item);
        child.variables.putAll(this.variables);
        child.headers.putAll(this.headers);
        child.attributes.putAll(this.attributes);
        child.properties.putAll(this.properties);
        return child;
    }
}
