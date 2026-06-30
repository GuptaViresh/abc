package com.example.integration.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.example.integration.utilities.ExecutionContext;
import com.example.integration.services.AbcFlowService;

@RestController
public class IntegrationController {

    private final AbcFlowService abcFlowService;

    public IntegrationController(AbcFlowService abcFlowService) {
        this.abcFlowService = abcFlowService;
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> handleFlowError(Exception e) {
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("status", 500);
        body.put("message", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        java.util.List<java.util.Map<String, String>> causes = new java.util.ArrayList<>();
        Throwable t = e;
        while (t != null) {
            java.util.Map<String, String> entry = new java.util.LinkedHashMap<>();
            entry.put("type",    t.getClass().getSimpleName());
            entry.put("message", t.getMessage() != null ? t.getMessage() : "");
            causes.add(entry);
            t = t.getCause();
        }
        body.put("causeChain", causes);
        return org.springframework.http.ResponseEntity.status(500).body(body);
    }

    @PostMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> abcHandle(@RequestBody(required = false) Object request, HttpServletRequest httpRequest) throws Exception {
        ExecutionContext context = new ExecutionContext(request);
        java.util.Enumeration<String> _names = httpRequest.getHeaderNames();
        while (_names != null && _names.hasMoreElements()) {
            String _h = _names.nextElement();
            context.setHeader(_h, httpRequest.getHeader(_h));
        }
        ExecutionContext result = abcFlowService.execute(context);
        return ResponseEntity.ok(result.getPayload());
    }
}
