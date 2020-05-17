package solutions.fairdata.fdp.index.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Schema(name = "Entry")
public class EntryDTO {
    @NotNull
    @URL
    private String clientUrl;

    @NotNull
    private OffsetDateTime registrationTime;

    @NotNull
    private OffsetDateTime modificationTime;
}
