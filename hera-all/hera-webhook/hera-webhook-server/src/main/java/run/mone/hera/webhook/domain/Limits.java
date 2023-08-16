package run.mone.hera.webhook.domain;

import lombok.Data;

@Data
public class Limits {
    private String cpu;
    private String memory;
}
