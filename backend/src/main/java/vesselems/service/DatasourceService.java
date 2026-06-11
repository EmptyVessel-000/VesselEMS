package vesselems.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import vesselems.model.Datasource;
import vesselems.repository.DatasourceRepository;

@Service
public class DatasourceService {

    private final DatasourceRepository datasourceRepository;

    public DatasourceService(DatasourceRepository datasourceRepository) {
        this.datasourceRepository = datasourceRepository;
    }

    public List<Datasource> listDatasources() {
        return datasourceRepository.findAll();
    }

    public Datasource getById(Long id) {
        return datasourceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("数据源不存在"));
    }

    public Datasource create(Datasource ds) {
        ds.setStatus(ds.getStatus() != null ? ds.getStatus() : 1);
        ds.setCreateTime(LocalDateTime.now());
        return datasourceRepository.save(ds);
    }

    public Datasource update(Long id, Datasource ds) {
        Datasource exist = getById(id);
        if (ds.getName() != null)
            exist.setName(ds.getName());
        if (ds.getDbType() != null)
            exist.setDbType(ds.getDbType());
        if (ds.getHost() != null)
            exist.setHost(ds.getHost());
        if (ds.getPort() != null)
            exist.setPort(ds.getPort());
        if (ds.getDatabaseName() != null)
            exist.setDatabaseName(ds.getDatabaseName());
        if (ds.getUsername() != null)
            exist.setUsername(ds.getUsername());
        if (ds.getPassword() != null)
            exist.setPassword(ds.getPassword());
        if (ds.getStatus() != null)
            exist.setStatus(ds.getStatus());
        return datasourceRepository.save(exist);
    }

    public void deleteById(Long id) {
        datasourceRepository.deleteById(id);
    }
}