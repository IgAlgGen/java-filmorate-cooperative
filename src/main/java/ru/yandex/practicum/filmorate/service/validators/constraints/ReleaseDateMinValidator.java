package ru.yandex.practicum.filmorate.service.validators.constraints;



import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateMinValidator implements ConstraintValidator<ReleaseDateMin, LocalDate> {
    private LocalDate min;

    @Override
    public void initialize(ReleaseDateMin annotation) {
        this.min = LocalDate.parse(annotation.value());
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value != null && !value.isBefore(min);
    }
}
