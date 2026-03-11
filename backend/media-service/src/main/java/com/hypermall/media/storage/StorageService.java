package com.hypermall.media.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StorageResult store(MultipartFile file, String directory);

    StorageResult storeThumbnail(MultipartFile file, String directory, int width, int height);

    void delete(String path);

    byte[] load(String path);

    String getPublicUrl(String path);
}
