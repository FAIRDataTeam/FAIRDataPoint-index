package solutions.fairdata.fdp.index.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import solutions.fairdata.fdp.index.entity.webhooks.WebhookEvent;

@Data
@NoArgsConstructor
public class WebhookPayloadDTO {
    private WebhookEvent event;
    private String uuid;
    private String clientUrl;
    private String timestamp;
    private String secret;
}
