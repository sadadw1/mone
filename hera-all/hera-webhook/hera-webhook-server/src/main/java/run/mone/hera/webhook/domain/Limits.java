package run.mone.hera.webhook.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Limits {
    private String cpu;
    private String memory;
}
