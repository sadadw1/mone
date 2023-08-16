package run.mone.hera.webhook.domain;

import lombok.Data;

@Data
public class VolumeMount {
    private String name;
    private String mountPath;
    private String subPathExpr;
}
