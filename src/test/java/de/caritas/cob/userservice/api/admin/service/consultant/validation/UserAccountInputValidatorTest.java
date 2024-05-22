package de.caritas.cob.userservice.api.admin.service.consultant.validation;

import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.EMAIL_NOT_VALID;
import static de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason.MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import javax.validation.Path;
import javax.validation.Validator;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserAccountInputValidatorTest {

  @InjectMocks private UserAccountInputValidator userAccountInputValidator;

  @Mock private Validator validator;

  @Test
  public void validateCreateConsultantDTO_ShouldNot_throwException_When_consultantIsNotAbsent() {
    AbsenceInputValidation createConsultantDTO =
        new CreateConsultantDTOAbsenceInputAdapter(new CreateConsultantDTO().absent(false));

    try {
      this.userAccountInputValidator.validateAbsence(createConsultantDTO);
    } catch (CustomValidationHttpStatusException e) {
      fail("Exception should not be thrown");
    }
  }

  @Test
  public void
      validateCreateConsultantDTO_ShouldNot_throwException_When_consultantIsAbsentAndAbsenceMessageIsSet() {
    AbsenceInputValidation createConsultantDTO =
        new CreateConsultantDTOAbsenceInputAdapter(
            new CreateConsultantDTO().absent(true).absenceMessage("Absent"));

    try {
      this.userAccountInputValidator.validateAbsence(createConsultantDTO);
    } catch (CustomValidationHttpStatusException e) {
      fail("Exception should not be thrown");
    }
  }

  @Test
  public void
      validateCreateConsultantDTO_Should_throwExpectedException_When_consultantIsAbsentAndAbsenceMessageIsEmpty() {
    AbsenceInputValidation createConsultantDTO =
        new CreateConsultantDTOAbsenceInputAdapter(
            new CreateConsultantDTO().absent(true).absenceMessage(null));

    try {
      this.userAccountInputValidator.validateAbsence(createConsultantDTO);
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeaders(), notNullValue());
      assertThat(
          e.getCustomHttpHeaders().get("X-Reason").get(0),
          is(MISSING_ABSENCE_MESSAGE_FOR_ABSENT_USER.name()));
    }
  }

  @Test
  public void validateKeycloakResponse_ShouldNot_throwException_When_keycloakResponseDTOIsValid() {
    KeycloakCreateUserResponseDTO responseDTO = new KeycloakCreateUserResponseDTO();
    responseDTO.setUserId("userId");

    try {
      this.userAccountInputValidator.validateKeycloakResponse(responseDTO);
    } catch (Exception e) {
      fail("Exception should not be thrown");
    }
  }

  @Test
  public void validateKeycloakResponse_Should_throwKeycloakException_When_userIdIsNull() {
    assertThrows(
        KeycloakException.class,
        () -> {
          KeycloakCreateUserResponseDTO responseDTO = new KeycloakCreateUserResponseDTO();

          this.userAccountInputValidator.validateKeycloakResponse(responseDTO);
        });
  }

  @Test
  public void validateEmailAddressShould_throwExpectedException_When_EmailIsInvalid() {
    ConstraintViolationImpl constraintViolation = mock(ConstraintViolationImpl.class);
    Path email = mock(Path.class);
    when(email.toString()).thenReturn("email");
    when(constraintViolation.getPropertyPath()).thenReturn(email);
    when(validator.validate(any())).thenReturn(asSet(constraintViolation));

    try {
      this.userAccountInputValidator.validateEmailAddress("invalid");
      fail("Exception should be thrown");
    } catch (CustomValidationHttpStatusException e) {
      assertThat(e.getCustomHttpHeaders(), notNullValue());
      assertThat(e.getCustomHttpHeaders().get("X-Reason").get(0), is(EMAIL_NOT_VALID.name()));
    }
  }
}
