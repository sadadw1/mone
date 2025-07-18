package run.mone.hive.mcp.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author goodjava@qq.com
 * @date 2025/6/6 10:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rag {

    private boolean autoRag;
    private String version;
    private String modelType;
    private String releaseServiceName;
}
