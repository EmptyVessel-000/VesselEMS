package vesselems.service;

import java.util.List;

import org.springframework.stereotype.Service;

import vesselems.model.Config;
import vesselems.repository.ConfigRepository;

@Service
public class ConfigService {

    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    public List<Config> listConfigs() {
        return configRepository.findAll();
    }

    public Config getConfigById(Long id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("配置不存在: " + id));
    }

    public Config createConfig(Config config) {
        if (configRepository.existsByConfigKey(config.getConfigKey())) {
            throw new IllegalArgumentException("配置键已存在: " + config.getConfigKey());
        }
        config.setStatus(config.getStatus() != null ? config.getStatus() : 1);
        return configRepository.save(config);
    }

    public Config updateConfig(Long id, Config updated) {
        Config config = getConfigById(id);
        if (updated.getConfigKey() != null && !updated.getConfigKey().equals(config.getConfigKey())) {
            if (configRepository.existsByConfigKey(updated.getConfigKey())) {
                throw new IllegalArgumentException("配置键已存在: " + updated.getConfigKey());
            }
            config.setConfigKey(updated.getConfigKey());
        }
        if (updated.getConfigValue() != null)
            config.setConfigValue(updated.getConfigValue());
        if (updated.getConfigType() != null)
            config.setConfigType(updated.getConfigType());
        if (updated.getDescription() != null)
            config.setDescription(updated.getDescription());
        if (updated.getSortOrder() != null)
            config.setSortOrder(updated.getSortOrder());
        if (updated.getStatus() != null)
            config.setStatus(updated.getStatus());
        return configRepository.save(config);
    }

    public void deleteConfig(Long id) {
        if (!configRepository.existsById(id)) {
            throw new IllegalArgumentException("配置不存在: " + id);
        }
        configRepository.deleteById(id);
    }

    public long count() {
        return configRepository.count();
    }
}