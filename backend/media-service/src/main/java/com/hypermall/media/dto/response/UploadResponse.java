package com.hypermall.media.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {

    private List<MediaResponse> files;
    private int totalUploaded;
    private int totalFailed;
    private List<String> errors;
}
