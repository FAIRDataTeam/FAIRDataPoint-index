package solutions.fairdata.fdp.index.entity.config;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Builder
@Data
public class EventsConfig {
    private final Duration retrievalRateLimitWait;
    private final Duration retrievalTimeout;
    private final Duration pingValidDuration;
}
