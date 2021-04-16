package de.caritas.cob.userservice.api.admin.service.consultant.update;

import static de.caritas.cob.userservice.localdatetime.CustomLocalDateTime.nowInUtc;

import de.caritas.cob.userservice.api.admin.service.consultant.validation.UpdateConsultantDTOAbsenceInputAdapter;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserUpdateDataDTO;
import de.caritas.cob.userservice.api.model.rocketchat.user.UserUpdateRequestDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientService;
import de.caritas.cob.userservice.api.service.rocketchat.RocketChatService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service class to provide update functionality for consultants.
 */
@Service
@RequiredArgsConstructor
public class ConsultantUpdateService {

  private final @NonNull KeycloakAdminClientService keycloakAdminClientService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull UserAccountInputValidator userAccountInputValidator;
  private final @NonNull RocketChatService rocketChatService;

  /**
   * Updates the basic data of consultant with given id.
   *
   * @param consultantId        the id of the consultant to update
   * @param updateConsultantDTO the update input data
   * @return the updated persisted {@link Consultant}
   */
  public Consultant updateConsultant(String consultantId, UpdateAdminConsultantDTO updateConsultantDTO) {
    this.userAccountInputValidator
        .validateAbsence(new UpdateConsultantDTOAbsenceInputAdapter(updateConsultantDTO));

    Consultant consultant =
        this.consultantService.getConsultant(consultantId)
            .orElseThrow(() -> new BadRequestException(
                String.format("Consultant with id %s does not exist", consultantId)));

    UserDTO userDTO = buildValidatedUserDTO(updateConsultantDTO, consultant);
    this.keycloakAdminClientService.updateUserData(consultant.getId(), userDTO,
        updateConsultantDTO.getFirstname(), updateConsultantDTO.getLastname());

    this.rocketChatService
        .updateUser(buildUserUpdateRequestDTO(consultant.getRocketChatId(), updateConsultantDTO));

    return updateDatabaseConsultant(updateConsultantDTO, consultant);
  }

  private UserDTO buildValidatedUserDTO(UpdateAdminConsultantDTO updateConsultantDTO,
      Consultant consultant) {
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail(updateConsultantDTO.getEmail());
    userDTO.setUsername(consultant.getUsername());

    this.userAccountInputValidator.validateUserDTO(userDTO);
    return userDTO;
  }

  private UserUpdateRequestDTO buildUserUpdateRequestDTO(String rcUserId,
      UpdateAdminConsultantDTO updateConsultantDTO) {
    UserUpdateDataDTO userUpdateDataDTO = new UserUpdateDataDTO(updateConsultantDTO.getEmail(),
        updateConsultantDTO.getFirstname().concat(" ").concat(updateConsultantDTO.getLastname()));
    return new UserUpdateRequestDTO(rcUserId, userUpdateDataDTO);
  }

  private Consultant updateDatabaseConsultant(UpdateAdminConsultantDTO updateConsultantDTO,
      Consultant consultant) {
    consultant.setFirstName(updateConsultantDTO.getFirstname());
    consultant.setLastName(updateConsultantDTO.getLastname());
    consultant.setEmail(updateConsultantDTO.getEmail());
    consultant.setLanguageFormal(updateConsultantDTO.getFormalLanguage());
    consultant.setAbsent(updateConsultantDTO.getAbsent());
    consultant.setAbsenceMessage(updateConsultantDTO.getAbsenceMessage());
    consultant.setUpdateDate(nowInUtc());

    return this.consultantService.saveConsultant(consultant);
  }
}
