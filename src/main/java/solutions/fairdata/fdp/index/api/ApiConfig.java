package solutions.fairdata.fdp.index.api;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import solutions.fairdata.fdp.index.service.ServiceConfig;

@Configuration
@ComponentScan
@Import(ServiceConfig.class)
public class ApiConfig {
}
