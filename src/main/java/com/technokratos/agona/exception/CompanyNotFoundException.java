package com.technokratos.agona.exception;

import java.util.UUID;

public class CompanyNotFoundException extends NotFoundException {
    public CompanyNotFoundException(UUID companyId) {
        super(String.format("Company with id = %s, not found", companyId));
    }
}
