package vesselems.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import vesselems.model.Model;
import vesselems.repository.ModelRepository;

@Service
public class ModelService {

    private final ModelRepository modelRepository;

    public ModelService(ModelRepository modelRepository) {
        this.modelRepository = modelRepository;
    }

    public List<Model> listModels() {
        return modelRepository.findAll();
    }

    public long count() {
        return modelRepository.count();
    }

    public Model getById(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在"));
    }

    public Model create(Model m) {
        m.setStatus(m.getStatus() != null ? m.getStatus() : 1);
        m.setCreateTime(LocalDateTime.now());
        return modelRepository.save(m);
    }

    public Model update(Long id, Model m) {
        Model exist = getById(id);
        if (m.getName() != null)
            exist.setName(m.getName());
        if (m.getApiUrl() != null)
            exist.setApiUrl(m.getApiUrl());
        if (m.getApiKey() != null)
            exist.setApiKey(m.getApiKey());
        if (m.getModelId() != null)
            exist.setModelId(m.getModelId());
        if (m.getVersion() != null)
            exist.setVersion(m.getVersion());
        if (m.getModelType() != null)
            exist.setModelType(m.getModelType());
        if (m.getStatus() != null)
            exist.setStatus(m.getStatus());
        return modelRepository.save(exist);
    }

    public void deleteById(Long id) {
        modelRepository.deleteById(id);
    }
}