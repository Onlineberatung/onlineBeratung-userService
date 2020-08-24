package de.caritas.cob.userservice.api.facade;

import static de.caritas.cob.userservice.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ATTACHMENT_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_DURATION_30;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_DURATION_60;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_DURATION_90;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_ID_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_TOPIC;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_TOPIC_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.CHAT_TOPIC_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.CONSULTANT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.COUNT_1;
import static de.caritas.cob.userservice.testHelper.TestConstants.COUNT_10;
import static de.caritas.cob.userservice.testHelper.TestConstants.DECRYPTED_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.EMPTY_AGENCY_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.EMPTY_ROOMS_UPDATE_DTO_LIST;
import static de.caritas.cob.userservice.testHelper.TestConstants.ENCRYPTED_MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.FILE_DTO;
import static de.caritas.cob.userservice.testHelper.TestConstants.INVALID_CONSULTING_TYPE;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_ACTIVE;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_NOT_ACTIVE;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_NOT_MONITORING;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_NOT_REPETITIVE;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_NO_TEAM_SESSION;
import static de.caritas.cob.userservice.testHelper.TestConstants.IS_REPETITIVE;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.userservice.testHelper.TestConstants.MESSAGES_NOT_READ;
import static de.caritas.cob.userservice.testHelper.TestConstants.NOW;
import static de.caritas.cob.userservice.testHelper.TestConstants.NOW_MINUS_1_DAY;
import static de.caritas.cob.userservice.testHelper.TestConstants.NOW_MINUS_2_DAYS;
import static de.caritas.cob.userservice.testHelper.TestConstants.NOW_MINUS_3_DAYS;
import static de.caritas.cob.userservice.testHelper.TestConstants.OFFSET_0;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_CREDENTIALS;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_4;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_5;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_GROUP_ID_6;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_TOKEN;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.RC_USER_ID_3;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.ROCKETCHAT_ID_2;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_ID;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_IN_PROGRESS;
import static de.caritas.cob.userservice.testHelper.TestConstants.SESSION_STATUS_NEW;
import static de.caritas.cob.userservice.testHelper.TestConstants.USER_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import de.caritas.cob.userservice.api.exception.CustomCryptoException;
import de.caritas.cob.userservice.api.helper.Helper;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeManager;
import de.caritas.cob.userservice.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.userservice.api.model.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantSessionResponseDTO;
import de.caritas.cob.userservice.api.model.SessionConsultantForUserDTO;
import de.caritas.cob.userservice.api.model.SessionDTO;
import de.caritas.cob.userservice.api.model.UserSessionListResponseDTO;
import de.caritas.cob.userservice.api.model.UserSessionResponseDTO;
import de.caritas.cob.userservice.api.model.chat.UserChatDTO;
import de.caritas.cob.userservice.api.model.rocketChat.RocketChatUserDTO;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsLastMessageDTO;
import de.caritas.cob.userservice.api.model.rocketChat.room.RoomsUpdateDTO;
import de.caritas.cob.userservice.api.model.rocketChat.subscriptions.SubscriptionsUpdateDTO;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.session.SessionFilter;
import de.caritas.cob.userservice.api.service.ChatService;
import de.caritas.cob.userservice.api.service.DecryptionService;
import de.caritas.cob.userservice.api.service.LogService;
import de.caritas.cob.userservice.api.service.RocketChatService;
import de.caritas.cob.userservice.api.service.SessionService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.hibernate.service.spi.ServiceException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class GetSessionListFacadeTest {

  private final SessionDTO SESSION_DTO_1 =
      new SessionDTO(SESSION_ID, AGENCY_ID, 0, 0, null, RC_GROUP_ID, RC_FEEDBACK_GROUP_ID,
          RC_USER_ID, Helper.getUnixTimestampFromDate(NOW), IS_NO_TEAM_SESSION, IS_MONITORING);
  private final SessionDTO SESSION_DTO_2 =
      new SessionDTO(SESSION_ID, AGENCY_ID, 0, 0, null, RC_GROUP_ID_2, RC_FEEDBACK_GROUP_ID_2,
          RC_USER_ID_2, Helper.getUnixTimestampFromDate(new Date(NOW.getTime() + 86400000)),
          IS_NO_TEAM_SESSION, IS_MONITORING);
  private final SessionDTO SESSION_DTO_3 =
      new SessionDTO(SESSION_ID, AGENCY_ID, 0, 0, null, RC_GROUP_ID_3, RC_FEEDBACK_GROUP_ID_3,
          RC_USER_ID_3, Helper.getUnixTimestampFromDate(new Date(NOW.getTime() + 8640000)),
          IS_NO_TEAM_SESSION, IS_MONITORING);
  private final UserChatDTO USER_CHAT_DTO_1 = new UserChatDTO(CHAT_ID, CHAT_TOPIC, null, null,
      CHAT_DURATION_30, IS_REPETITIVE, IS_ACTIVE, ConsultingType.PREGNANCY.getValue(), null,
      Helper.getUnixTimestampFromDate(new Date(NOW.getTime() + 86300000)), MESSAGES_NOT_READ,
      RC_GROUP_ID_4, null, false, null, LocalDateTime.now());
  private final UserChatDTO USER_CHAT_DTO_2 = new UserChatDTO(CHAT_ID_2, CHAT_TOPIC_2, null, null,
      CHAT_DURATION_60, IS_REPETITIVE, IS_NOT_ACTIVE, ConsultingType.DEBT.getValue(), null,
      Helper.getUnixTimestampFromDate(new Date(NOW.getTime() + 86200000)), MESSAGES_NOT_READ,
      RC_GROUP_ID_5, null, false, null, LocalDateTime.now());
  private final UserChatDTO USER_CHAT_DTO_3 = new UserChatDTO(CHAT_ID_3, CHAT_TOPIC_3, null, null,
      CHAT_DURATION_90, IS_NOT_REPETITIVE, IS_NOT_ACTIVE, ConsultingType.CHILDREN.getValue(), null,
      Helper.getUnixTimestampFromDate(new Date(NOW.getTime() + 86410000)), MESSAGES_NOT_READ,
      RC_GROUP_ID_6, null, false, null, LocalDateTime.now());
  private final UserChatDTO USER_CHAT_DTO_WITH_ENCRYPTED_MESSAGE =
      new UserChatDTO(CHAT_ID_3, CHAT_TOPIC_3, null, null, CHAT_DURATION_90, IS_NOT_REPETITIVE,
          IS_NOT_ACTIVE, ConsultingType.CHILDREN.getValue(), ENCRYPTED_MESSAGE,
          Helper.getUnixTimestampFromDate(new Date(NOW.getTime() + 86410000)), MESSAGES_NOT_READ,
          RC_GROUP_ID_6, null, false, null, LocalDateTime.now());
  private final SessionDTO SESSION_DTO_WITH_ENCRYPTED_MESSAGE = new SessionDTO(SESSION_ID,
      AGENCY_ID, 0, 0, null, RC_GROUP_ID, RC_FEEDBACK_GROUP_ID, RC_USER_ID, ENCRYPTED_MESSAGE,
      Helper.getUnixTimestampFromDate(NOW), false, false, IS_NO_TEAM_SESSION, IS_MONITORING, null);
  private final SessionDTO SESSION_DTO_WITHOUT_FEEDBACK_CHAT =
      new SessionDTO(SESSION_ID, AGENCY_ID, 0, 0, null, RC_GROUP_ID, null, RC_USER_ID,
          Helper.getUnixTimestampFromDate(NOW), IS_NO_TEAM_SESSION, IS_MONITORING);
  private final SessionDTO SESSION_DTO_WITH_INVALID_CONSULTING_TYPE =
      new SessionDTO(SESSION_ID, AGENCY_ID, INVALID_CONSULTING_TYPE, 0, null, RC_GROUP_ID,
          RC_FEEDBACK_GROUP_ID, RC_USER_ID, null, null, true, true, false, IS_NOT_MONITORING, null);
  private final SessionConsultantForUserDTO CONSULTANT_DTO = new SessionConsultantForUserDTO();
  private final UserSessionResponseDTO USER_SESSION_RESPONSE_DTO =
      new UserSessionResponseDTO(SESSION_DTO_1, null, EMPTY_AGENCY_DTO, CONSULTANT_DTO, NOW);
  private final UserSessionResponseDTO USER_SESSION_RESPONSE_DTO_2 = new UserSessionResponseDTO(
      SESSION_DTO_2, null, EMPTY_AGENCY_DTO, CONSULTANT_DTO, new Date(NOW.getTime() + 86400000));
  private final UserSessionResponseDTO USER_SESSION_RESPONSE_DTO_3 = new UserSessionResponseDTO(
      SESSION_DTO_3, null, EMPTY_AGENCY_DTO, CONSULTANT_DTO, new Date(NOW.getTime() + 8640000));
  private final UserSessionResponseDTO USER_CHAT_RESPONSE_DTO =
      new UserSessionResponseDTO(null, USER_CHAT_DTO_1, EMPTY_AGENCY_DTO, CONSULTANT_DTO, NOW);
  private final UserSessionResponseDTO USER_CHAT_RESPONSE_DTO_2 = new UserSessionResponseDTO(null,
      USER_CHAT_DTO_2, EMPTY_AGENCY_DTO, CONSULTANT_DTO, new Date(NOW.getTime() + 80000000));
  private final UserSessionResponseDTO USER_CHAT_RESPONSE_DTO_3 = new UserSessionResponseDTO(null,
      USER_CHAT_DTO_3, EMPTY_AGENCY_DTO, CONSULTANT_DTO, new Date(NOW.getTime() + 70000000));
  private final List<UserSessionResponseDTO> USER_CHAT_RESPONSE_DTO_LIST =
      Arrays.asList(USER_CHAT_RESPONSE_DTO, USER_CHAT_RESPONSE_DTO_2, USER_CHAT_RESPONSE_DTO_3);
  private final List<UserSessionResponseDTO> USER_SESSION_REPONSE_DTO_LIST = Arrays
      .asList(USER_SESSION_RESPONSE_DTO, USER_SESSION_RESPONSE_DTO_2, USER_SESSION_RESPONSE_DTO_3);
  private final ConsultantSessionResponseDTO CONSULTANT_SESSION_RESPONSE_DTO =
      new ConsultantSessionResponseDTO(SESSION_DTO_1, null, null, null, NOW);
  private final ConsultantSessionResponseDTO CONSULTANT_SESSION_RESPONSE_DTO_2 =
      new ConsultantSessionResponseDTO(SESSION_DTO_2, null, null, null,
          new Date(NOW.getTime() + 86400000));
  private final ConsultantSessionResponseDTO CONSULTANT_SESSION_RESPONSE_DTO_3 =
      new ConsultantSessionResponseDTO(SESSION_DTO_3, null, null, null,
          new Date(NOW.getTime() + 8640000));
  private final ConsultantSessionResponseDTO CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_MESSAGE =
      new ConsultantSessionResponseDTO(SESSION_DTO_WITH_ENCRYPTED_MESSAGE, null, null, null,
          new Date(NOW.getTime() + 8640000));
  private final ConsultantSessionResponseDTO CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE =
      new ConsultantSessionResponseDTO(null, USER_CHAT_DTO_WITH_ENCRYPTED_MESSAGE, null, null,
          new Date(NOW.getTime() + 8640000));
  private final ConsultantSessionResponseDTO CONSULTANT_SESSION_RESPONSE_DTO_WITHOUT_FEEDBACK_CHAT =
      new ConsultantSessionResponseDTO(SESSION_DTO_WITHOUT_FEEDBACK_CHAT, null, null, null, NOW);
  private final ConsultantSessionResponseDTO CONSULTANT_SESSION_RESPONSE_DTO_WITH_INVALID_CONSULTING_TYPE =
      new ConsultantSessionResponseDTO(SESSION_DTO_WITH_INVALID_CONSULTING_TYPE, null, null);
  private final List<ConsultantSessionResponseDTO> CONSULTANT_SESSION_RESPONSE_DTO_LIST =
      new ArrayList<ConsultantSessionResponseDTO>() {
        private static final long serialVersionUID = 1L;

        {
          add(CONSULTANT_SESSION_RESPONSE_DTO);
          add(CONSULTANT_SESSION_RESPONSE_DTO_2);
          add(CONSULTANT_SESSION_RESPONSE_DTO_3);
        }
      };
  private final List<ConsultantSessionResponseDTO> CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_MESSAGE =
      new ArrayList<ConsultantSessionResponseDTO>() {
        private static final long serialVersionUID = 1L;

        {
          add(CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_MESSAGE);
        }
      };
  private final List<ConsultantSessionResponseDTO> CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE =
      new ArrayList<ConsultantSessionResponseDTO>() {
        private static final long serialVersionUID = 1L;

        {
          add(CONSULTANT_SESSION_RESPONSE_DTO_WITH_ENCRYPTED_CHAT_MESSAGE);
        }
      };
  private final List<ConsultantSessionResponseDTO> CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ONE_SESSION_WITH_FEEDBACK =
      new ArrayList<ConsultantSessionResponseDTO>() {
        private static final long serialVersionUID = 1L;

        {
          add(CONSULTANT_SESSION_RESPONSE_DTO);
        }
      };
  private final List<ConsultantSessionResponseDTO> CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITHOUT_FEEDBACK_CHAT =
      Arrays.asList(CONSULTANT_SESSION_RESPONSE_DTO_WITHOUT_FEEDBACK_CHAT);
  private final List<ConsultantSessionResponseDTO> CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_INVALID_CONSULTING_TYPE =
      Arrays.asList(CONSULTANT_SESSION_RESPONSE_DTO_WITH_INVALID_CONSULTING_TYPE);
  private final Consultant CONSULTANT = new Consultant(CONSULTANT_ID, ROCKETCHAT_ID, "consultant",
      "first name", "last name", "consultant@cob.de", false, true, "", false, null, null, null);
  private final Consultant CONSULTANT_2 =
      new Consultant(CONSULTANT_ID, ROCKETCHAT_ID_2, "consultant", "first name", "last name",
          "consultant@cob.de", false, true, "", false, null, null, null);
  private final List<SubscriptionsUpdateDTO> SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS =
      Arrays.asList(
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_GROUP_ID, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_GROUP_ID_2, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_GROUP_ID_3, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_FEEDBACK_GROUP_ID, "A", "A",
              "P", null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_FEEDBACK_GROUP_ID_2, "A",
              "A", "P", null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_FEEDBACK_GROUP_ID_3, "A",
              "A", "P", null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_GROUP_ID_4, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_GROUP_ID_5, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_GROUP_ID_6, "A", "A", "P",
              null, null, null, null));
  private final List<SubscriptionsUpdateDTO> SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS =
      Arrays.asList(
          new SubscriptionsUpdateDTO("A", true, false, 4, 0, 0, NOW, RC_GROUP_ID, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 4, 0, 0, NOW, RC_GROUP_ID_2, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 4, 0, 0, NOW, RC_GROUP_ID_3, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 4, 0, 0, NOW, RC_FEEDBACK_GROUP_ID, "A", "A",
              "P", null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 4, 0, 0, NOW, RC_FEEDBACK_GROUP_ID_2, "A",
              "A", "P", null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 4, 0, 0, NOW, RC_FEEDBACK_GROUP_ID_3, "A",
              "A", "P", null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 4, 0, 0, NOW, RC_GROUP_ID_4, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 4, 0, 0, NOW, RC_GROUP_ID_5, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 4, 0, 0, NOW, RC_GROUP_ID_6, "A", "A", "P",
              null, null, null, null));
  private final List<SubscriptionsUpdateDTO> SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_ONE_FEEDBACK_UNREAD =
      Arrays.asList(
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_GROUP_ID, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_GROUP_ID_2, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_GROUP_ID_3, "A", "A", "P",
              null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 1, 0, 0, NOW, RC_FEEDBACK_GROUP_ID, "A", "A",
              "P", null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_FEEDBACK_GROUP_ID_2, "A",
              "A", "P", null, null, null, null),
          new SubscriptionsUpdateDTO("A", true, false, 0, 0, 0, NOW, RC_FEEDBACK_GROUP_ID_3, "A",
              "A", "P", null, null, null, null));
  private final RocketChatUserDTO USER_DTO_1 = new RocketChatUserDTO("xyz", "123");
  private final RoomsLastMessageDTO ROOMS_LAST_MESSAGE_DTO_1 =
      new RoomsLastMessageDTO("id", RC_GROUP_ID, NOW_MINUS_1_DAY, USER_DTO_1, true, NOW_MINUS_1_DAY,
          MESSAGE, FILE_DTO, org.assertj.core.util.Arrays.array(ATTACHMENT_DTO));
  private final RoomsLastMessageDTO ROOMS_LAST_MESSAGE_DTO_WITH_ATTACHMENT =
      new RoomsLastMessageDTO("id", RC_GROUP_ID, NOW_MINUS_1_DAY, USER_DTO_1, true, NOW_MINUS_1_DAY,
          MESSAGE, FILE_DTO, org.assertj.core.util.Arrays.array(ATTACHMENT_DTO));
  private final RoomsLastMessageDTO ROOMS_LAST_MESSAGE_DTO_WITH_ATTACHMENT_FOR_CHAT =
      new RoomsLastMessageDTO("id", RC_GROUP_ID_6, NOW_MINUS_1_DAY, USER_DTO_1, true,
          NOW_MINUS_1_DAY, MESSAGE, FILE_DTO, org.assertj.core.util.Arrays.array(ATTACHMENT_DTO));
  private final RocketChatUserDTO USER_DTO_2 = new RocketChatUserDTO(ROCKETCHAT_ID_2, "456");
  private final RoomsLastMessageDTO ROOMS_LAST_MESSAGE_DTO_2 =
      new RoomsLastMessageDTO("id", RC_GROUP_ID_2, NOW_MINUS_3_DAYS, USER_DTO_2, true,
          NOW_MINUS_3_DAYS, MESSAGE, FILE_DTO, org.assertj.core.util.Arrays.array(ATTACHMENT_DTO));
  private final RoomsLastMessageDTO ROOMS_LAST_MESSAGE_DTO_4 =
      new RoomsLastMessageDTO("id", RC_GROUP_ID_4, NOW_MINUS_1_DAY, USER_DTO_1, true,
          NOW_MINUS_1_DAY, MESSAGE, FILE_DTO, org.assertj.core.util.Arrays.array(ATTACHMENT_DTO));
  private final RoomsLastMessageDTO ROOMS_LAST_MESSAGE_DTO_5 =
      new RoomsLastMessageDTO("id", RC_GROUP_ID_5, NOW_MINUS_1_DAY, USER_DTO_1, true,
          NOW_MINUS_1_DAY, MESSAGE, FILE_DTO, org.assertj.core.util.Arrays.array(ATTACHMENT_DTO));
  private final RoomsLastMessageDTO ROOMS_LAST_MESSAGE_DTO_6 =
      new RoomsLastMessageDTO("id", RC_GROUP_ID_6, NOW_MINUS_1_DAY, USER_DTO_1, true,
          NOW_MINUS_1_DAY, MESSAGE, FILE_DTO, org.assertj.core.util.Arrays.array(ATTACHMENT_DTO));
  private final RocketChatUserDTO USER_DTO_3 = new RocketChatUserDTO("adg", "789");
  private final RoomsLastMessageDTO ROOMS_LAST_MESSAGE_DTO_3 = new RoomsLastMessageDTO("id",
      RC_GROUP_ID, NOW_MINUS_2_DAYS, USER_DTO_3, true, NOW_MINUS_2_DAYS, MESSAGE, null, null);
  private final List<RoomsUpdateDTO> ROOMS_UPDATE_DTO_LIST = Arrays.asList(
      new RoomsUpdateDTO(RC_GROUP_ID, "name1", "fname1", "P", USER_DTO_1, true, false, new Date(),
          ROOMS_LAST_MESSAGE_DTO_1),
      new RoomsUpdateDTO(RC_GROUP_ID_2, "name2", "fname2", "P", USER_DTO_2, true, false, new Date(),
          ROOMS_LAST_MESSAGE_DTO_2),
      new RoomsUpdateDTO(RC_GROUP_ID_3, "name3", "fname3", "P", USER_DTO_3, true, false, new Date(),
          ROOMS_LAST_MESSAGE_DTO_3),
      new RoomsUpdateDTO(RC_FEEDBACK_GROUP_ID, "name1", "fname1", "P", USER_DTO_1, true, false,
          new Date(), ROOMS_LAST_MESSAGE_DTO_1),
      new RoomsUpdateDTO(RC_FEEDBACK_GROUP_ID_2, "name2", "fname2", "P", USER_DTO_2, true, false,
          new Date(), ROOMS_LAST_MESSAGE_DTO_2),
      new RoomsUpdateDTO(RC_FEEDBACK_GROUP_ID_3, "name3", "fname3", "P", USER_DTO_3, true, false,
          new Date(), ROOMS_LAST_MESSAGE_DTO_3),
      new RoomsUpdateDTO(RC_GROUP_ID_4, "name4", "fname4", "P", USER_DTO_1, true, false, new Date(),
          ROOMS_LAST_MESSAGE_DTO_4),
      new RoomsUpdateDTO(RC_GROUP_ID_5, "name5", "fname5", "P", USER_DTO_2, true, false, new Date(),
          ROOMS_LAST_MESSAGE_DTO_5),
      new RoomsUpdateDTO(RC_GROUP_ID_6, "name6", "fname6", "P", USER_DTO_3, true, false, new Date(),
          ROOMS_LAST_MESSAGE_DTO_6));
  private final List<RoomsUpdateDTO> ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT =
      Arrays.asList(new RoomsUpdateDTO(RC_GROUP_ID, "name1", "fname1", "P", USER_DTO_1, true, false,
          new Date(), ROOMS_LAST_MESSAGE_DTO_WITH_ATTACHMENT));
  private final List<RoomsUpdateDTO> ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT_FOR_CHAT = Arrays.asList(
      new RoomsUpdateDTO(RC_GROUP_ID_4, "name1", "fname1", "P", USER_DTO_1, true, false, new Date(),
          ROOMS_LAST_MESSAGE_DTO_WITH_ATTACHMENT_FOR_CHAT),
      new RoomsUpdateDTO(RC_GROUP_ID_6, "name1", "fname1", "P", USER_DTO_1, true, false, new Date(),
          ROOMS_LAST_MESSAGE_DTO_WITH_ATTACHMENT_FOR_CHAT));
  private final List<RoomsUpdateDTO> ROOMS_UPDATE_DTO_LIST_WITHOUT_FEEDBACK =
      Arrays.asList(new RoomsUpdateDTO(RC_GROUP_ID, "name1", "fname1", "P", USER_DTO_1, true, false,
          new Date(), ROOMS_LAST_MESSAGE_DTO_1));
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_MONITORING =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null, null, IS_MONITORING, null,
          false, null, false, null, null);
  private final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null, null, IS_NOT_MONITORING, null,
          false, null, false, null, null);

  @InjectMocks
  private GetSessionListFacade getSessionListFacade;
  @Mock
  private RocketChatService rocketChatService;
  @Mock
  private SessionService sessionService;
  @Mock
  private ChatService chatService;
  @Mock
  private DecryptionService decryptionService;
  @Mock
  private Logger logger;
  @Mock
  private ConsultingTypeManager consultingTypeManager;

  @Before
  public void setup() {
    setInternalState(LogService.class, "LOGGER", logger);
  }

  /**
   * Method: getSessionsForAuthenticatedUser
   * 
   */

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithSessionMessagesReadTrue_WhenThereAreNoUnreadMessages()
      throws Exception {

    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_REPONSE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);

    assertEquals(true, getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS)
        .getSessions().get(0).getSession().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithChatMessagesReadTrue_WhenThereAreNoUnreadMessages()
      throws Exception {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);

    assertEquals(true, getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS)
        .getSessions().get(0).getChat().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithSessionMessagesReadFalse_WhenThereAreUnreadMessages()
      throws Exception {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(null);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_REPONSE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);

    assertEquals(false,
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS).getSessions()
            .get(0).getSession().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnValidSessionListWithChatMessagesReadFalse_WhenThereAreUnreadMessages()
      throws Exception {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(null);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);

    assertEquals(false,
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS).getSessions()
            .get(0).getChat().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnCorrectlySortedSessionList()
      throws Exception {

    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_REPONSE_DTO_LIST);
    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);

    UserSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    for (UserSessionResponseDTO dto : responseList.getSessions()) {
      Long previousDate = (dto.getSession() != null) ? dto.getSession().getMessageDate()
          : dto.getChat().getMessageDate();
      if (dto.getSession() != null) {
        assertTrue(previousDate <= dto.getSession().getMessageDate());
      } else {
        assertTrue(previousDate <= dto.getChat().getMessageDate());
      }
    }
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_SetCorrectSessionMessageDate() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(null);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_REPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);

    UserSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertEquals(
        Helper
            .getUnixTimestampFromDate(ROOMS_UPDATE_DTO_LIST.get(0).getLastMessage().getTimestamp()),
        responseList.getSessions().get(0).getSession().getMessageDate());
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_SetCorrectChatMessageDate() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(null);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);

    UserSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertEquals(
        Helper
            .getUnixTimestampFromDate(ROOMS_UPDATE_DTO_LIST.get(0).getLastMessage().getTimestamp()),
        responseList.getSessions().get(0).getChat().getMessageDate());
  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnCorrectFileTypeAndImagePreviewForSession() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(null);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_REPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any()))
        .thenReturn(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);

    UserSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertEquals(FILE_DTO.getType(),
        responseList.getSessions().get(0).getSession().getAttachment().getFileType());
    assertEquals(ATTACHMENT_DTO.getImagePreview(),
        responseList.getSessions().get(0).getSession().getAttachment().getImagePreview());

  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnCorrectFileTypeAndImagePreviewForChat() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(Arrays.asList(USER_CHAT_RESPONSE_DTO));
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(null);
    when(rocketChatService.getRoomsOfUser(Mockito.any()))
        .thenReturn(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT_FOR_CHAT);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);

    UserSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertEquals(FILE_DTO.getType(),
        responseList.getSessions().get(0).getChat().getAttachment().getFileType());
    assertEquals(ATTACHMENT_DTO.getImagePreview(),
        responseList.getSessions().get(0).getChat().getAttachment().getImagePreview());

  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_ReturnNull_WhenSessionAndChatListAreEmpty() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(null);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(null);

    UserSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertNull(responseList.getSessions());

    verify(rocketChatService, times(0)).getSubscriptionsOfUser(Mockito.any());
    verify(rocketChatService, times(0)).getRoomsOfUser(Mockito.any());

  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_MergeSessionsAndChats() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(USER_SESSION_REPONSE_DTO_LIST);

    UserSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    assertNotNull(responseList.getSessions());
    assertEquals(responseList.getSessions().size(),
        USER_CHAT_RESPONSE_DTO_LIST.size() + USER_SESSION_REPONSE_DTO_LIST.size());

    for (UserSessionResponseDTO userSessionResponseDTO : USER_CHAT_RESPONSE_DTO_LIST) {
      boolean containsChat = false;
      for (UserSessionResponseDTO response : responseList.getSessions()) {
        if (response.getChat() != null
            && response.getChat().equals(userSessionResponseDTO.getChat())) {
          containsChat = true;
        }
      }
      if (!containsChat) {
        fail("ResponseList does not contain all expected chats");
      }
    }

    for (UserSessionResponseDTO userSessionResponseDTO : USER_SESSION_REPONSE_DTO_LIST) {
      boolean containsSession = false;
      for (UserSessionResponseDTO response : responseList.getSessions()) {
        if (response.getSession() != null
            && response.getSession().equals(userSessionResponseDTO.getSession())) {
          containsSession = true;
        }
      }
      if (!containsSession) {
        fail("ResponseList does not contain all expected sessions");
      }
    }

  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_SetSubscribedFlagToTrue_WhenUserIsAttendeeOfAChat() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(null);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);

    UserSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    for (UserSessionResponseDTO userSessionResponseDTO : responseList.getSessions()) {
      assertTrue(userSessionResponseDTO.getChat().isSubscribed());
    }

  }

  @Test
  public void getSessionsForAuthenticatedUser_Should_SetSubscribedFlagToFalse_WhenUserIsNotAttendeeOfAChat() {

    when(chatService.getChatsForUserId(USER_ID)).thenReturn(USER_CHAT_RESPONSE_DTO_LIST);
    when(sessionService.getSessionsForUserId(USER_ID)).thenReturn(null);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(EMPTY_ROOMS_UPDATE_DTO_LIST);

    UserSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedUser(USER_ID, RC_CREDENTIALS);

    for (UserSessionResponseDTO userSessionResponseDTO : responseList.getSessions()) {
      assertFalse(userSessionResponseDTO.getChat().isSubscribed());
    }

  }

  /**
   * Method: getSessionsForAuthenticatedConsultant
   * 
   */

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnValidSessionListWithMessagesReadTrue_WhenThereAreNoUnreadMessages()
      throws Exception {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(true, response.getSessions().get(0).getSession().isMessagesRead());
    assertNotNull(response.getSessions().get(0).getSession().getAskerRcId());
    assertEquals(Helper.getUnixTimestampFromDate(response.getSessions().get(0).getLatestMessage()),
        response.getSessions().get(0).getSession().getMessageDate());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnValidSessionListWithMessagesReadFalse_WhenThereAreUnreadMessages()
      throws Exception {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(false, response.getSessions().get(0).getSession().isMessagesRead());
    assertNotNull(response.getSessions().get(0).getSession().getAskerRcId());
    assertEquals(Helper.getUnixTimestampFromDate(response.getSessions().get(0).getLatestMessage()),
        response.getSessions().get(0).getSession().getMessageDate());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnCorrectlySortedSessionList()
      throws Exception {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    for (ConsultantSessionResponseDTO dto : response.getSessions()) {
      Long previousDate = dto.getSession().getMessageDate();
      assertTrue(previousDate <= dto.getSession().getMessageDate());
    }
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_SetCorrectMessageDate() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(
        Helper
            .getUnixTimestampFromDate(ROOMS_UPDATE_DTO_LIST.get(0).getLastMessage().getTimestamp()),
        response.getSessions().get(0).getSession().getMessageDate());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnTrueAsAttachmentReceivedStatus_WhenCallingConsultantIsNotSenderOfTheAttachment() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().getAttachment().isFileReceived());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnFalseAsAttachmentReceivedStatus_WhenCallingConsultantIsSenderOfTheAttachment() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT_2, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertFalse(response.getSessions().get(1).getSession().getAttachment().isFileReceived());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_SetSubscribedFlagToTrue_WhenConsultantIsAttendeeOfAChat() {

    when(chatService.getChatsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(sessionService.getSessionsForConsultant(CONSULTANT, SESSION_STATUS_IN_PROGRESS))
        .thenReturn(null);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);

    ConsultantSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    for (ConsultantSessionResponseDTO consultantSessionResponseDTO : responseList.getSessions()) {
      assertTrue(consultantSessionResponseDTO.getChat().isSubscribed());
    }

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_SetSubscribedFlagToFalse_WhenConsultantIsNotAttendeeOfAChat() {

    when(chatService.getChatsForConsultant(CONSULTANT))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(sessionService.getSessionsForConsultant(CONSULTANT, SESSION_STATUS_IN_PROGRESS))
        .thenReturn(null);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(EMPTY_ROOMS_UPDATE_DTO_LIST);

    ConsultantSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    for (ConsultantSessionResponseDTO consultantSessionResponseDTO : responseList.getSessions()) {
      assertFalse(consultantSessionResponseDTO.getChat().isSubscribed());
    }

  }

  /**
   * Method: getTeamSessionsForAuthenticatedConsultant
   * 
   */

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithMessagesReadTrue_WhenThereAreNoUnreadMessages()
      throws Exception {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(true, response.getSessions().get(0).getSession().isMessagesRead());
    assertNotNull(response.getSessions().get(0).getSession().getAskerRcId());
    assertEquals(Helper.getUnixTimestampFromDate(response.getSessions().get(0).getLatestMessage()),
        response.getSessions().get(0).getSession().getMessageDate());
  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithMessagesReadFalse_WhenThereAreUnreadMessages()
      throws Exception {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(false, response.getSessions().get(0).getSession().isMessagesRead());
    assertNotNull(response.getSessions().get(0).getSession().getAskerRcId());
    assertEquals(Helper.getUnixTimestampFromDate(response.getSessions().get(0).getLatestMessage()),
        response.getSessions().get(0).getSession().getMessageDate());
  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectlySortedSessionList()
      throws Exception {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_USER_ID,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    for (ConsultantSessionResponseDTO dto : response.getSessions()) {
      Long previousDate = dto.getSession().getMessageDate();
      assertTrue(previousDate <= dto.getSession().getMessageDate());
    }
  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithFeedbackReadTrue_WhenThereAreNoUnreadFeedbackMessages()
      throws Exception {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().isFeedbackRead());

  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithFeedbackReadTrue_WhenFeedbackGroupIdIsNull()
      throws Exception {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITHOUT_FEEDBACK_CHAT);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().isFeedbackRead());

  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnMessageDateAsUnixtime0WhenNoMessages() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().getMessageDate()
        .equals(Helper.UNIXTIME_0.getTime()));

  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_SetCorrectMessageDate() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(
        Helper
            .getUnixTimestampFromDate(ROOMS_UPDATE_DTO_LIST.get(0).getLastMessage().getTimestamp()),
        response.getSessions().get(0).getSession().getMessageDate());
  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_WhenCountIsGreaterThanTotal() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), response.getTotal());
  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_WhenCountIsSmallerThanTotal() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() - 1, SessionFilter.ALL);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), response.getTotal());
  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectOffset() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(OFFSET_0, response.getOffset());

  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnNoSessionsIfOffsetIsGreaterThanTotal() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0 + CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 1, COUNT_10,
            SessionFilter.ALL);

    assertNull(response);

  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectNumberOfSessions_WhenCountIsSmallerThanTotal() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_1, SessionFilter.ALL);

    assertEquals(COUNT_1, response.getSessions().size());

  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectNumberOfSessions_WhenCountIsGreaterThanTotal() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 5, SessionFilter.ALL);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), response.getSessions().size());

  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnFilteredSessionList_WhenFeedbackFilter() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_ONE_FEEDBACK_UNREAD);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.FEEDBACK);

    assertEquals(1, response.getSessions().size());
    assertFalse(response.getSessions().get(0).getSession().isFeedbackRead());

  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectCount_WhenCountIsGreaterThanAmountOfSessions() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 5, SessionFilter.ALL);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), response.getCount());

  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_ShouldNot_SetIsFeedbackReadToFalse_WhenNoMessageWasPostedInTheFeedbackRoom() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ONE_SESSION_WITH_FEEDBACK);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any()))
        .thenReturn(ROOMS_UPDATE_DTO_LIST_WITHOUT_FEEDBACK);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().isFeedbackRead());

  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_SendNullAsMessageAndLogError_WhenDecryptionOfMessageFails() {

    CustomCryptoException exception = new CustomCryptoException(new Exception());

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenThrow(exception);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertNull(response.getSessions().get(0).getSession().getLastMessage());
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_SendNullAsMessageAndLogError_WhenTruncationOfMessageFails() {

    IndexOutOfBoundsException exception = new IndexOutOfBoundsException();

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenThrow(exception);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertNull(response.getSessions().get(0).getSession().getLastMessage());
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_SendListWithDecryptedMessages() {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_MESSAGE);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(response.getSessions().get(0).getSession().getLastMessage(), DECRYPTED_MESSAGE);
  }

  @Test
  public void getTeamSessionsForAuthenticatedConsultant_Should_ReturnCorrectFileTypeAndImagePreview()
      throws Exception {

    when(sessionService.getTeamSessionsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any()))
        .thenReturn(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO responseList =
        getSessionListFacade.getTeamSessionsForAuthenticatedConsultant(CONSULTANT, RC_TOKEN,
            OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(FILE_DTO.getType(),
        responseList.getSessions().get(0).getSession().getAttachment().getFileType());
    assertEquals(ATTACHMENT_DTO.getImagePreview(),
        responseList.getSessions().get(0).getSession().getAttachment().getImagePreview());

  }


  /**
   * Method: getSessionsForAuthenticatedConsultant
   * 
   */

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithFeedbackReadFalse_WhenThereAreUnreadFeedbackMessages()
      throws Exception {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertFalse(response.getSessions().get(0).getSession().isFeedbackRead());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithChatMessagesReadFalse_WhenThereAreUnreadSessionMessages()
      throws Exception {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertFalse(response.getSessions().get(0).getSession().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithChatMessagesReadFalse_WhenThereAreUnreadChatMessages()
      throws Exception {

    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertFalse(response.getSessions().get(0).getChat().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithFeedbackReadTrue_WhenThereAreNoUnreadFeedbackMessages()
      throws Exception {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().isFeedbackRead());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithChatMessagesReadTrue_WhenThereAreNoUnreadSessionMessages()
      throws Exception {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithChatMessagesReadTrue_WhenThereAreNoUnreadChatMessages()
      throws Exception {

    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getChat().isMessagesRead());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnSessionListWithFeedbackReadTrue_WhenFeedbackGroupIdIsNull()
      throws Exception {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITHOUT_FEEDBACK_CHAT);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().isFeedbackRead());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnMessageDateAsUnixtime0_WhenNoMessages() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().getMessageDate()
        .equals(Helper.UNIXTIME_0.getTime()));

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_WhenCountIsGreaterThanTotal() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), response.getTotal());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnCorrectTotalValue_WhenCountIsSmallerThanTotal() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response = getSessionListFacade
        .getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_IN_PROGRESS, RC_TOKEN,
            OFFSET_0, CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() - 1, SessionFilter.ALL);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), response.getTotal());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnCorrectOffset() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(OFFSET_0, response.getOffset());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnNoSessions_WhenOffsetIsGreaterThanTotal() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);

    ConsultantSessionListResponseDTO response = getSessionListFacade
        .getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_IN_PROGRESS, RC_TOKEN,
            OFFSET_0 + CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 1, COUNT_10,
            SessionFilter.ALL);

    assertNull(response.getSessions());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnCorrectNumberOfSessions_WhenCountIsSmallerThanTotal() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_1, SessionFilter.ALL);

    assertEquals(COUNT_1, response.getSessions().size());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnCorrectNumberOfSessions_WhenCountIsGreaterThanTotal() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response = getSessionListFacade
        .getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_IN_PROGRESS, RC_TOKEN,
            OFFSET_0, CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 5, SessionFilter.ALL);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), response.getSessions().size());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnFilteredSessionList_WhenFeedbackFilterIsSet() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_ONE_FEEDBACK_UNREAD);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.FEEDBACK);

    assertEquals(1, response.getSessions().size());
    assertFalse(response.getSessions().get(0).getSession().isFeedbackRead());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnCorrectCount_WhenCountIsGreaterThanAmountOfSessions() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response = getSessionListFacade
        .getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_IN_PROGRESS, RC_TOKEN,
            OFFSET_0, CONSULTANT_SESSION_RESPONSE_DTO_LIST.size() + 5, SessionFilter.ALL);

    assertEquals(CONSULTANT_SESSION_RESPONSE_DTO_LIST.size(), response.getCount());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_ShouldNot_SetIsFeedbackReadToFalse_WhenNoMessageWasPostedInTheFeedbackRoom() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ONE_SESSION_WITH_FEEDBACK);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any()))
        .thenReturn(ROOMS_UPDATE_DTO_LIST_WITHOUT_FEEDBACK);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().isFeedbackRead());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_SendNullAsMessageAndLogError_WhenDecryptionOfSessionMessageFails() {

    CustomCryptoException exception = new CustomCryptoException(new Exception());

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenThrow(exception);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertNull(response.getSessions().get(0).getSession().getLastMessage());
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_SendNullAsMessageAndLogError_WhenDecryptionOfChatMessageFails() {

    CustomCryptoException exception = new CustomCryptoException(new Exception());

    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenThrow(exception);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertNull(response.getSessions().get(0).getChat().getLastMessage());
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_SendNullAsMessageAndLogError_WhenTruncationOfSessionMessageFails() {

    IndexOutOfBoundsException exception = new IndexOutOfBoundsException();

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenThrow(exception);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertNull(response.getSessions().get(0).getSession().getLastMessage());
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_SendNullAsMessageAndLogError_WhenTruncationOfChatMessageFails() {

    IndexOutOfBoundsException exception = new IndexOutOfBoundsException();

    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString())).thenThrow(exception);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertNull(response.getSessions().get(0).getChat().getLastMessage());
    verify(logger, atLeastOnce()).error(anyString(), anyString(), anyString());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_SendListWithDecryptedSessionAndChatMessages() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_MESSAGE);
    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(response.getSessions().get(0).getSession().getLastMessage(), DECRYPTED_MESSAGE);
    assertEquals(response.getSessions().get(1).getChat().getLastMessage(), DECRYPTED_MESSAGE);
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_SendListWithMonitoringFalse_When_NoMonitoringSetInConsultingTypeSettings() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_MESSAGE);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertFalse(response.getSessions().get(0).getSession().isMonitoring());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_SendListWithMonitoringTrue_When_MonitoringSetInConsultingTypeSettings() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_MESSAGE);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITH_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertTrue(response.getSessions().get(0).getSession().isMonitoring());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ThrowServiceException_When_SessionHasInvalidConsultingType()
      throws ServiceException {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_INVALID_CONSULTING_TYPE);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);

    try {
      getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
          SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);
      fail("The expected ServiceException was not thrown.");
    } catch (ServiceException ex) {
      assertTrue("Expected ServiceException thrown", true);
    }
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnCorrectFileTypeAndImagePreviewForSession()
      throws Exception {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITHOUT_FEEDBACK_CHAT);
    when(rocketChatService.getRoomsOfUser(Mockito.any()))
        .thenReturn(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(FILE_DTO.getType(),
        responseList.getSessions().get(0).getSession().getAttachment().getFileType());
    assertEquals(ATTACHMENT_DTO.getImagePreview(),
        responseList.getSessions().get(0).getSession().getAttachment().getImagePreview());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnCorrectFileTypeAndImagePreviewForChat()
      throws Exception {

    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(rocketChatService.getRoomsOfUser(Mockito.any()))
        .thenReturn(ROOMS_UPDATE_DTO_LIST_WITH_ATTACHMENT_FOR_CHAT);
    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITH_UNREADS);
    when(decryptionService.decrypt(Mockito.anyString(), Mockito.anyString())).thenReturn(MESSAGE);

    ConsultantSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertEquals(FILE_DTO.getType(),
        responseList.getSessions().get(0).getChat().getAttachment().getFileType());
    assertEquals(ATTACHMENT_DTO.getImagePreview(),
        responseList.getSessions().get(0).getChat().getAttachment().getImagePreview());

  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_ReturnNull_WhenSessionAndChatListAreEmpty() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any())).thenReturn(null);
    when(chatService.getChatsForConsultant(Mockito.any())).thenReturn(null);

    ConsultantSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertNull(responseList.getSessions());

    verify(rocketChatService, times(0)).getSubscriptionsOfUser(Mockito.any());
    verify(rocketChatService, times(0)).getRoomsOfUser(Mockito.any());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_ShouldNot_SendChatsInEnquiryList() {

    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_MESSAGE);

    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO response =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT, SESSION_STATUS_NEW,
            RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertNull(response.getSessions().get(0).getChat());

    verify(chatService, times(0)).getChatsForConsultant(Mockito.any());
  }

  @Test
  public void getSessionsForAuthenticatedConsultant_Should_MergeSessionsAndChats() {

    when(chatService.getChatsForConsultant(Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE);
    when(sessionService.getSessionsForConsultant(Mockito.any(), Mockito.any()))
        .thenReturn(CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_MESSAGE);

    when(rocketChatService.getSubscriptionsOfUser(Mockito.any()))
        .thenReturn(SUBSCRIPTIONS_UPDATE_LIST_DTO_WITHOUT_UNREADS);
    when(rocketChatService.getRoomsOfUser(Mockito.any())).thenReturn(ROOMS_UPDATE_DTO_LIST);
    when(decryptionService.decrypt(Mockito.any(), Mockito.anyString()))
        .thenReturn(DECRYPTED_MESSAGE);
    when(consultingTypeManager.getConsultantTypeSettings(Mockito.any()))
        .thenReturn(CONSULTING_TYPE_SETTINGS_WITHOUT_MONITORING);

    ConsultantSessionListResponseDTO responseList =
        getSessionListFacade.getSessionsForAuthenticatedConsultant(CONSULTANT,
            SESSION_STATUS_IN_PROGRESS, RC_TOKEN, OFFSET_0, COUNT_10, SessionFilter.ALL);

    assertNotNull(responseList.getSessions());
    assertEquals(responseList.getSessions().size(),
        CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE.size()
            + CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_MESSAGE.size());

    for (ConsultantSessionResponseDTO consultantSessionResponseDTO : CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_CHAT_MESSAGE) {
      boolean containsChat = false;
      for (ConsultantSessionResponseDTO response : responseList.getSessions()) {
        if (response.getChat() != null
            && response.getChat().equals(consultantSessionResponseDTO.getChat())) {
          containsChat = true;
        }
      }
      if (!containsChat) {
        fail("ResponseList does not contain all expected chats");
      }
    }

    for (ConsultantSessionResponseDTO consultantSessionResponseDTO : CONSULTANT_SESSION_RESPONSE_DTO_LIST_WITH_ENCRYPTED_MESSAGE) {
      boolean containsSession = false;
      for (ConsultantSessionResponseDTO response : responseList.getSessions()) {
        if (response.getSession() != null
            && response.getSession().equals(consultantSessionResponseDTO.getSession())) {
          containsSession = true;
        }
      }
      if (!containsSession) {
        fail("ResponseList does not contain all expected sessions");
      }
    }

  }

}
