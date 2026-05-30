package com.technokratos.agona.validation;

import com.technokratos.agona.dto.VacancyRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SalaryRangeValidator implements ConstraintValidator<ValidSalaryRange, VacancyRequest> {

    @Override
    public boolean isValid(VacancyRequest request, ConstraintValidatorContext context) {
        if (request == null) return true;

        Integer from = request.getSalaryFrom();
        Integer to   = request.getSalaryTo();

        if (from == null || to == null) return true;

        if (from > to) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("salaryTo")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
