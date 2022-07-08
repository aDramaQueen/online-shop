package com.acme.onlineshop.condition;

import com.acme.onlineshop.Constants;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SecurityActiveCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (Constants.WEB_SECURITY) {
            return ConditionEvaluationResult.enabled("WebSecurity is active");
        } else {
            return ConditionEvaluationResult.disabled("WebSecurity is NOT active");
        }
    }

}
