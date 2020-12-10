package de.caritas.cob.userservice.api.model.user;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.model.registration.NewRegistrationDto;
import de.caritas.cob.userservice.api.model.registration.UserDTO;

/**
 * Builder to create an {@link UserDTO}.
 */
public class UserDTOBuilder {

  private NewRegistrationDto newRegistrationDto;

  private UserDTOBuilder() {
  }

  /**
   * Creates an {@link UserDTOBuilder} instance.
   *
   * @return an instance of {@link UserDTOBuilder}
   */
  public static UserDTOBuilder getInstance() {
    return new UserDTOBuilder();
  }

  /**
   * Sets the {@link NewRegistrationDto} param.
   *
   * @param newRegistrationDto {@link NewRegistrationDto}
   * @return the current {@link UserDTOBuilder}
   */
  public UserDTOBuilder withNewRegistrationDto(NewRegistrationDto newRegistrationDto) {
    this.newRegistrationDto = newRegistrationDto;
    return this;
  }

  /**
   * Creates the {@link UserDTO}.
   *
   * @return the generated {@link UserDTO}
   */
  public UserDTO build() {
    if (nonNull(this.newRegistrationDto)) {
      UserDTO userDTO = new UserDTO();
      userDTO.setAgencyId(this.newRegistrationDto.getAgencyId());
      userDTO.setPostcode(this.newRegistrationDto.getPostcode());
      userDTO.setConsultingType(this.newRegistrationDto.getConsultingType());

      return userDTO;
    }

    return null;
  }
}
