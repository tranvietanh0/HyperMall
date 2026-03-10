package com.hypermall.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerReplyRequest {

    @NotBlank(message = "Reply content is required")
    @Size(max = 1000, message = "Reply cannot exceed 1000 characters")
    private String reply;
}
