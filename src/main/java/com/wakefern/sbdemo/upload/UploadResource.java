package com.wakefern.sbdemo.upload;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value = "/api/uploads", produces = MediaType.APPLICATION_JSON_VALUE)
public class UploadResource {

    private final UploadService uploadService;

    public UploadResource(final UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @GetMapping
    public ResponseEntity<List<UploadDTO>> getAllUploads() {
        return ResponseEntity.ok(uploadService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UploadDTO> getUpload(@PathVariable(name = "id") final Long id) {
        return ResponseEntity.ok(uploadService.get(id));
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<Long> createUpload(@RequestBody @Valid final UploadDTO uploadDTO) {
        final Long createdId = uploadService.create(uploadDTO);
        return new ResponseEntity<>(createdId, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateUpload(@PathVariable(name = "id") final Long id,
            @RequestBody @Valid final UploadDTO uploadDTO) {
        uploadService.update(id, uploadDTO);
        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    @ApiResponse(responseCode = "204")
    public ResponseEntity<Void> deleteUpload(@PathVariable(name = "id") final Long id) {
        uploadService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
