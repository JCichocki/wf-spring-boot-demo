package com.wakefern.sbdemo.upload;

import com.wakefern.sbdemo.util.NotFoundException;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Service
public class UploadService {

    private final UploadRepository uploadRepository;

    public UploadService(final UploadRepository uploadRepository) {
        this.uploadRepository = uploadRepository;
    }

    public List<UploadDTO> findAll() {
        final List<Upload> uploads = uploadRepository.findAll(Sort.by("id"));
        return uploads.stream()
                .map(upload -> mapToDTO(upload, new UploadDTO()))
                .toList();
    }

    public UploadDTO get(final Long id) {
        return uploadRepository.findById(id)
                .map(upload -> mapToDTO(upload, new UploadDTO()))
                .orElseThrow(NotFoundException::new);
    }

    public Long create(final UploadDTO uploadDTO) {
        final Upload upload = new Upload();
        mapToEntity(uploadDTO, upload);
        return uploadRepository.save(upload).getId();
    }

    public void update(final Long id, final UploadDTO uploadDTO) {
        final Upload upload = uploadRepository.findById(id)
                .orElseThrow(NotFoundException::new);
        mapToEntity(uploadDTO, upload);
        uploadRepository.save(upload);
    }

    public void delete(final Long id) {
        uploadRepository.deleteById(id);
    }

    private UploadDTO mapToDTO(final Upload upload, final UploadDTO uploadDTO) {
        uploadDTO.setId(upload.getId());
        uploadDTO.setName(upload.getName());
        uploadDTO.setStatus(upload.getStatus());
        return uploadDTO;
    }

    private Upload mapToEntity(final UploadDTO uploadDTO, final Upload upload) {
        upload.setName(uploadDTO.getName());
        upload.setStatus(uploadDTO.getStatus());
        return upload;
    }

}
