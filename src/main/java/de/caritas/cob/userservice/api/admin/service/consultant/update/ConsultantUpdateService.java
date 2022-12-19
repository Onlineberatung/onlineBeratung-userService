package de.caritas.cob.userservice.api.admin.service.consultant.update;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.util.Objects.isNull;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserUpdateDataDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.user.UserUpdateRequestDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UpdateConsultantDTOAbsenceInputAdapter;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Language;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Service class to provide update functionality for consultants. */
@Service
@RequiredArgsConstructor
public class ConsultantUpdateService {

  private final @NonNull IdentityClient identityClient;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull UserAccountInputValidator userAccountInputValidator;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull AppointmentService appointmentService;

  /**
   * Updates the basic data of consultant with given id.
   *
   * @param consultantId the id of the consultant to update
   * @param updateConsultantDTO the update input data
   * @return the updated persisted {@link Consultant}
   */
  public Consultant updateConsultant(
      String consultantId, UpdateAdminConsultantDTO updateConsultantDTO) {
    this.userAccountInputValidator.validateAbsence(
        new UpdateConsultantDTOAbsenceInputAdapter(updateConsultantDTO));

    Consultant consultant =
        this.consultantService
            .getConsultant(consultantId)
            .orElseThrow(
                () ->
                    new BadRequestException(
                        String.format("Consultant with id %s does not exist", consultantId)));

    UserDTO userDTO = buildValidatedUserDTO(updateConsultantDTO, consultant);
    this.identityClient.updateUserData(
        consultant.getId(),
        userDTO,
        updateConsultantDTO.getFirstname(),
        updateConsultantDTO.getLastname());

    this.rocketChatService.updateUser(
        buildUserUpdateRequestDTO(consultant.getRocketChatId(), updateConsultantDTO));

    var updatedConsultant = updateDatabaseConsultant(updateConsultantDTO, consultant);
    appointmentService.syncConsultantData(updatedConsultant);
    return updatedConsultant;
  }

  private UserDTO buildValidatedUserDTO(
      UpdateAdminConsultantDTO updateConsultantDTO, Consultant consultant) {
    UserDTO userDTO = new UserDTO();
    userDTO.setEmail(updateConsultantDTO.getEmail());
    userDTO.setUsername(consultant.getUsername());

    this.userAccountInputValidator.validateUserDTO(userDTO);
    return userDTO;
  }

  private UserUpdateRequestDTO buildUserUpdateRequestDTO(
      String rcUserId, UpdateAdminConsultantDTO updateConsultantDTO) {
    UserUpdateDataDTO userUpdateDataDTO = new UserUpdateDataDTO(updateConsultantDTO.getEmail(),
        false);
    return new UserUpdateRequestDTO(rcUserId, userUpdateDataDTO);
  }

  private Consultant updateDatabaseConsultant(
      UpdateAdminConsultantDTO updateConsultantDTO, Consultant consultant) {
    consultant.setFirstName(updateConsultantDTO.getFirstname());
    consultant.setLastName(updateConsultantDTO.getLastname());
    consultant.setEmail(updateConsultantDTO.getEmail());
    consultant.setLanguageFormal(updateConsultantDTO.getFormalLanguage());
    consultant.setLanguages(languagesOf(updateConsultantDTO, consultant));
    consultant.setAbsent(updateConsultantDTO.getAbsent());
    consultant.setAbsenceMessage(updateConsultantDTO.getAbsenceMessage());
    consultant.setUpdateDate(nowInUtc());

    return this.consultantService.saveConsultant(consultant);
  }

  private Set<Language> languagesOf(
      UpdateAdminConsultantDTO updateConsultantDTO, Consultant consultant) {
    var languages = updateConsultantDTO.getLanguages();

    return isNull(languages)
        ? Set.of()
        : languages.stream()
            .map(LanguageCode::getByCode)
            .map(languageCode -> new Language(consultant, languageCode))
            .collect(Collectors.toSet());
  }
}
