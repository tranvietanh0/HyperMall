package com.hypermall.media.dto.response;

import com.hypermall.media.entity.MediaType;
import com.hypermall.media.entity.StorageProvider;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {

    private Long id;
    private String originalName;
    private MediaType mediaType;
    private String mimeType;
    private Long fileSize;
    private String url;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private StorageProvider storageProvider;
    private String referenceType;
    private Long referenceId;
    private LocalDateTime createdAt;
}
