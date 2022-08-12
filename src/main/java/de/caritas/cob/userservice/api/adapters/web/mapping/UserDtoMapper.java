package de.caritas.cob.userservice.api.adapters.web.mapping;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.EmailToggle;
import de.caritas.cob.userservice.api.adapters.web.dto.EmailType;
import de.caritas.cob.userservice.api.adapters.web.dto.OtpType;
import de.caritas.cob.userservice.api.adapters.web.dto.PatchUserDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.TwoFactorAuthDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDataResponseDTO;
import de.caritas.cob.userservice.api.config.auth.UserRole;
import de.caritas.cob.userservice.api.helper.AuthenticatedUser;
import de.caritas.cob.userservice.api.model.OtpInfoDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserDtoMapper {

  @Value("${feature.appointment.enabled}")
  private boolean appointmentFeatureEnabled;

  public UserDataResponseDTO userDataOf(UserDataResponseDTO userData, OtpInfoDTO otpInfoDTO,
      boolean isE2eEncEnabled, boolean isDisplayNameAllowed) {
    var twoFactorAuthDTO = new TwoFactorAuthDTO();

    if (nonNull(otpInfoDTO)) {
      twoFactorAuthDTO.setIsEnabled(true);
      if (otpInfoDTO.getOtpSetup()) {
        twoFactorAuthDTO.isActive(true);
        var foreignType = otpInfoDTO.getOtpType();
        if (nonNull(foreignType)) {
          var type = foreignType.getValue().equals("APP") ? OtpType.APP : OtpType.EMAIL;
          twoFactorAuthDTO.setType(type);
        }
      } else {
        twoFactorAuthDTO.setQrCode(otpInfoDTO.getOtpSecretQrCode());
        twoFactorAuthDTO.setSecret(otpInfoDTO.getOtpSecret());
      }
    }

    twoFactorAuthDTO.setIsToEncourage(userData.getEncourage2fa());
    userData.setTwoFactorAuth(twoFactorAuthDTO);
    userData.setE2eEncryptionEnabled(isE2eEncEnabled);
    userData.setIsDisplayNameEditable(
        isDisplayNameAllowed && userData.getUserRoles().contains(UserRole.CONSULTANT.getValue())
    );

    userData.setAppointmentFeatureEnabled(appointmentFeatureEnabled);

    return userData;
  }

  public String displayNameOf(Map<String, Object> consultantMap) {
    if (consultantMap.containsKey("displayName")) {
      return (String) consultantMap.get("displayName");
    }

    return null;
  }

  public String chatUserIdOf(Map<String, Object> userMap) {
    if (userMap.containsKey("chatUserId")) {
      return (String) userMap.get("chatUserId");
    }

    return null;
  }

  public Optional<Map<String, Object>> mapOf(PatchUserDTO patchUserDTO, AuthenticatedUser user) {
    if (isNull(patchUserDTO.getEncourage2fa()) && isNull(patchUserDTO.getDisplayName()) && isNull(
        patchUserDTO.getWalkThroughEnabled()) && isNull(patchUserDTO.getEmailToggles()) && isNull(
        patchUserDTO.getPreferredLanguage())) {
      return Optional.empty();
    }

    var map = new HashMap<String, Object>();
    map.put("id", user.getUserId());
    if (nonNull(patchUserDTO.getEncourage2fa())) {
      map.put("encourage2fa", patchUserDTO.getEncourage2fa());
    }
    if (nonNull(patchUserDTO.getDisplayName())) {
      map.put("displayName", patchUserDTO.getDisplayName());
    }
    if (nonNull(patchUserDTO.getWalkThroughEnabled())) {
      map.put("walkThroughEnabled", patchUserDTO.getWalkThroughEnabled());
    }
    if (nonNull(patchUserDTO.getPreferredLanguage())) {
      map.put("preferredLanguage", patchUserDTO.getPreferredLanguage().toString());
    }
    var emailToggles = patchUserDTO.getEmailToggles();
    if (nonNull(emailToggles)) {
      var emailToggleMap = emailToggles.stream().collect(Collectors.toMap(
          this::mapEmailType, EmailToggle::getState
      ));
      map.putAll(emailToggleMap);
    }

    return Optional.of(map);
  }

  public Map<String, Object> mapOf(String email, AuthenticatedUser user) {
    return Map.of(
        "id", user.getUserId(),
        "email", email
    );
  }

  private String mapEmailType(EmailToggle emailToggle) {
    var name = emailToggle.getName();
    if (name.equals(EmailType.DAILY_ENQUIRY)) {
      return "notifyEnquiriesRepeating";
    }
    if (name.equals(EmailType.NEW_CHAT_MESSAGE_FROM_ADVICE_SEEKER)) {
      return "notifyNewChatMessageFromAdviceSeeker";
    }
    if (name.equals(EmailType.NEW_FEEDBACK_MESSAGE_FROM_ADVICE_SEEKER)) {
      return "notifyNewFeedbackMessageFromAdviceSeeker";
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public List<String> bannedChatUserIdsOf(Map<String, Object> chatMetaInfoMap) {
    return (List<String>) chatMetaInfoMap.get("mutedUsers");
  }
}
