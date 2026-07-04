package local.wifiadmin.sync;

import local.wifiadmin.api.model.WifiConfiguration;
import local.wifiadmin.persistence.WifiConfigurationRepository;
import local.wifiadmin.platform.WifiPlatformClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class WifiConfigurationSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(WifiConfigurationSyncScheduler.class);

    private final WifiPlatformClient platformClient;
    private final WifiConfigurationRepository repository;
    private final WifiSyncProperties properties;

    WifiConfigurationSyncScheduler(
            WifiPlatformClient platformClient,
            WifiConfigurationRepository repository,
            WifiSyncProperties properties
    ) {
        this.platformClient = platformClient;
        this.repository = repository;
        this.properties = properties;
    }

    @Scheduled(cron = "${wifi-sync.cron}")
    void synchronizeConfiguredCpes() {
        int successful = 0;
        int failed = 0;

        log.info("Starting WiFi configuration sync for {} CPE devices", properties.cpeCount());
        for (int index = 1; index <= properties.cpeCount(); index++) {
            String cpeId = cpeId(index);
            try {
                WifiConfiguration configuration = platformClient.getWifiConfiguration(cpeId);
                repository.save(configuration);
                successful++;
            } catch (RuntimeException exception) {
                failed++;
                log.warn("Failed to sync WiFi configuration for cpeId {}: {}", cpeId, exception.getMessage());
            }
        }

        log.info("Finished WiFi configuration sync: successful={}, failed={}", successful, failed);
    }

    private String cpeId(int index) {
        return "CPE_%03d".formatted(index);
    }
}
