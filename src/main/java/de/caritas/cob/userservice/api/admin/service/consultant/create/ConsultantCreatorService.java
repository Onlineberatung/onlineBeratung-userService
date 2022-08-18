package de.caritas.cob.userservice.api.admin.service.consultant.create;

import static de.caritas.cob.userservice.api.config.auth.UserRole.CONSULTANT;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.adapters.keycloak.dto.KeycloakCreateUserResponseDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.CreateConsultantDTOAbsenceInputAdapter;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantAdminService;
import de.caritas.cob.userservice.api.exception.httpresponses.CustomValidationHttpStatusException;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.exception.httpresponses.customheader.HttpStatusExceptionReason;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatLoginException;
import de.caritas.cob.userservice.api.helper.UserHelper;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantStatus;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.service.ConsultantImportService.ImportRecord;
import de.caritas.cob.userservice.api.service.ConsultantService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.tenantadminservice.generated.web.model.TenantDTO;
import java.util.Set;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Creator class to generate new {@link Consultant} instances in database, keycloak and rocket chat.
 */
@Service
@RequiredArgsConstructor
public class ConsultantCreatorService {

  private final @NonNull IdentityClient identityClient;
  private final @NonNull RocketChatService rocketChatService;
  private final @NonNull ConsultantService consultantService;
  private final @NonNull UserHelper userHelper;
  private final @NonNull UserAccountInputValidator userAccountInputValidator;
  private final @NonNull TenantAdminService tenantAdminService;

  @Value("${multitenancy.enabled}")
  private boolean multiTenancyEnabled;

  /**
   * Creates a new {@link Consultant} by {@link CreateConsultantDTO} in database, keycloak and
   * rocket chat.
   *
   * @param createConsultantDTO the input used for creation
   * @return the generated {@link Consultant}
   */
  public Consultant createNewConsultant(CreateConsultantDTO createConsultantDTO) {
    assertLicensesNotExceeded();
    this.userAccountInputValidator.validateAbsence(
        new CreateConsultantDTOAbsenceInputAdapter(createConsultantDTO));
    ConsultantCreationInput consultantCreationInput =
        new CreateConsultantDTOCreationInputAdapter(createConsultantDTO);
    assignCurrentTenantContext(createConsultantDTO);
    return createNewConsultant(consultantCreationInput, asSet(CONSULTANT.getValue()));
  }

  /**
   * Creates a new {@link Consultant} by {@link ImportRecord} in database, keycloak and rocket chat.
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

  private Consultant createNewConsultant(
      ConsultantCreationInput consultantCreationInput, Set<String> roles) {
    String keycloakUserId = createKeycloakUser(consultantCreationInput);

    String password = userHelper.getRandomPassword();
    identityClient.updatePassword(keycloakUserId, password);
    roles.forEach(roleName -> identityClient.updateRole(keycloakUserId, roleName));

    String rocketChatUserId =
        createRocketChatUser(consultantCreationInput, keycloakUserId, password);

    return consultantService.saveConsultant(
        buildConsultant(consultantCreationInput, keycloakUserId, rocketChatUserId));
  }

  private void assignCurrentTenantContext(CreateConsultantDTO createConsultantDTO) {
    if (multiTenancyEnabled) {
      createConsultantDTO.setTenantId(TenantContext.getCurrentTenant().intValue());
    }
  }

  private String createKeycloakUser(ConsultantCreationInput consultantCreationInput) {
    UserDTO userDto =
        buildUserDTO(
            consultantCreationInput.getUserName(),
            consultantCreationInput.getEmail(),
            consultantCreationInput.getTenantId());

    this.userAccountInputValidator.validateUserDTO(userDto);

    KeycloakCreateUserResponseDTO response =
        identityClient.createKeycloakUser(
            userDto, consultantCreationInput.getFirstName(), consultantCreationInput.getLastName());

    this.userAccountInputValidator.validateKeycloakResponse(response);

    return response.getUserId();
  }

  private String createRocketChatUser(
      ConsultantCreationInput consultantCreationInput, String keycloakUserId, String password) {
    try {
      return this.rocketChatService.getUserID(
          consultantCreationInput.getEncodedUsername(), password, true);
    } catch (RocketChatLoginException e) {
      throw new InternalServerErrorException(
          String.format("Unable to login user with id %s first time", keycloakUserId));
    }
  }

  private Consultant buildConsultant(
      ConsultantCreationInput consultantCreationInput,
      String keycloakUserId,
      String rocketChatUserId) {
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
        .encourage2fa(true)
        .notifyEnquiriesRepeating(true)
        .notifyNewChatMessageFromAdviceSeeker(true)
        .notifyNewFeedbackMessageFromAdviceSeeker(true)
        .languageFormal(consultantCreationInput.isLanguageFormal())
        .languages(Set.of())
        .createDate(consultantCreationInput.getCreateDate())
        .updateDate(consultantCreationInput.getUpdateDate())
        .tenantId(consultantCreationInput.getTenantId())
        .status(ConsultantStatus.IN_PROGRESS)
        .walkThroughEnabled(true)
        .languageCode(LanguageCode.de)
        .build();
  }

  private UserDTO buildUserDTO(String username, String email, Long tenantId) {
    UserDTO userDto = new UserDTO();
    userDto.setUsername(new UsernameTranscoder().encodeUsername(username));
    userDto.setEmail(email);
    userDto.setTenantId(tenantId);
    return userDto;
  }

  private void assertLicensesNotExceeded() {
    if (multiTenancyEnabled) {
      TenantDTO tenantById = tenantAdminService.getTenantById(TenantContext.getCurrentTenant());
      long numberOfActiveConsultants = consultantService.getNumberOfActiveConsultants();
      Integer allowedNumberOfUsers = tenantById.getLicensing().getAllowedNumberOfUsers();
      if (numberOfActiveConsultants >= allowedNumberOfUsers) {
        throw new CustomValidationHttpStatusException(
            HttpStatusExceptionReason.NUMBER_OF_LICENSES_EXCEEDED);
      }
    }
  }
}
