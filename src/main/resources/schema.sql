CREATE TABLE IF NOT EXISTS wifi_configuration (
    cpe_id VARCHAR(64) PRIMARY KEY,
    wifi_band VARCHAR(32) NOT NULL,
    ssid VARCHAR(255) NOT NULL,
    encryption_type VARCHAR(32),
    password VARCHAR(255)
);
