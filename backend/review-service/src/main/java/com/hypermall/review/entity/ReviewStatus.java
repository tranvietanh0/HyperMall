package com.hypermall.review.entity;

public enum ReviewStatus {
    PENDING,    // Waiting for moderation
    APPROVED,   // Visible to public
    REJECTED,   // Hidden due to violation
    HIDDEN      // Hidden by user
}
