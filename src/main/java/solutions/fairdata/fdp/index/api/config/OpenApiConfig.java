package solutions.fairdata.fdp.index.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Autowired
    BuildProperties buildProperties;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(
                        new Info().title(
                                "FAIR Data Point Index API"
                        ).description(
                                "This is OpenAPI documentation of FAIR Data Point Index REST API."
                        ).version(
                                buildProperties.getVersion()
                        ).license(
                                new License().name("MIT")
                        )
                );
    }
}
