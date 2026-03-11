package com.hypermall.media.storage;

import com.hypermall.common.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class LocalStorageService implements StorageService {

    @Value("${media.storage.local.path:./uploads}")
    private String uploadPath;

    @Value("${media.storage.local.base-url:http://localhost:8093/api/media/files}")
    private String baseUrl;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        rootLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
            log.info("Created upload directory: {}", rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public StorageResult store(MultipartFile file, String directory) {
        try {
            if (file.isEmpty()) {
                throw new BadRequestException("Cannot store empty file");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String storedName = UUID.randomUUID() + extension;

            Path targetDir = rootLocation.resolve(directory);
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(storedName);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            String relativePath = directory + "/" + storedName;

            return StorageResult.builder()
                    .storedName(storedName)
                    .path(relativePath)
                    .url(baseUrl + "/" + relativePath)
                    .size(file.getSize())
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public StorageResult storeThumbnail(MultipartFile file, String directory, int width, int height) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);
            String storedName = UUID.randomUUID() + "_thumb" + extension;

            Path targetDir = rootLocation.resolve(directory).resolve("thumbnails");
            Files.createDirectories(targetDir);

            Path targetPath = targetDir.resolve(storedName);

            Thumbnails.of(file.getInputStream())
                    .size(width, height)
                    .keepAspectRatio(true)
                    .toFile(targetPath.toFile());

            String relativePath = directory + "/thumbnails/" + storedName;
            long size = Files.size(targetPath);

            return StorageResult.builder()
                    .storedName(storedName)
                    .path(relativePath)
                    .url(baseUrl + "/" + relativePath)
                    .size(size)
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Failed to create thumbnail", e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path filePath = rootLocation.resolve(path);
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", path);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", path, e);
        }
    }

    @Override
    public byte[] load(String path) {
        try {
            Path filePath = rootLocation.resolve(path);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + path, e);
        }
    }

    @Override
    public String getPublicUrl(String path) {
        return baseUrl + "/" + path;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
