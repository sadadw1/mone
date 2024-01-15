package run.mone.hera.webhook.config;

import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Base64Utils;
import run.mone.hera.webhook.common.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Configuration
public class HttpsConfiguration {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            byte[] base64Ori = FileUtils.fileToByteArray("/tmp/hera-webhook-server-p12/hera-webhook-server.p12");
            // Decode the Base64-encoded certificate content
            byte[] certificateBytes = Base64.decodeBase64(base64Ori);

            // Save the certificate to a file
            String certificateFilePath = "/tmp/certificate.p12"; // Specify the desired file path
            try (FileOutputStream fos = new FileOutputStream(certificateFilePath)) {
                fos.write(certificateBytes);
            } catch (IOException e) {
                throw new RuntimeException("Error saving the certificate to file", e);
            }

            // Set the SSL configuration with the decoded certificate
            Ssl ssl = new Ssl();
            ssl.setEnabled(true);
            ssl.setKeyStoreType("PKCS12");
            ssl.setKeyStorePassword("mone");
            ssl.setKeyStore(certificateFilePath);
            factory.setSsl(ssl);
        };
    }
}
