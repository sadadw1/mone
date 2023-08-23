package run.mone.hera.webhook.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class VolumeMount {
    private String name;
    private String mountPath;
    private String subPathExpr;
    private String subPath;
}
