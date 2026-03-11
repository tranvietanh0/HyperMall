package com.hypermall.media.controller;

import com.hypermall.common.dto.ApiResponse;
import com.hypermall.common.dto.PageResponse;
import com.hypermall.common.security.CurrentUser;
import com.hypermall.common.security.UserPrincipal;
import com.hypermall.media.dto.response.MediaResponse;
import com.hypermall.media.dto.response.UploadResponse;
import com.hypermall.media.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "Media", description = "Media upload and management APIs")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    @Operation(summary = "Upload files")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadFiles(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "referenceType", required = false) String referenceType,
            @RequestParam(value = "referenceId", required = false) Long referenceId) {

        UploadResponse response = mediaService.uploadFiles(
                currentUser.getId(), files, referenceType, referenceId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Files uploaded", response));
    }

    @PostMapping("/upload/single")
    @Operation(summary = "Upload single file")
    public ResponseEntity<ApiResponse<MediaResponse>> uploadSingleFile(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "referenceType", required = false) String referenceType,
            @RequestParam(value = "referenceId", required = false) Long referenceId) {

        String directory = "user_" + currentUser.getId() + "/" +
                java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        MediaResponse response = mediaService.uploadSingleFile(
                currentUser.getId(), file, directory, referenceType, referenceId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get media by ID")
    public ResponseEntity<ApiResponse<MediaResponse>> getMediaById(@PathVariable Long id) {
        MediaResponse response = mediaService.getMediaById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's media")
    public ResponseEntity<ApiResponse<PageResponse<MediaResponse>>> getMyMedia(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) com.hypermall.media.entity.MediaType mediaType,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<MediaResponse> response = mediaService.getUserMedia(currentUser.getId(), mediaType, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(response)));
    }

    @GetMapping("/reference/{referenceType}/{referenceId}")
    @Operation(summary = "Get media by reference")
    public ResponseEntity<ApiResponse<List<MediaResponse>>> getMediaByReference(
            @PathVariable String referenceType,
            @PathVariable Long referenceId) {

        List<MediaResponse> response = mediaService.getMediaByReference(referenceType, referenceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete media")
    public ResponseEntity<ApiResponse<Void>> deleteMedia(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long id) {

        mediaService.deleteMedia(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Media deleted", null));
    }

    @GetMapping("/files/**")
    @Operation(summary = "Serve file (public)")
    public ResponseEntity<Resource> serveFile(
            jakarta.servlet.http.HttpServletRequest request) {

        String path = request.getRequestURI().substring("/api/media/files/".length());
        byte[] data = mediaService.loadFile(path);

        ByteArrayResource resource = new ByteArrayResource(data);

        String contentType = determineContentType(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .contentLength(data.length)
                .body(resource);
    }

    private String determineContentType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        } else if (lower.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lower.endsWith(".webm")) {
            return "video/webm";
        } else if (lower.endsWith(".pdf")) {
            return "application/pdf";
        }
        return "application/octet-stream";
    }
}
