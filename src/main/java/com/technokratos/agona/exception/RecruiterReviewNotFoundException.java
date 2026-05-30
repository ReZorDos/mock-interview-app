package com.technokratos.agona.exception;

import java.util.UUID;

public class RecruiterReviewNotFoundException extends NotFoundException {
    public RecruiterReviewNotFoundException(UUID progressId) {
        super(String.format("Recruiter review with progresId = %s not found", progressId));
    }
}
