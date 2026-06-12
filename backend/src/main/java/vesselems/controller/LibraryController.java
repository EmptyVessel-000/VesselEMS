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

import vesselems.common.ApiResponse;
import vesselems.model.Library;
import vesselems.service.LibraryService;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    public ApiResponse<List<Library>> list() {
        return ApiResponse.success(libraryService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<Library> get(@PathVariable Long id) {
        return ApiResponse.success(libraryService.getById(id));
    }

    @PostMapping
    public ApiResponse<Library> create(@RequestBody Library lib) {
        return ApiResponse.success(libraryService.create(lib));
    }

    @PutMapping("/{id}")
    public ApiResponse<Library> update(@PathVariable Long id, @RequestBody Library lib) {
        return ApiResponse.success(libraryService.update(id, lib));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        libraryService.delete(id);
        return ApiResponse.success(null);
    }
}