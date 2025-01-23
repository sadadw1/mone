
package run.mone.hive.llm;

import lombok.Getter;

@Getter
public enum LLMProvider {

    STEPFUN("https://api.stepfun.com/v1/chat/completions", "STEPFUN_API_KEY", "step-1-8k", null),
    GOOGLE("https://generativelanguage.googleapis.com/v1beta/openai/chat/completions", "GOOGLE_API_KEY", "gemini-2.0-flash-exp", null),
    DEEPSEEK("https://api.deepseek.com/v1/chat/completions", "DEEPSEEK_API_KEY", "deepseek-chat", null),
    OPENROUTER("https://openrouter.ai/api/v1/chat/completions", "OPENROUTER_API_KEY", "anthropic/claude-3.5-sonnet:beta", null),
    DOUBAO("https://ark.cn-beijing.volces.com/api/v3/chat/completions", "DOUBAO_API_KEY", null, "DOUBAO_MODEL_KEY");

    private final String url;

    private final String envName;

    private final String defaultModel;

    // 允许在环境变量中设置模型名称
    private final String customModelEnv;

    LLMProvider(String url, String envName, String defaultModel, String customModelEnv) {
        this.url = url;
        this.envName = envName;
        if (customModelEnv != null) {
            this.defaultModel = System.getenv(customModelEnv);
        } else {
            this.defaultModel = defaultModel;
        }
        this.customModelEnv = customModelEnv;
    }
}
