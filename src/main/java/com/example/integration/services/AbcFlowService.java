package com.example.integration.services;

import org.springframework.stereotype.Service;
import com.example.integration.utilities.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.integration.utilities.FlowBridgeDebugHook;

@Service
public class AbcFlowService {

    private static final Logger log = LoggerFactory.getLogger(AbcFlowService.class);

    public ExecutionContext execute(ExecutionContext context) throws Exception {

        // Step 1: Log Message
        FlowBridgeDebugHook.before("step-1782844699536", "Log Message", context);
        try {
            logMessage(context);
            FlowBridgeDebugHook.after("step-1782844699536", "Log Message", context);
        } catch (Exception _fbe_step1) {
            FlowBridgeDebugHook.error("step-1782844699536", "Log Message", _fbe_step1, context);
            if (_fbe_step1 instanceof RuntimeException _rte) throw _rte;
            throw new RuntimeException(_fbe_step1);
        }

        // Step 2: Set Payload
        FlowBridgeDebugHook.before("step-1782844519858", "Set Payload", context);
        try {
            setPayload(context);
            FlowBridgeDebugHook.after("step-1782844519858", "Set Payload", context);
        } catch (Exception _fbe_step2) {
            FlowBridgeDebugHook.error("step-1782844519858", "Set Payload", _fbe_step2, context);
            if (_fbe_step2 instanceof RuntimeException _rte) throw _rte;
            throw new RuntimeException(_fbe_step2);
        }

        // Step 3: JSON Logger
        FlowBridgeDebugHook.before("step-1782896452718", "JSON Logger", context);
        try {
            jsonLogger1(context);
            FlowBridgeDebugHook.after("step-1782896452718", "JSON Logger", context);
        } catch (Exception _fbe_step3) {
            FlowBridgeDebugHook.error("step-1782896452718", "JSON Logger", _fbe_step3, context);
            if (_fbe_step3 instanceof RuntimeException _rte) throw _rte;
            throw new RuntimeException(_fbe_step3);
        }

        // Step 4: JSON Logger
        FlowBridgeDebugHook.before("step-1782896585805", "JSON Logger", context);
        try {
            jsonLogger2(context);
            FlowBridgeDebugHook.after("step-1782896585805", "JSON Logger", context);
        } catch (Exception _fbe_step4) {
            FlowBridgeDebugHook.error("step-1782896585805", "JSON Logger", _fbe_step4, context);
            if (_fbe_step4 instanceof RuntimeException _rte) throw _rte;
            throw new RuntimeException(_fbe_step4);
        }

        return context;
    }

    private void logMessage(ExecutionContext context) throws Exception {
        log.info("[{}] Hello", "Log Message");
    }

    private void setPayload(ExecutionContext context) throws Exception {
        Object payload = context.getPayload();
        context.setPayload("Hello World!");
        payload = context.getPayload();
    }

    private void jsonLogger1(ExecutionContext context) throws Exception {
        {
            var mapper   = new com.fasterxml.jackson.databind.ObjectMapper();
            var logEntry = mapper.createObjectNode();
            logEntry.put("timestamp",     java.time.Instant.now().toString());
            logEntry.put("level",         "INFO");
            logEntry.put("correlationId", java.util.Optional.ofNullable(context.getHeader("x-correlation-id")).orElseGet(() -> java.util.UUID.randomUUID().toString()));
            logEntry.put("message",    "dsf");
            logEntry.put("content", "sdf");
            log.info("{}", logEntry);
        }
    }

    private void jsonLogger2(ExecutionContext context) throws Exception {
        {
            var mapper   = new com.fasterxml.jackson.databind.ObjectMapper();
            var logEntry = mapper.createObjectNode();
            logEntry.put("timestamp",     java.time.Instant.now().toString());
            logEntry.put("level",         "INFO");
            logEntry.put("correlationId", java.util.Optional.ofNullable(context.getHeader("x-correlation-id")).orElseGet(() -> java.util.UUID.randomUUID().toString()));
            logEntry.put("message",    "fdgd");
            logEntry.put("tracePoint", "dfg");
            logEntry.put("content", "dfg");
            log.info("{}", logEntry);
        }
    }
}
