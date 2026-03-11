package com.hypermall.media.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageResult {

    private String storedName;
    private String path;
    private String url;
    private long size;
}
