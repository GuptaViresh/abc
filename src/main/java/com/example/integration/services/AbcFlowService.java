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
            _step1_log_message(context);
            FlowBridgeDebugHook.after("step-1782844699536", "Log Message", context);
        } catch (Exception _fbe_step1) {
            FlowBridgeDebugHook.error("step-1782844699536", "Log Message", _fbe_step1, context);
            if (_fbe_step1 instanceof RuntimeException _rte) throw _rte;
            throw new RuntimeException(_fbe_step1);
        }

        // Step 2: Set Payload
        FlowBridgeDebugHook.before("step-1782844519858", "Set Payload", context);
        try {
            _step2_set_payload(context);
            FlowBridgeDebugHook.after("step-1782844519858", "Set Payload", context);
        } catch (Exception _fbe_step2) {
            FlowBridgeDebugHook.error("step-1782844519858", "Set Payload", _fbe_step2, context);
            if (_fbe_step2 instanceof RuntimeException _rte) throw _rte;
            throw new RuntimeException(_fbe_step2);
        }

        return context;
    }

    private void _step1_log_message(ExecutionContext context) throws Exception {
        Object payload = context.getPayload();
        log.info("[{}] Hello", "Log Message");
    }

    private void _step2_set_payload(ExecutionContext context) throws Exception {
        Object payload = context.getPayload();
        context.setPayload("Hello World!");
        payload = context.getPayload();
    }
}
