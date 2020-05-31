package solutions.fairdata.fdp.index;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(Profiles.TESTING)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureMockMvc
public abstract class WebIntegrationTest {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected TestRestTemplate client;
}
