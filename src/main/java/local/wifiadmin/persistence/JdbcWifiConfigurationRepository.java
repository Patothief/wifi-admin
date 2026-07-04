package local.wifiadmin.persistence;

import local.wifiadmin.api.model.EncryptionType;
import local.wifiadmin.api.model.WifiBand;
import local.wifiadmin.api.model.WifiConfiguration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
class JdbcWifiConfigurationRepository implements WifiConfigurationRepository {

    private final JdbcClient jdbcClient;

    JdbcWifiConfigurationRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<WifiConfiguration> findByCpeId(String cpeId) {
        return jdbcClient.sql("""
                        SELECT cpe_id, wifi_band, ssid, encryption_type, password
                        FROM wifi_configuration
                        WHERE cpe_id = :cpeId
                        """)
                .param("cpeId", cpeId)
                .query(this::mapRow)
                .optional();
    }

    @Override
    public WifiConfiguration save(WifiConfiguration configuration) {
        int updatedRows = jdbcClient.sql("""
                        UPDATE wifi_configuration
                        SET wifi_band = :wifiBand,
                            ssid = :ssid,
                            encryption_type = :encryptionType,
                            password = :password
                        WHERE cpe_id = :cpeId
                        """)
                .params(configurationParameters(configuration))
                .update();

        if (updatedRows == 0) {
            jdbcClient.sql("""
                            INSERT INTO wifi_configuration (cpe_id, wifi_band, ssid, encryption_type, password)
                            VALUES (:cpeId, :wifiBand, :ssid, :encryptionType, :password)
                            """)
                    .params(configurationParameters(configuration))
                    .update();
        }

        return configuration;
    }

    private WifiConfiguration mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
        WifiConfiguration configuration = new WifiConfiguration(
                resultSet.getString("cpe_id"),
                WifiBand.fromValue(resultSet.getString("wifi_band")),
                resultSet.getString("ssid")
        );

        String encryptionType = resultSet.getString("encryption_type");
        if (encryptionType != null) {
            configuration.setEncryptionType(EncryptionType.fromValue(encryptionType));
        }

        configuration.setPassword(resultSet.getString("password"));
        return configuration;
    }

    private java.util.Map<String, ?> configurationParameters(WifiConfiguration configuration) {
        EncryptionType encryptionType = configuration.getEncryptionType();
        java.util.Map<String, Object> parameters = new java.util.HashMap<>();
        parameters.put("cpeId", configuration.getCpeId());
        parameters.put("wifiBand", configuration.getWifiBand().getValue());
        parameters.put("ssid", configuration.getSsid());
        parameters.put("encryptionType", encryptionType == null ? null : encryptionType.getValue());
        parameters.put("password", configuration.getPassword());
        return parameters;
    }
}
