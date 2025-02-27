package run.mone.mcp.multimodal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.mone.hive.llm.LLM;
import run.mone.hive.configs.LLMConfig;
import run.mone.hive.llm.LLMProvider;

@Configuration
public class MultiModalConfig {

    @Bean("imageLLM")
    public LLM imageLLM() {
        LLMConfig config = LLMConfig.builder()
            .llmProvider(LLMProvider.GOOGLE_2) // 默认使用Google的Gemini
            .build();
        return new LLM(config);
    }

    @Bean("asrLLM")
    public LLM asrLLM() {
        LLMConfig config = LLMConfig.builder()
            .llmProvider(LLMProvider.STEPFUN_ASR)
            .build();
        return new LLM(config);
    }

    @Bean("ttsLLM")
    public LLM ttsLLM() {
        LLMConfig config = LLMConfig.builder()
            .llmProvider(LLMProvider.STEPFUN_TTS)
            .build();
        return new LLM(config);
    }
}