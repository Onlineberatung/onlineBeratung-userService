package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static de.caritas.cob.userservice.api.authorization.UserRole.CONSULTANT;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.keycloak.KeycloakException;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.keycloak.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.model.registration.UserDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.service.ConsultantImportService.ImportRecord;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.helper.KeycloakAdminClientHelper;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Creator class to generate new {@link Consultant} instances in database, keycloak and rocket chat.
 */
@Service
@RequiredArgsConstructor
public class ConsultantCreatorService {

  private final @NonNull KeycloakAdminClientHelper keycloakAdminClientHelper;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull UserHelper userHelper;

  /**
   * Creates a new {@link Consultant} by {@link CreateConsultantDTO} in database, keycloak and
   * rocket chat.
   *
   * @param createConsultantDTO the input used for creation
   * @return the generated {@link Consultant}
   */
  public Consultant createNewConsultant(CreateConsultantDTO createConsultantDTO) {
    ConsultantCreationInput consultantCreationInput =
        new CreateConsultantDTOCreationInputAdapter(createConsultantDTO, this.userHelper);
    return createNewConsultant(consultantCreationInput, asSet(CONSULTANT.getValue()));
  }

  /**
   * Creates a new {@link Consultant} by {@link ImportRecord} in database, keycloak and
   * rocket chat.
   *
   * @param importRecord the input record from csv used by the importer service
   * @param roles the roles to add to given {@link Consultant}
   * @return the generated {@link Consultant}
   */
  public Consultant createNewConsultant(ImportRecord importRecord, Set<String> roles) {
    ConsultantCreationInput consultantCreationInput =
        new ImportRecordCreationInputAdapter(importRecord);
    return createNewConsultant(consultantCreationInput, roles);
  }

  private Consultant createNewConsultant(ConsultantCreationInput consultantCreationInput,
      Set<String> roles) {
    String keycloakUserId = createKeycloakUser(consultantCreationInput);

    String password = userHelper.getRandomPassword();
    keycloakAdminClientHelper.updatePassword(keycloakUserId, password);
    roles.forEach(roleName -> this.keycloakAdminClientHelper.updateRole(keycloakUserId, roleName));

    String rocketChatUserId =
        createRocketChatUser(consultantCreationInput, keycloakUserId, password);

    return consultantService.saveConsultant(
        buildConsultant(consultantCreationInput, keycloakUserId, rocketChatUserId));
  }

  private String createRocketChatUser(ConsultantCreationInput consultantCreationInput,
      String keycloakUserId, String password) {
    try {
      return rocketChatService
          .getUserID(consultantCreationInput.getEncodedUsername(), password, true);
    } catch (RocketChatLoginException e) {
      throw new InternalServerErrorException(
          String.format("Unable to login user with id %s first time", keycloakUserId));
    }
  }

  private String createKeycloakUser(ConsultantCreationInput consultantCreationInput) {
    UserDTO userDto = buildUserDTO(consultantCreationInput.getUserName(),
        consultantCreationInput.getEmail());
    KeycloakCreateUserResponseDTO response = keycloakAdminClientHelper.createKeycloakUser(userDto,
        consultantCreationInput.getFirstName(), consultantCreationInput.getLastName());

    if (isNull(response.getUserId())) {
      throw new KeycloakException("ERROR: Keycloak user id is missing");
    }

    return response.getUserId();
  }

  private Consultant buildConsultant(ConsultantCreationInput consultantCreationInput,
      String keycloakUserId, String rocketChatUserId) {
    return Consultant.builder()
        .id(keycloakUserId)
        .idOld(consultantCreationInput.getIdOld())
        .username(consultantCreationInput.getEncodedUsername())
        .firstName(consultantCreationInput.getFirstName())
        .lastName(consultantCreationInput.getLastName())
        .email(consultantCreationInput.getEmail())
        .absent(isTrue(consultantCreationInput.isAbsent()))
        .absenceMessage(consultantCreationInput.getAbsenceMessage())
        .teamConsultant(consultantCreationInput.isTeamConsultant())
        .rocketChatId(rocketChatUserId)
        .languageFormal(consultantCreationInput.isLanguageFormal())
        .createDate(consultantCreationInput.getCreateDate())
        .updateDate(consultantCreationInput.getUpateDate())
        .build();
  }

  private UserDTO buildUserDTO(String username, String email) {
    UserDTO userDto = new UserDTO();
    userDto.setUsername(userHelper.encodeUsername(username));
    userDto.setEmail(email);
    return userDto;
  }

}
