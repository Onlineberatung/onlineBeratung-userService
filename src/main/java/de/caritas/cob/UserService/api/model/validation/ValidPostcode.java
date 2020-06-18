package de.caritas.cob.UserService.api.model.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.UserService.api.model.UserDTO;

/**
 * Custom validation annotation for the postcode property of the registration {@link UserDTO}. The
 * postcode should have the minimum size that is defined in the {@link ConsultingTypeSettings}
 *
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPostcodeValidator.class)
public @interface ValidPostcode {

  String message() default "{user.custom.postcode.invalid}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
