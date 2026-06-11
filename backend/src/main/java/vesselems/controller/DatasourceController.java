package vesselems.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.common.ApiResponse;
import vesselems.model.Datasource;
import vesselems.service.DSManager;
import vesselems.service.DatasourceService;
import vesselems.service.SchemaService;

@RestController
@RequestMapping("/api/ds")
public class DatasourceController {

    private final DatasourceService datasourceService;
    private final DSManager dsManager;
    private final SchemaService schemaService;

    public DatasourceController(DatasourceService datasourceService, DSManager dsManager, SchemaService schemaService) {
        this.datasourceService = datasourceService;
        this.dsManager = dsManager;
        this.schemaService = schemaService;
    }

    @GetMapping
    public ApiResponse<List<Datasource>> list() {
        return ApiResponse.success(datasourceService.listDatasources());
    }

    @GetMapping("/{id}")
    public ApiResponse<Datasource> get(@PathVariable Long id) {
        return ApiResponse.success(datasourceService.getById(id));
    }

    @PostMapping
    public ApiResponse<Datasource> create(@RequestBody Datasource ds) {
        return ApiResponse.success(datasourceService.create(ds));
    }

    @PutMapping("/{id}")
    public ApiResponse<Datasource> update(@PathVariable Long id, @RequestBody Datasource ds) {
        Datasource updated = datasourceService.update(id, ds);
        dsManager.evict(id);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        dsManager.evict(id);
        datasourceService.deleteById(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/test")
    public ApiResponse<Boolean> test(@PathVariable Long id) {
        Datasource ds = datasourceService.getById(id);
        return ApiResponse.success(dsManager.test(ds));
    }

    @GetMapping("/{id}/schema")
    public ApiResponse<List<Map<String, Object>>> schema(@PathVariable Long id) {
        Datasource ds = datasourceService.getById(id);
        return ApiResponse.success(schemaService.getSchema(dsManager.get(ds)));
    }
}