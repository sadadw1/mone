package run.mone.hera.webhook.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Resource {
    private Limits limits;
    private Requests requests;

}
