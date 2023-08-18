package run.mone.hera.webhook.domain;

import io.fabric8.kubernetes.api.model.EnvVar;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Container {
    private String name;
    private String image;
    private Resource resources;
    private List<VolumeMount> volumeMounts;
    private List<EnvVar> env;
}
