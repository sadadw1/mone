package run.mone.mcp.multimodal.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import run.mone.hive.llm.LLM;
import run.mone.hive.mcp.server.McpServer;
import run.mone.hive.mcp.server.McpServer.ToolRegistration;
import run.mone.hive.mcp.server.McpSyncServer;
import run.mone.hive.mcp.spec.McpSchema.ServerCapabilities;
import run.mone.hive.mcp.spec.McpSchema.Tool;
import run.mone.hive.mcp.spec.ServerMcpTransport;
import run.mone.mcp.multimodal.function.*;

@Slf4j
@Component
public class MultimodalMcpServer {
    private final ServerMcpTransport transport;
    private final ObjectMapper objectMapper;
    private McpSyncServer syncServer;

    @Resource(name = "imageLLM")
    private LLM imageLLM;
    @Resource(name = "asrLLM")
    private LLM asrLLM;
    @Resource(name = "ttsLLM")
    private LLM ttsLLM;

    public MultimodalMcpServer(ServerMcpTransport transport, ObjectMapper objectMapper) {
        this.transport = transport;
        this.objectMapper = objectMapper;
    }

    public McpSyncServer start() {
        log.info("Starting MultimodalMcpServer...");
        McpSyncServer syncServer = McpServer.using(transport)
                .serverInfo("multimodal_mcp_server", "1.0.0")
                .capabilities(ServerCapabilities.builder()
                        .tools(true)
                        .logging()
                        .build())
                .sync();

        log.info("Registering multimodal tools...");
        try {
            // 注册图像分析功能
            ImageAnalysisFunction imageAnalysisFunction = new ImageAnalysisFunction(objectMapper, imageLLM);
            var imageAnalysisRegistration = new ToolRegistration(
                    new Tool(imageAnalysisFunction.getName(),
                            imageAnalysisFunction.getDesc(),
                            imageAnalysisFunction.getToolScheme()),
                    imageAnalysisFunction
            );
            syncServer.addTool(imageAnalysisRegistration);
            log.info("Successfully registered ImageAnalysis tool");

            // 注册语音识别功能
            SpeechRecognitionFunction speechRecognitionFunction = new SpeechRecognitionFunction(objectMapper);
            var speechRecognitionRegistration = new ToolRegistration(
                    new Tool(speechRecognitionFunction.getName(),
                            speechRecognitionFunction.getDesc(),
                            speechRecognitionFunction.getToolScheme()),
                    speechRecognitionFunction
            );
            syncServer.addTool(speechRecognitionRegistration);
            log.info("Successfully registered SpeechRecognition tool");

            // 注册视频处理功能
            VideoProcessingFunction videoProcessingFunction = new VideoProcessingFunction(objectMapper);
            var videoProcessingRegistration = new ToolRegistration(
                    new Tool(videoProcessingFunction.getName(),
                            videoProcessingFunction.getDesc(),
                            videoProcessingFunction.getToolScheme()),
                    videoProcessingFunction
            );
            syncServer.addTool(videoProcessingRegistration);
            log.info("Successfully registered VideoProcessing tool");

        } catch (Exception e) {
            log.error("Failed to register multimodal tools", e);
            throw e;
        }

        return syncServer;
    }

    @PostConstruct
    public void init() {
        this.syncServer = start();
    }

    @PreDestroy
    public void stop() {
        if (this.syncServer != null) {
            log.info("Stopping MultimodalMcpServer...");
            this.syncServer.closeGracefully();
        }
    }
} 