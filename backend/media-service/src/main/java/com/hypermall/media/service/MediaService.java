package com.hypermall.media.service;

import com.hypermall.common.exception.BadRequestException;
import com.hypermall.common.exception.ForbiddenException;
import com.hypermall.common.exception.ResourceNotFoundException;
import com.hypermall.media.dto.response.MediaResponse;
import com.hypermall.media.dto.response.UploadResponse;
import com.hypermall.media.entity.Media;
import com.hypermall.media.entity.MediaType;
import com.hypermall.media.entity.StorageProvider;
import com.hypermall.media.mapper.MediaMapper;
import com.hypermall.media.repository.MediaRepository;
import com.hypermall.media.storage.LocalStorageService;
import com.hypermall.media.storage.StorageResult;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;
    private final LocalStorageService storageService;

    @Value("${media.upload.max-size:10485760}")
    private long maxFileSize;

    @Value("${media.upload.thumbnail-width:300}")
    private int thumbnailWidth;

    @Value("${media.upload.thumbnail-height:300}")
    private int thumbnailHeight;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm", "video/quicktime"
    );

    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Transactional
    public UploadResponse uploadFiles(Long userId, MultipartFile[] files, String referenceType, Long referenceId) {
        List<MediaResponse> uploaded = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        String directory = generateDirectory(userId);

        for (MultipartFile file : files) {
            try {
                validateFile(file);
                MediaResponse response = uploadSingleFile(userId, file, directory, referenceType, referenceId);
                uploaded.add(response);
            } catch (Exception e) {
                log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        return UploadResponse.builder()
                .files(uploaded)
                .totalUploaded(uploaded.size())
                .totalFailed(errors.size())
                .errors(errors)
                .build();
    }

    @Transactional
    public MediaResponse uploadSingleFile(Long userId, MultipartFile file, String directory,
                                          String referenceType, Long referenceId) {
        validateFile(file);

        MediaType mediaType = detectMediaType(file.getContentType());
        StorageResult result = storageService.store(file, directory);

        int width = 0;
        int height = 0;
        String thumbnailUrl = null;

        if (mediaType == MediaType.IMAGE) {
            try {
                BufferedImage image = ImageIO.read(file.getInputStream());
                if (image != null) {
                    width = image.getWidth();
                    height = image.getHeight();

                    StorageResult thumbResult = storageService.storeThumbnail(
                            file, directory, thumbnailWidth, thumbnailHeight);
                    thumbnailUrl = thumbResult.getUrl();
                }
            } catch (IOException e) {
                log.warn("Could not read image dimensions: {}", e.getMessage());
            }
        }

        Media media = Media.builder()
                .userId(userId)
                .originalName(file.getOriginalFilename())
                .storedName(result.getStoredName())
                .mediaType(mediaType)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .url(result.getUrl())
                .thumbnailUrl(thumbnailUrl)
                .width(width)
                .height(height)
                .storageProvider(StorageProvider.LOCAL)
                .storagePath(result.getPath())
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();

        Media saved = mediaRepository.save(media);
        log.info("Uploaded media: id={}, name={}, size={}", saved.getId(), saved.getOriginalName(), saved.getFileSize());

        return mediaMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public MediaResponse getMediaById(Long id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + id));
        return mediaMapper.toResponse(media);
    }

    @Transactional(readOnly = true)
    public Page<MediaResponse> getUserMedia(Long userId, MediaType mediaType, Pageable pageable) {
        Page<Media> mediaPage;
        if (mediaType != null) {
            mediaPage = mediaRepository.findByUserIdAndMediaTypeOrderByCreatedAtDesc(userId, mediaType, pageable);
        } else {
            mediaPage = mediaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return mediaPage.map(mediaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<MediaResponse> getMediaByReference(String referenceType, Long referenceId) {
        List<Media> mediaList = mediaRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId);
        return mediaMapper.toResponseList(mediaList);
    }

    @Transactional
    public void deleteMedia(Long userId, Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + mediaId));

        if (!media.getUserId().equals(userId)) {
            throw new ForbiddenException("Not authorized to delete this media");
        }

        storageService.delete(media.getStoragePath());

        if (media.getThumbnailUrl() != null) {
            String thumbPath = media.getStoragePath().replace(media.getStoredName(),
                    "thumbnails/" + media.getStoredName().replace(".", "_thumb."));
            storageService.delete(thumbPath);
        }

        mediaRepository.delete(media);
        log.info("Deleted media: id={}", mediaId);
    }

    @Transactional(readOnly = true)
    public byte[] loadFile(String path) {
        return storageService.load(path);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed: " + (maxFileSize / 1024 / 1024) + "MB");
        }

        String contentType = file.getContentType();
        if (!isAllowedType(contentType)) {
            throw new BadRequestException("File type not allowed: " + contentType);
        }
    }

    private boolean isAllowedType(String contentType) {
        return ALLOWED_IMAGE_TYPES.contains(contentType)
                || ALLOWED_VIDEO_TYPES.contains(contentType)
                || ALLOWED_DOCUMENT_TYPES.contains(contentType);
    }

    private MediaType detectMediaType(String contentType) {
        if (ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return MediaType.IMAGE;
        } else if (ALLOWED_VIDEO_TYPES.contains(contentType)) {
            return MediaType.VIDEO;
        } else {
            return MediaType.DOCUMENT;
        }
    }

    private String generateDirectory(Long userId) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return "user_" + userId + "/" + date;
    }
}
