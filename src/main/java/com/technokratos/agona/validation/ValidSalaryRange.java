package com.technokratos.agona.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SalaryRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSalaryRange {

    String message() default "Зарплата «от» не может быть больше зарплаты «до»";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
