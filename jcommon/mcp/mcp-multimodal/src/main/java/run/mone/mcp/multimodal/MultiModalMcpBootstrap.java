package run.mone.mcp.multimodal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"run.mone.mcp.multimodal"})
@Slf4j
public class MultiModalMcpBootstrap {

    public static void main(String[] args) {
        log.info("Starting multi modal MCP Server...");
        SpringApplication.run(MultiModalMcpBootstrap.class, args);
    }
}
