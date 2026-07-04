package local.wifiadmin.sync;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "wifi-sync")
record WifiSyncProperties(
        @NotBlank String cron,
        @Min(1) int cpeCount
) {

    WifiSyncProperties {
        if (cron != null && !cron.isBlank()) {
            CronExpression.parse(cron);
        }
    }
}
