package de.caritas.cob.userservice.api.manager.consultingtype.registration.mandatoryfields;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.RegistrationMandatoryFieldsDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MandatoryFields {

  private boolean age;
  private boolean state;

  public static MandatoryFields convertMandatoryFieldsDTOtoMandatoryFields(
      RegistrationMandatoryFieldsDTO registrationMandatoryFieldsDTO) {
    return new MandatoryFields(
        isTrue(registrationMandatoryFieldsDTO.getAge()),
        isTrue(registrationMandatoryFieldsDTO.getState()));
  }
}
