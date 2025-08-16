package ru.yandex.practicum.filmorate.service.validators.constraints;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReleaseDateMinValidator.class)
public @interface ReleaseDateMin {
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String message() default "Дата релиза не может быть раньше {value}";
    String value(); // ISO-8601, например "1895-12-28"
}