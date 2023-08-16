package run.mone.hera.webhook.domain;

import lombok.Data;

@Data
public class Resource {
    private Limits limits;
    private Requests requests;

}
