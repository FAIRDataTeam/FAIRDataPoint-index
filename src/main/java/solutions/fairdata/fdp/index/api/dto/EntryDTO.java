package solutions.fairdata.fdp.index.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EntryDTO {
    private String clientUrl;
    private String registrationTime;
    private String modificationTime;
}
