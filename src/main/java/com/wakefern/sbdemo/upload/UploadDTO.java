package com.wakefern.sbdemo.upload;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UploadDTO {

    private Long id;

    @Size(max = 255)
    private String name;

    @Size(max = 255)
    private String status;

}
