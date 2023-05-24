package com.xebia.functional.xef;

import com.xebia.functional.xef.auto.AIScope;
import com.xebia.functional.xef.auto.Agent;
import com.xebia.functional.xef.llm.openai.LLMModel;
import kotlin.coroutines.Continuation;
import kotlin.jvm.functions.Function1;
import kotlinx.serialization.descriptors.SerialDescriptor;

public class AgentKt {
    public <A> A prompt(
            AIScope scope,
            String prompt,
            SerialDescriptor descriptor,
            Function1<? super String, ? extends A> parser,
            int maxAttempts,
            LLMModel model,
            String user,
            boolean echo,
            int n,
            double temperature,
            int bringFromContext,
            int minResponseToken,
            Continuation<? super A> continuation
    ) {
        return Agent.<A>prompt(
                scope,
                prompt,
                descriptor,
                parser,
                maxAttempts,
                model,
                user,
                echo,
                n,
                temperature,
                bringFromContext,
                minResponseToken,
                continuation
        );
    }
}
