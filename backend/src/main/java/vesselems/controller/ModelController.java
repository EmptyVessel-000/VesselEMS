package vesselems.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import vesselems.annotation.OperateLog;
import vesselems.common.ApiResponse;
import vesselems.model.Model;
import vesselems.service.ModelService;

@RestController
@RequestMapping("/api/model")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping
    public ApiResponse<List<Model>> list() {
        return ApiResponse.success(modelService.listModels());
    }

    @GetMapping("/{id}")
    public ApiResponse<Model> get(@PathVariable Long id) {
        return ApiResponse.success(modelService.getById(id));
    }

    @PostMapping
    @OperateLog(module = "模型管理", operation = "新增模型")
    public ApiResponse<Model> create(@RequestBody Model m) {
        return ApiResponse.success(modelService.create(m));
    }

    @PutMapping("/{id}")
    @OperateLog(module = "模型管理", operation = "修改模型")
    public ApiResponse<Model> update(@PathVariable Long id, @RequestBody Model m) {
        return ApiResponse.success(modelService.update(id, m));
    }

    @DeleteMapping("/{id}")
    @OperateLog(module = "模型管理", operation = "删除模型")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        modelService.deleteById(id);
        return ApiResponse.success(null);
    }
}