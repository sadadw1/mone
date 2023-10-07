package run.mone.hera.webhook.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class TpcAppInfo {

    private Long id;

    private Long outId;

    private String name;

    private String envName;

    private Long envId;

    // eg: 2-hera-demo-client
    private String idAndName;
}
