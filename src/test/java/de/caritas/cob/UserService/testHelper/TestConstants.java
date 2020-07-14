package de.caritas.cob.UserService.testHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import de.caritas.cob.UserService.api.container.RocketChatCredentials;
import de.caritas.cob.UserService.api.helper.AuthenticatedUser;
import de.caritas.cob.UserService.api.manager.consultingType.ConsultingTypeSettings;
import de.caritas.cob.UserService.api.manager.consultingType.SessionDataInitializing;
import de.caritas.cob.UserService.api.manager.consultingType.registration.Registration;
import de.caritas.cob.UserService.api.manager.consultingType.registration.mandatoryFields.MandatoryFields;
import de.caritas.cob.UserService.api.model.AbsenceDTO;
import de.caritas.cob.UserService.api.model.AgencyDTO;
import de.caritas.cob.UserService.api.model.ChatDTO;
import de.caritas.cob.UserService.api.model.CreateChatResponseDTO;
import de.caritas.cob.UserService.api.model.UserDTO;
import de.caritas.cob.UserService.api.model.rocketChat.RocketChatUserDTO;
import de.caritas.cob.UserService.api.model.rocketChat.group.GroupMemberDTO;
import de.caritas.cob.UserService.api.model.rocketChat.login.DataDTO;
import de.caritas.cob.UserService.api.model.rocketChat.login.LoginResponseDTO;
import de.caritas.cob.UserService.api.model.rocketChat.message.attachment.AttachmentDTO;
import de.caritas.cob.UserService.api.model.rocketChat.message.attachment.FileDTO;
import de.caritas.cob.UserService.api.model.rocketChat.room.RoomsUpdateDTO;
import de.caritas.cob.UserService.api.model.rocketChat.user.UserInfoResponseDTO;
import de.caritas.cob.UserService.api.repository.chat.Chat;
import de.caritas.cob.UserService.api.repository.chat.ChatInterval;
import de.caritas.cob.UserService.api.repository.chatAgency.ChatAgency;
import de.caritas.cob.UserService.api.repository.consultant.Consultant;
import de.caritas.cob.UserService.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.UserService.api.repository.session.ConsultingType;
import de.caritas.cob.UserService.api.repository.session.Session;
import de.caritas.cob.UserService.api.repository.session.SessionFilter;
import de.caritas.cob.UserService.api.repository.session.SessionStatus;
import de.caritas.cob.UserService.api.repository.user.User;
import de.caritas.cob.UserService.api.repository.userAgency.UserAgency;

public class TestConstants {

  /*
   * Common
   */
  public static final String APPLICATION_BASE_URL = "https://beratung.caritas.de";
  public static final String HOST_BASE_URL = "https://beratung.caritas.de";
  public static final String APPLICATION_BASE_URL_FIELD_NAME = "APPLICATION_BASE_URL";
  public static final String POSTCODE = "12345";
  public static final String INVALID_POSTCODE = "12";
  public static final String NAME = "testname";
  public static final String CITY = "testcity";
  public static final String ERROR = "error";
  public static final String NULL = null;
  public static final boolean SUCCESS = true;
  public static final Exception EXCEPTION = new Exception();

  /**
   * ConsultingTypes
   */
  public static final int INVALID_CONSULTING_TYPE = 9999;
  public static final String CONSULTING_TYPE_SUCHT_URL_NAME = ConsultingType.SUCHT.getUrlName();
  public static final ConsultingType CONSULTING_TYPE_SUCHT = ConsultingType.SUCHT;
  public static final ConsultingType CONSULTING_TYPE_U25 = ConsultingType.U25;
  public static final ConsultingType CONSULTING_TYPE_PREGNANCY = ConsultingType.PREGNANCY;
  public static final ConsultingType CONSULTING_TYPE_AIDS = ConsultingType.AIDS;
  public static final ConsultingType CONSULTING_TYPE_CHILDREN = ConsultingType.CHILDREN;
  public static final ConsultingType CONSULTING_TYPE_CURE = ConsultingType.CURE;
  public static final ConsultingType CONSULTING_TYPE_DEBT = ConsultingType.DEBT;
  public static final ConsultingType CONSULTING_TYPE_DISABILITY = ConsultingType.DISABILITY;
  public static final ConsultingType CONSULTING_TYPE_LAW = ConsultingType.LAW;
  public static final ConsultingType CONSULTING_TYPE_OFFENDER = ConsultingType.OFFENDER;
  public static final ConsultingType CONSULTING_TYPE_PARENTING = ConsultingType.PARENTING;
  public static final ConsultingType CONSULTING_TYPE_PLANB = ConsultingType.PLANB;
  public static final ConsultingType CONSULTING_TYPE_REHABILITATION = ConsultingType.REHABILITATION;
  public static final ConsultingType CONSULTING_TYPE_SENIORITY = ConsultingType.SENIORITY;
  public static final ConsultingType CONSULTING_TYPE_SOCIAL = ConsultingType.SOCIAL;
  public static final ConsultingType CONSULTING_TYPE_KREUZBUND = ConsultingType.KREUZBUND;
  public static final ConsultingType CONSULTING_TYPE_MIGRATION = ConsultingType.MIGRATION;
  public static final ConsultingType CONSULTING_TYPE_EMIGRATION = ConsultingType.EMIGRATION;
  public static final ConsultingType CONSULTING_TYPE_HOSPICE = ConsultingType.HOSPICE;
  public static final ConsultingType CONSULTING_TYPE_REGIONAL = ConsultingType.REGIONAL;

  /*
   * Session data
   */
  public static final String ACLOHOL = "alcohol";
  public static final String DRUGS = "drugs";
  public static final String OTHERS = "others";
  public static final String ADDICTIVE_DRUGS = "addictiveDrugs";
  public static final String RELATION = "relation";
  public static final String AGE = "age";
  public static final String GENDER = "gender";
  public static final String STATE = "state";

  /*
   * RocketChat
   */
  public static final String ROCKETCHAT_ID = "xN3Mobksn3xdp7gEk";
  public static final String ROCKETCHAT_ID_2 = "tZ6Mdfks5rtxdp7a8i";
  public static final String RC_TOKEN = "2fUGwNSqvpiEDTsMJQ54XeYdx0XzzCWdu0PP0lXFNu8";
  public static final String RC_USER_ID = "ogCRwt3ieDiBNJFaR";
  public static final String RC_USER_ID_2 = "sd3ssdfFFGSGDGWww";
  public static final String RC_USER_ID_3 = "abcdefsDefdfrWrt";
  public static final String RC_USERNAME = "rcUsername";
  public static final String RC_SYSTEM_USERNAME = "system";
  public static final String RC_SYSTEM_PASSWORD = "system";
  public static final String ROCKET_CHAT_SYSTEM_USER_ID = "xN3Msb3ksnfxda7gEk";
  public static final String RC_TOKEN_HEADER_PARAMETER_NAME = "RCToken";
  public static final String RC_USER_ID_HEADER_PARAMETER_NAME = "RCUserId";
  public static final String ROCKET_CHAT_TECHNICAL_USER_ID = "dasd83juiosdf";
  public static final String RC_TECHNICAL_USERNAME = "technical";
  public static final String RC_TECHNICAL_PASSWORD = "technical";
  public static final String RC_GROUP_ID = "jjjuuu";
  public static final String RC_GROUP_ID_2 = "sdfsdff";
  public static final String RC_GROUP_ID_3 = "gggewwww";
  public static final String RC_GROUP_ID_4 = "jjjuuu";
  public static final String RC_GROUP_ID_5 = "aldoeke";
  public static final String RC_GROUP_ID_6 = "vmndsjk";
  public static final String RC_GROUP_NAME = "group-name";
  public static final String RC_STATUS_ONLINE = "online";
  public static final String RC_UTC_OFFSET = "1";
  public static final String RC_FEEDBACK_GROUP_ID = "yyyZZZ";
  public static final String RC_FEEDBACK_GROUP_ID_2 = "gggasaa";
  public static final String RC_FEEDBACK_GROUP_ID_3 = "llldkdks";
  public static final String RC_ATTACHMENT_TITLE = "filename.jpg";
  public static final String RC_ATTACHMENT_FILE_TYPE = "image/jpeg";
  public static final String RC_ATTACHMENT_IMAGE_PREVIEW =
      "/9j/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAAVACADASIAAhEBAxEB/8QAGQAAAgMBAAAAAAAAAAAAAAAAAAYDBAUH/8QAKBAAAQMDBAAFBQAAAAAAAAAAAQIDBAAFBhESITETIkFRgQcjQmHw/8QAGAEAAwEBAAAAAAAAAAAAAAAAAgMEAQX/xAAdEQADAAICAwAAAAAAAAAAAAAAAQIDEQQSIjEy/9oADAMBAAIRAxEAPwCjapLjU7YhHlA+77I+aY0XNgHxZGo52pI9RSvIx6LerBPbsd0U1JLv56jcNff4rHstquEF+PFuc0uBscacgfNc+uJExpMzrpGxdszGL5avw5BYQ+gHno1LdMzs0yKXX56FSFebeDyKzc6iW+4WZ5TzBelAbG3D6HQ/qlbHcPiSVoTKhLUdnSOdDxyafgfWFsKZO24JFiy8ZZfXFaCndSfL1/a1PZ7ZEl3p1l9lJbSNQANKKKg5NNNJDn7QqJsEef8AUB+1OrWIpOoHe3vqnqxYnGs8mT4D7i1ngKUOhRRV+L4AT8j/2Q==";
  public static final List<RoomsUpdateDTO> EMPTY_ROOMS_UPDATE_DTO_LIST =
      new ArrayList<RoomsUpdateDTO>();
  public static final GroupMemberDTO GROUP_MEMBER_SYS_USER =
      new GroupMemberDTO(ROCKET_CHAT_SYSTEM_USER_ID, RC_STATUS_ONLINE, RC_SYSTEM_USERNAME,
          RC_SYSTEM_USERNAME, RC_UTC_OFFSET);
  public static final GroupMemberDTO GROUP_MEMBER_TECH_USER =
      new GroupMemberDTO(ROCKET_CHAT_TECHNICAL_USER_ID, RC_STATUS_ONLINE, RC_TECHNICAL_USERNAME,
          RC_SYSTEM_USERNAME, RC_UTC_OFFSET);
  public static final ResponseEntity<LoginResponseDTO> LOGIN_RESPONSE_ENTITY_BAD_REQUEST =
      new ResponseEntity<LoginResponseDTO>(HttpStatus.BAD_REQUEST);
  public static final DataDTO DATA_DTO_LOGIN = new DataDTO(RC_USER_ID, RC_TOKEN, null);
  public static final DataDTO DATA_DTO_LOGIN_NO_TOKEN = new DataDTO(RC_USER_ID, null, null);
  public static final String STATUS_OK = "OK";
  public static final LoginResponseDTO LOGIN_RESPONSE_DTO =
      new LoginResponseDTO(STATUS_OK, DATA_DTO_LOGIN);
  public static final LoginResponseDTO LOGIN_RESPONSE_DTO_NO_TOKEN =
      new LoginResponseDTO(STATUS_OK, DATA_DTO_LOGIN_NO_TOKEN);
  public static final ResponseEntity<LoginResponseDTO> LOGIN_RESPONSE_ENTITY_OK =
      new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO, HttpStatus.OK);
  public static final ResponseEntity<LoginResponseDTO> LOGIN_RESPONSE_ENTITY_OK_NO_TOKEN =
      new ResponseEntity<LoginResponseDTO>(LOGIN_RESPONSE_DTO_NO_TOKEN, HttpStatus.OK);

  /**
   * Rocket.Chat credentials
   */
  public final static String TECHNICAL_USER_A_USERNAME = "techUserAName";
  public final static String TECHNICAL_USER_A_TOKEN = "techUserAToken";
  public final static String TECHNICAL_USER_A_ID = "techUserAID";

  public final static String TECHNICAL_USER_B_USERNAME = "techUserBName";
  public final static String TECHNICAL_USER_B_TOKEN = "techUserBToken";
  public final static String TECHNICAL_USER_B_ID = "techUserBID";

  public final static String TECHNICAL_USER_C_USERNAME = "techUserCName";
  public final static String TECHNICAL_USER_C_TOKEN = "techUserCToken";
  public final static String TECHNICAL_USER_C_ID = "techUserCID";

  public final static String SYSTEM_USER_A_USERNAME = "sysUserAName";
  public final static String SYSTEM_USER_A_TOKEN = "sysUserAToken";
  public final static String SYSTEM_USER_A_ID = "sysUserAID";

  public final static String SYSTEM_USER_B_USERNAME = "sysUserBName";
  public final static String SYSTEM_USER_B_TOKEN = "sysUserBToken";
  public final static String SYSTEM_USER_B_ID = "sysUserBID";

  public final static String SYSTEM_USER_C_USERNAME = "sysUserBName";
  public final static String SYSTEM_USER_C_TOKEN = "sysUserBToken";
  public final static String SYSTEM_USER_C_ID = "sysUserBID";

  public static final RocketChatCredentials RC_CREDENTIALS =
      RocketChatCredentials.builder().RocketChatToken(RC_TOKEN).RocketChatUserId(RC_USER_ID)
          .RocketChatUsername(RC_USERNAME).TimeStampCreated(LocalDateTime.now()).build();

  public static final RocketChatCredentials RC_CREDENTIALS_TECHNICAL_A =
      RocketChatCredentials.builder().RocketChatToken(TECHNICAL_USER_A_TOKEN)
          .RocketChatUserId(TECHNICAL_USER_A_ID).RocketChatUsername(TECHNICAL_USER_A_USERNAME)
          .TimeStampCreated(LocalDateTime.now().minusMinutes(5)).build();

  public static final RocketChatCredentials RC_CREDENTIALS_TECHNICAL_B =
      RocketChatCredentials.builder().RocketChatToken(TECHNICAL_USER_B_TOKEN)
          .RocketChatUserId(TECHNICAL_USER_B_ID).RocketChatUsername(TECHNICAL_USER_B_USERNAME)
          .TimeStampCreated(LocalDateTime.now().minusMinutes(1)).build();

  public static final RocketChatCredentials RC_CREDENTIALS_TECHNICAL_C =
      RocketChatCredentials.builder().RocketChatToken(TECHNICAL_USER_C_TOKEN)
          .RocketChatUserId(TECHNICAL_USER_C_ID).RocketChatUsername(TECHNICAL_USER_C_USERNAME)
          .TimeStampCreated(LocalDateTime.now().minusMinutes(10)).build();

  public static final RocketChatCredentials RC_CREDENTIALS_SYSTEM_A =
      RocketChatCredentials.builder().RocketChatToken(SYSTEM_USER_A_TOKEN)
          .RocketChatUserId(SYSTEM_USER_A_ID).RocketChatUsername(SYSTEM_USER_A_USERNAME)
          .TimeStampCreated(LocalDateTime.now().minusMinutes(5)).build();

  public static final RocketChatCredentials RC_CREDENTIALS_SYSTEM_B =
      RocketChatCredentials.builder().RocketChatToken(SYSTEM_USER_B_TOKEN)
          .RocketChatUserId(SYSTEM_USER_B_ID).RocketChatUsername(SYSTEM_USER_A_USERNAME)
          .TimeStampCreated(LocalDateTime.now().minusMinutes(1)).build();

  public static final RocketChatCredentials RC_CREDENTIALS_SYSTEM_C =
      RocketChatCredentials.builder().RocketChatToken(SYSTEM_USER_C_TOKEN)
          .RocketChatUserId(SYSTEM_USER_C_ID).RocketChatUsername(SYSTEM_USER_C_USERNAME)
          .TimeStampCreated(LocalDateTime.now().minusMinutes(10)).build();


  /*
   * Agencies
   */
  public static final Long AGENCY_ID = 1L;
  public static final Long AGENCY_ID_2 = 2L;
  public static final Long AGENCY_ID_3 = 3L;
  public static List<Long> AGENCY_ID_LIST = Arrays.asList(1L, 2L);
  public static final String AGENCY_NAME = "Test Beratungsstelle";
  public static final AgencyDTO EMPTY_AGENCY_DTO = new AgencyDTO();
  public static final String DESCRIPTION = "description";
  public static final boolean IS_TEAM_AGENCY = true;
  public static final boolean IS_NOT_OFFLINE = false;
  public static final AgencyDTO AGENCY_DTO_SUCHT = new AgencyDTO(AGENCY_ID, AGENCY_NAME, POSTCODE,
      CITY,
      DESCRIPTION, IS_TEAM_AGENCY, IS_NOT_OFFLINE, CONSULTING_TYPE_SUCHT);
  public static final AgencyDTO AGENCY_DTO_U25 = new AgencyDTO(AGENCY_ID, AGENCY_NAME, POSTCODE,
      CITY,
      DESCRIPTION, IS_TEAM_AGENCY, IS_NOT_OFFLINE, CONSULTING_TYPE_U25);
  public static final AgencyDTO AGENCY_DTO_KREUZBUND = new AgencyDTO(AGENCY_ID, AGENCY_NAME,
      POSTCODE, CITY, DESCRIPTION, IS_TEAM_AGENCY, IS_NOT_OFFLINE, CONSULTING_TYPE_KREUZBUND);
  public static final List<AgencyDTO> AGENCY_DTO_LIST = Collections.singletonList(AGENCY_DTO_SUCHT);
  /*
   * Users / Consultants
   */
  public static final String USER_ID = "9b71cc46-650d-42bb-8299-f8e3f6d7249f";
  public static final String USER_ID_2 = "3234234-3344-32gg-2344-sdfsdf33333";
  public static final String USER_ID_3 = "df3322-fd33-asdf-3333-2332jk23j32j";
  public static final String CONSULTANT_ID = "87ddcss-650d-42bb-8299-f8e3f6j8dk3";
  public static final String CONSULTANT_ID_2 = "kd93kd-fd33-asdf-3333-2332jkkk39d";
  public static final String CONSULTANT_ID_3 = "ksf93j-3344-32gg-2344-93kd93jaf";
  public static final String MAIN_CONSULTANT_ID = "asdj78wfjsdf";
  public static final String RC_USER_ID_MAIN_CONSULTANT = "xxxyyy";
  public static final String TEAM_CONSULTANT_ID = "34t789hqeg-q34g8weq9rhg-q34g09";
  public static final String ENCODING_PREFIX = "enc.";
  public static final String USERNAME_CONSULTANT_DECODED = "Consultantname!#123";
  public static final String USERNAME_CONSULTANT_ENCODED =
      ENCODING_PREFIX + "INXW443VNR2GC3TUNZQW2ZJBEMYTEMY.";
  public static final String USERNAME = "username";
  public static final String USERNAME_SIMPLE_ENCODED = ENCODING_PREFIX + "OVZWK4TOMFWWK===";
  public static final String USERNAME_DECODED = "Username!#123";
  public static final String USERNAME_ENCODED = ENCODING_PREFIX + "KVZWK4TOMFWWKIJDGEZDG...";
  public static final String USERNAME_INVALID_ENCODED = ENCODING_PREFIX + "223======";
  public static final boolean IS_ABSENT = true;
  public static final String ABSENCE_MESSAGE = "Bin nicht da";
  public static final String FIRST_NAME = "vorname";
  public static final String LAST_NAME = "nachname";
  public static final String EMAIL = "email@email.com";
  public static final String PASSWORD = "TestPw!#123";
  public static final String PASSWORD_URL_ENCODED = "TestPw!%23123";
  public static final boolean IS_TEAM_CONSULTANT = true;
  public static final boolean IS_LANGUAGE_FORMAL = true;
  public static final String VALID_AGE = "1";
  public static final String INVALID_AGE = "xxx";
  public static final String VALID_STATE = "1";
  public static final String INVALID_STATE = "xxx";
  public static final Consultant CONSULTANT =
      new Consultant(CONSULTANT_ID, ROCKETCHAT_ID, USERNAME, FIRST_NAME, LAST_NAME, EMAIL,
          IS_ABSENT, IS_TEAM_CONSULTANT, ABSENCE_MESSAGE, IS_LANGUAGE_FORMAL, null, null, null);
  public static final Consultant CONSULTANT_2 = new Consultant(CONSULTANT_ID_2, ROCKETCHAT_ID,
      USERNAME, "first name", "last name", EMAIL, false, false, null, false, null, null, null);
  public static final Consultant CONSULTANT_NO_RC_USER_ID = new Consultant(CONSULTANT_ID, "",
      USERNAME, "first name", "last name", EMAIL, false, false, null, false, null, null, null);
  public static final Consultant MAIN_CONSULTANT =
      new Consultant(MAIN_CONSULTANT_ID, RC_USER_ID_MAIN_CONSULTANT, USERNAME, "first name",
          "last name", EMAIL, false, false, null, false, null, null, null);
  public static final AbsenceDTO ABSENCE_DTO = new AbsenceDTO(true, TestConstants.MESSAGE);
  public static final AbsenceDTO ABSENCE_DTO_WITH_EMPTY_MESSAGE =
      new AbsenceDTO(true, TestConstants.MESSAGE_EMPTY);
  public static final AbsenceDTO ABSENCE_DTO_WITH_NULL_MESSAGE = new AbsenceDTO(true, null);
  public static AbsenceDTO ABSENCE_DTO_WITH_HTML_AND_JS =
      new AbsenceDTO(true, TestConstants.MESSAGE_WITH_HTML_AND_JS);
  public static final GroupMemberDTO GROUP_MEMBER_USER_1 =
      new GroupMemberDTO(RC_USER_ID, RC_STATUS_ONLINE, USERNAME, USERNAME, RC_UTC_OFFSET);
  public static final GroupMemberDTO GROUP_MEMBER_USER_2 = new GroupMemberDTO(RC_USER_ID_2,
      RC_STATUS_ONLINE, USERNAME_SIMPLE_ENCODED, USERNAME_SIMPLE_ENCODED, RC_UTC_OFFSET);
  public static final List<GroupMemberDTO> GROUP_MEMBER_DTO_LIST = Arrays.asList(
      GROUP_MEMBER_SYS_USER, GROUP_MEMBER_TECH_USER, GROUP_MEMBER_USER_1, GROUP_MEMBER_USER_2);
  public static final User USER = new User(USER_ID, null, USERNAME, EMAIL, IS_LANGUAGE_FORMAL);
  public static final User USER_WITH_RC_ID =
      new User(USER_ID, null, USERNAME, EMAIL, RC_USER_ID, IS_LANGUAGE_FORMAL, null, null);
  public static final User USER_WITH_RC_ID_2 =
      new User(USER_ID_2, null, USERNAME, EMAIL, RC_USER_ID_2, IS_LANGUAGE_FORMAL, null, null);
  public static final User USER_NO_RC_USER_ID =
      new User(USER_ID, null, USERNAME, EMAIL, null, false, null, null);
  public static final String ACCESS_TOKEN = "DASDLAJS835u83hKSAJDF";
  public static final AuthenticatedUser AUTHENTICATED_USER =
      new AuthenticatedUser(USER_ID, USERNAME, null, ACCESS_TOKEN, null);
  public static final AuthenticatedUser AUTHENTICATED_USER_3 =
      new AuthenticatedUser(USER_ID_3, USERNAME, null, ACCESS_TOKEN, null);
  public static final AuthenticatedUser AUTHENTICATED_USER_CONSULTANT =
      new AuthenticatedUser(CONSULTANT_ID, USERNAME, null, ACCESS_TOKEN, null);
  public static final User USER_NO_DATA = new User(null, null, null, null, true);
  public static final UserDTO USER_DTO_SUCHT =
      new UserDTO(USERNAME, POSTCODE, AGENCY_ID, PASSWORD, EMAIL, null, null, null, null, null,
          "true", Integer.toString(CONSULTING_TYPE_SUCHT.getValue()));
  public static final UserDTO USER_DTO_SUCHT_WITH_INVALID_POSTCODE =
      new UserDTO(USERNAME, INVALID_POSTCODE, AGENCY_ID, PASSWORD, EMAIL, null, null, null, null,
          null, "true", Integer.toString(CONSULTING_TYPE_SUCHT.getValue()));
  public static final UserDTO USER_DTO_SUCHT_WITHOUT_EMAIL =
      new UserDTO(USERNAME, POSTCODE, AGENCY_ID, PASSWORD, null, null, null, null, null, null,
          "true", Integer.toString(CONSULTING_TYPE_SUCHT.getValue()));
  public static final UserDTO USER_DTO_KREUZBUND =
      new UserDTO(USERNAME, POSTCODE, AGENCY_ID, PASSWORD, EMAIL, null, null, null, null, null,
          "true", Integer.toString(CONSULTING_TYPE_KREUZBUND.getValue()));
  public static final UserDTO USER_DTO_WITH_AGE =
      new UserDTO(VALID_AGE, null, Integer.toString(CONSULTING_TYPE_U25.getValue()));
  public static final UserDTO USER_DTO_WITH_INVALID_AGE =
      new UserDTO(INVALID_AGE, null, Integer.toString(CONSULTING_TYPE_U25.getValue()));
  public static final UserDTO USER_DTO_WITHOUT_MANDATORY_AGE = new UserDTO(null, null, null, null,
      null, null, Integer.toString(CONSULTING_TYPE_SUCHT.getValue()));
  public static final UserDTO USER_DTO_WITHOUT_CONSULTING_TYPE = new UserDTO();
  public static final UserDTO USER_DTO_WITH_STATE =
      new UserDTO(null, VALID_STATE, Integer.toString(CONSULTING_TYPE_U25.getValue()));
  public static final UserDTO USER_DTO_WITH_INVALID_STATE =
      new UserDTO(null, INVALID_STATE, Integer.toString(CONSULTING_TYPE_U25.getValue()));
  public static final UserDTO USER_DTO_WITHOUT_MANDATORY_STATE = new UserDTO(null, null, null, null,
      null, null, Integer.toString(CONSULTING_TYPE_SUCHT.getValue()));
  public static final RocketChatUserDTO ROCKET_CHAT_USER_DTO =
      new RocketChatUserDTO(RC_USER_ID, USERNAME);
  public static final UserInfoResponseDTO USER_INFO_RESPONSE_DTO =
      new UserInfoResponseDTO(ROCKET_CHAT_USER_DTO, SUCCESS);
  public static final RocketChatUserDTO ROCKET_CHAT_USER_DTO_2 =
      new RocketChatUserDTO(RC_USER_ID_2, USERNAME);
  public static final UserInfoResponseDTO USER_INFO_RESPONSE_DTO_2 =
      new UserInfoResponseDTO(ROCKET_CHAT_USER_DTO_2, SUCCESS);

  /*
   * ConsultantAgency
   */
  public static final ConsultantAgency[] CONSULTANT_AGENCY =
      new ConsultantAgency[] {new ConsultantAgency(1L, CONSULTANT, AGENCY_ID)};
  public static final ConsultantAgency CONSULTANT_AGENCY_2 =
      new ConsultantAgency(2L, CONSULTANT, AGENCY_ID_2);
  public static final Set<ConsultantAgency> CONSULTANT_AGENCY_SET =
      new HashSet<ConsultantAgency>(Arrays.asList(CONSULTANT_AGENCY));
  public static final Set<Long> CONSULTANT_AGENCY_IDS_SET =
      new HashSet<Long>(Arrays.asList(AGENCY_ID));
  public static final Consultant CONSULTANT_WITH_AGENCY = new Consultant(CONSULTANT_ID,
      ROCKETCHAT_ID, USERNAME, "first name", "last name", EMAIL, false, false, "", false, null,
      null, new HashSet<ConsultantAgency>(Arrays.asList(CONSULTANT_AGENCY)));
  public static final Consultant CONSULTANT_WITH_AGENCY_2 = new Consultant(CONSULTANT_ID_2,
      ROCKETCHAT_ID, USERNAME, "first name", "last name", EMAIL, false, false, null, false, null,
      null, new HashSet<ConsultantAgency>(Arrays.asList(CONSULTANT_AGENCY_2)));

  /**
   * UserAgency
   */
  public static final UserAgency USER_AGENCY = new UserAgency(USER, AGENCY_ID);
  public static final UserAgency USER_AGENCY_2 = new UserAgency(USER, AGENCY_ID_2);
  public static final List<UserAgency> USER_AGENCY_LIST = Arrays.asList(USER_AGENCY, USER_AGENCY_2);
  public static final Set<UserAgency> USER_AGENCY_SET = new HashSet<>(
      Arrays.asList(USER_AGENCY, USER_AGENCY_2));
  public static final User USER_WITH_AGENCIES = new User(USER_ID, null, USERNAME,
      EMAIL, RC_USER_ID,
      IS_LANGUAGE_FORMAL, null, USER_AGENCY_SET);

  /*
   * Session
   */
  public static final Long SESSION_ID = 1L;
  public static final Long TEAM_SESSION_ID = 55L;
  public static final Integer SESSION_STATUS_NEW = 1;
  public static final Integer SESSION_STATUS_IN_PROGRESS = 2;
  public static final Integer SESSION_STATUS_INVALID = 234234324;
  public static final boolean IS_TEAM_SESSION = true;
  public static final boolean IS_NO_TEAM_SESSION = false;
  public static final boolean IS_MONITORING = true;
  public static final boolean IS_NOT_MONITORING = false;
  public static final SessionFilter SESSION_FILTER_ALL = SessionFilter.ALL;
  public static final Long ENQUIRY_ID = 1L;
  public static final Long ENQUIRY_ID_2 = 2L;
  public static final Session SESSION =
      new Session(SESSION_ID, null, null, null, null, null, null, null, null);
  public static final Session SESSION_WITH_CONSULTANT =
      new Session(SESSION_ID, null, CONSULTANT_2, ConsultingType.SUCHT, POSTCODE, AGENCY_ID,
          SessionStatus.IN_PROGRESS, new Date(), RC_GROUP_ID);
  public static final Session ENQUIRY_SESSION_WITH_CONSULTANT =
      new Session(SESSION_ID, null, CONSULTANT_2, ConsultingType.SUCHT, POSTCODE, AGENCY_ID,
          SessionStatus.NEW, new Date(), RC_GROUP_ID);
  public static final Session SESSION_WITHOUT_CONSULTANT =
      new Session(SESSION_ID, USER_WITH_RC_ID, null, ConsultingType.U25, POSTCODE, AGENCY_ID,
          SessionStatus.NEW, null, RC_GROUP_ID, null, null, IS_TEAM_SESSION, IS_MONITORING);
  public static final Session FEEDBACKSESSION_WITHOUT_CONSULTANT = new Session(SESSION_ID,
      USER_WITH_RC_ID, null, ConsultingType.U25, POSTCODE, AGENCY_ID, SessionStatus.NEW, new Date(),
      RC_GROUP_ID, RC_FEEDBACK_GROUP_ID, null, IS_TEAM_SESSION, IS_MONITORING);
  public static final Session FEEDBACKSESSION_WITH_CONSULTANT =
      new Session(SESSION_ID, USER_WITH_RC_ID, CONSULTANT_2, ConsultingType.U25, POSTCODE,
          AGENCY_ID, SessionStatus.IN_PROGRESS, new Date(), RC_GROUP_ID, RC_FEEDBACK_GROUP_ID, null,
          IS_TEAM_SESSION, IS_MONITORING);
  public static final Session SESSION_WITHOUT_CONSULTANT_NO_RC_USER_ID =
      new Session(SESSION_ID, USER_NO_RC_USER_ID, null, ConsultingType.SUCHT, POSTCODE, AGENCY_ID,
          SessionStatus.NEW, new Date(), RC_GROUP_ID, null, IS_NO_TEAM_SESSION, IS_MONITORING);
  public static final Session U25_SESSION_WITH_CONSULTANT = new Session(SESSION_ID, USER_WITH_RC_ID,
      CONSULTANT_2, ConsultingType.U25, POSTCODE, AGENCY_ID, SessionStatus.IN_PROGRESS, new Date(),
      RC_GROUP_ID, RC_FEEDBACK_GROUP_ID, null, IS_TEAM_SESSION, IS_MONITORING);
  public static final Session U25_SESSION_WITHOUT_CONSULTANT = new Session(SESSION_ID,
      USER_WITH_RC_ID, null, ConsultingType.U25, POSTCODE, AGENCY_ID, SessionStatus.NEW, new Date(),
      RC_GROUP_ID, RC_FEEDBACK_GROUP_ID, null, IS_TEAM_SESSION, IS_MONITORING);
  public static final Set<Session> SESSION_SET = new HashSet<Session>(
      Arrays.asList(U25_SESSION_WITHOUT_CONSULTANT, SESSION_WITHOUT_CONSULTANT_NO_RC_USER_ID));
  public static final User USER_WITH_SESSIONS = new User(USER_ID, null, USERNAME,
      EMAIL, RC_USER_ID,
      IS_LANGUAGE_FORMAL, SESSION_SET, null);

  /**
   * GroupMemberDTO
   */
  public static final GroupMemberDTO GROUP_MEMBER_DTO =
      new GroupMemberDTO(RC_USER_ID, null, USERNAME, null, null);
  public static final GroupMemberDTO GROUP_MEMBER_DTO_2 =
      new GroupMemberDTO(RC_USER_ID_2, null, USERNAME, null, null);
  public static final GroupMemberDTO GROUP_MEMBER_DTO_MAIN_CONSULTANT =
      new GroupMemberDTO(RC_USER_ID_MAIN_CONSULTANT, null, USERNAME, null, null);
  public static final List<GroupMemberDTO> LIST_GROUP_MEMBER_DTO =
      Arrays.asList(GROUP_MEMBER_DTO, GROUP_MEMBER_DTO_2, GROUP_MEMBER_DTO_MAIN_CONSULTANT);

  /*
   * Parameter
   */
  public static int OFFSET_0 = 0;
  public static int COUNT_10 = 10;
  public static int COUNT_1 = 1;

  /*
   * Passwords
   */
  public static final String ENCODED_PASSWORD = "Tester123%24";
  public static final String DECODED_PASSWORD = "Tester123$";

  /*
   * Masterkey
   */
  public static final String MASTER_KEY_1 = "key1";
  public static final String MASTER_KEY_2 = "key2";
  public static final String MASTER_KEY_DTO_KEY_1 = "{\"masterKey\": \"" + MASTER_KEY_1 + "\"}";
  public static final String MASTER_KEY_DTO_KEY_2 = "{\"masterKey\": \"" + MASTER_KEY_2 + "\"}";

  /*
   * Roles
   */
  public static final String USER_ROLE = "user";
  public static final Set<String> USER_ROLES = new HashSet<>(Arrays.asList(USER_ROLE));
  public static final String CONSULTANT_ROLE = "consultant";
  public static final Set<String> CONSULTANT_ROLES = new HashSet<>(Arrays.asList(CONSULTANT_ROLE));

  /*
   * Date and Time
   */
  public static final Date NOW = new Date();
  public static final Date NOW_MINUS_1_DAY = new Date(NOW.getTime() - 86400000);
  public static final Date NOW_MINUS_2_DAYS = new Date(NOW.getTime() - (2 * 86400000));
  public static final Date NOW_MINUS_3_DAYS = new Date(NOW.getTime() - (3 * 86400000));

  /**
   * Registration values
   */
  public static final String USERNAME_TOO_SHORT = "flo";
  public static final String USERNAME_TOO_LONG = "UsernameVielZuLangMit31Zeicheeen";
  public static final String ADDICTIVE_DRUGS_VALUE = "2,4";
  public static final String RELATION_VALUE = "2";
  public static final String AGE_VALUE = "12";
  public static final String INVALID_AGE_VALUE = "12age";
  public static final String GENDER_VALUE = "1";
  public static final String STATE_VALUE = "16";
  public static final boolean TERMS_ACCEPTED = true;

  /*
   * /* Messages
   */
  public static final String ENCRYPTED_MESSAGE =
      "enc:uWHNUkWrQJikGnVpknvB3SkzT1RWHJuY0igDT9p7fGFHWECLBpV2+0eIZF6Qi7J0";
  public static final String DECRYPTED_MESSAGE = "Das hier ist jetzt mal eine Test-Message";
  public static final String MESSAGE = "Testnachricht";
  public static final String MESSAGE_WITH_HTML_AND_JS =
      "<b>Testnachricht</b><script>alert('1');</script>";
  public static final boolean MESSAGES_READ = true;
  public static final boolean MESSAGES_NOT_READ = false;
  public static final String MESSAGE_EMPTY = StringUtils.EMPTY;
  public static final String MESSAGE_WITH_NON_REPLACED_USERNAME = "Hello ${username}";
  public static final String MESSAGE_WITH_REPLACED_USERNAME = "Hello " + USER.getUsername();

  /*
   * Attachments
   */
  public static final FileDTO FILE_DTO = new FileDTO(RC_ATTACHMENT_TITLE, RC_ATTACHMENT_FILE_TYPE);
  public static final AttachmentDTO ATTACHMENT_DTO = new AttachmentDTO(RC_ATTACHMENT_IMAGE_PREVIEW);

  /**
   * Chat
   */
  public static final String CHAT_TOPIC = "Pregnancy";
  public static final String CHAT_TOPIC_2 = "Debts";
  public static final String CHAT_TOPIC_3 = "Children";
  public static final boolean IS_REPETITIVE = true;
  public static final boolean IS_NOT_REPETITIVE = false;
  public static final boolean IS_ACTIVE = true;
  public static final boolean IS_NOT_ACTIVE = false;
  public static final int CHAT_MAX_PARTICIPANTS = 5;
  public static final int CHAT_DURATION_30 = 30;
  public static final int CHAT_DURATION_60 = 60;
  public static final int CHAT_DURATION_90 = 90;
  public static final LocalDate CHAT_START_DATE = LocalDate.of(2019, 10, 23);
  public static final LocalTime CHAT_START_TIME = LocalTime.of(14, 0);
  public static final LocalDateTime CHAT_START_DATETIME =
      LocalDateTime.of(CHAT_START_DATE, CHAT_START_TIME);
  public static final int CHAT_DURATION = 60;
  public static final boolean CHAT_REPETITIVE = false;
  public static final ChatInterval CHAT_INTERVAL_WEEKLY = ChatInterval.WEEKLY;
  public static final Long CHAT_ID = 136L;
  public static final Long CHAT_ID_2 = 137L;
  public static final Long CHAT_ID_3 = 138L;
  public static final String INVALID_CHAT_ID = "xyz";
  public static final String CHAT_ID_ENCODED = "GEZTM";
  public static final String GROUP_CHAT_NAME = "GROUP_CHAT_NAME";
  public static final String CHAT_LINK_SUCHT =
      HOST_BASE_URL + "/" + CONSULTING_TYPE_SUCHT_URL_NAME + "/" + CHAT_ID_ENCODED;
  public static final ChatDTO CHAT_DTO =
      new ChatDTO(CHAT_TOPIC, CHAT_START_DATE, CHAT_START_TIME, CHAT_DURATION, CHAT_REPETITIVE);
  public static final CreateChatResponseDTO CREATE_CHAT_RESPONSE_DTO =
      new CreateChatResponseDTO(RC_GROUP_ID, CHAT_LINK_SUCHT);
  public static final Chat ACTIVE_CHAT = new Chat(CHAT_ID, CHAT_TOPIC, CONSULTING_TYPE_SUCHT,
      LocalDateTime.of(CHAT_START_DATE, CHAT_START_TIME),
      LocalDateTime.of(CHAT_START_DATE, CHAT_START_TIME), CHAT_DURATION_30, IS_REPETITIVE,
      CHAT_INTERVAL_WEEKLY, IS_ACTIVE, CHAT_MAX_PARTICIPANTS, RC_GROUP_ID, CONSULTANT, null);
  public static final Chat INACTIVE_CHAT = new Chat(CHAT_ID_2, CHAT_TOPIC, CONSULTING_TYPE_SUCHT,
      LocalDateTime.of(CHAT_START_DATE, CHAT_START_TIME),
      LocalDateTime.of(CHAT_START_DATE, CHAT_START_TIME), CHAT_DURATION_30, IS_REPETITIVE,
      ChatInterval.WEEKLY, IS_NOT_ACTIVE, CHAT_MAX_PARTICIPANTS, RC_GROUP_ID, CONSULTANT, null);
  public static final ChatAgency CHAT_AGENCY = new ChatAgency(ACTIVE_CHAT, AGENCY_ID);
  public static final Set<ChatAgency> CHAT_AGENCIES =
      new HashSet<ChatAgency>(Arrays.asList(CHAT_AGENCY));

  /**
   * ConsultingTypeSettings
   */

  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_FORMAL_LANGUAGE =
      new ConsultingTypeSettings(ConsultingType.SUCHT, false, null, null, true, null, false, null,
          true, null, null);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_KREUZBUND =
      new ConsultingTypeSettings(ConsultingType.KREUZBUND, false, null, null, true, null, false,
          null, true, null, null);
  public static final MandatoryFields MANDATORY_FIELDS_WITH_AGE = new MandatoryFields(true, false);
  public static final MandatoryFields MANDATORY_FIELDS_WITHOUT_AGE =
      new MandatoryFields(false, false);
  public static final Registration REGISTRATION_WITH_MANDATORY_AGE =
      new Registration(3, MANDATORY_FIELDS_WITH_AGE);
  public static final Registration REGISTRATION_WITHOUT_MANDATORY_AGE =
      new Registration(3, MANDATORY_FIELDS_WITHOUT_AGE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_AGE_MANDATORY =
      new ConsultingTypeSettings(CONSULTING_TYPE_U25, false, null, null, true, null, false, null,
          false, null, REGISTRATION_WITH_MANDATORY_AGE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITHOUT_AGE_MANDATORY =
      new ConsultingTypeSettings(CONSULTING_TYPE_U25, false, null, null, true, null, false, null,
          false, null, REGISTRATION_WITHOUT_MANDATORY_AGE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITHOUT_STATE_FIELD =
      new ConsultingTypeSettings();
  public static final MandatoryFields MANDATORY_FIELDS_WITH_STATE =
      new MandatoryFields(false, true);
  public static final MandatoryFields MANDATORY_FIELDS_WITHOUT_STATE =
      new MandatoryFields(false, false);
  public static final Registration REGISTRATION_WITH_MANDATORY_STATE =
      new Registration(3, MANDATORY_FIELDS_WITH_STATE);
  public static final Registration REGISTRATION_WITHOUT_MANDATORY_STATE =
      new Registration(3, MANDATORY_FIELDS_WITHOUT_STATE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_STATE =
      new ConsultingTypeSettings(CONSULTING_TYPE_U25, false, null, null, true, null, false, null,
          false, null, REGISTRATION_WITH_MANDATORY_STATE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITHOUT_MANDATORY_STATE =
      new ConsultingTypeSettings(CONSULTING_TYPE_U25, false, null, null, true, null, false, null,
          false, null, REGISTRATION_WITHOUT_MANDATORY_STATE);
  public static final MandatoryFields MANDATORY_FIELDS_FALSE = new MandatoryFields(false, false);
  public static final MandatoryFields MANDATORY_FIELDS_TRUE = new MandatoryFields(true, true);
  public static final SessionDataInitializing SESSION_DATA_INITIALIZING =
      new SessionDataInitializing(true, true, true, true, true);
  public static final Registration REGISTRATION_WITH_MANDATORY_FIELDS_TRUE =
      new Registration(3, MANDATORY_FIELDS_TRUE);
  public static final Registration REGISTRATION_WITH_MANDATORY_FIELDS_FALSE =
      new Registration(3, MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITHOUT_MANDATORY_FIELDS =
      new ConsultingTypeSettings(CONSULTING_TYPE_SUCHT, true, "Hallo", SESSION_DATA_INITIALIZING,
          true, null, false, null, false, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_MANDATORY_FIELDS =
      new ConsultingTypeSettings(CONSULTING_TYPE_U25, true, "Hallo", SESSION_DATA_INITIALIZING,
          true, null, false, null, false, null, REGISTRATION_WITH_MANDATORY_FIELDS_TRUE);
  public static final String CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH = "/monitoring/test.json";
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_SUCHT =
      new ConsultingTypeSettings(CONSULTING_TYPE_SUCHT, false, null, SESSION_DATA_INITIALIZING,
          true, CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH, false, null, true, null,
          REGISTRATION_WITH_MANDATORY_FIELDS_TRUE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_U25 =
      new ConsultingTypeSettings(CONSULTING_TYPE_U25, true, "Hallo", SESSION_DATA_INITIALIZING,
          true, CONSULTING_TYPE_SETTINGS_JSON_FILE_PATH, false, null, false, null,
          REGISTRATION_WITH_MANDATORY_FIELDS_TRUE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_PREGNANCY =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, false, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_AIDS =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, true, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_CHILDREN =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, false, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_CURE =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, true, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_DEBT =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, true, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_DISABILITY =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, true, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_LAW =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, true, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_OFFENDER =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, true, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_PARENTING =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, true, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_PLANB =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, false, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_REHABILITATION =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, true, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_SENIORITY =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, true, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_SOCIAL =
      new ConsultingTypeSettings(CONSULTING_TYPE_PREGNANCY, false, null, SESSION_DATA_INITIALIZING,
          false, null, false, null, true, null, REGISTRATION_WITH_MANDATORY_FIELDS_FALSE);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITHOUT_WELCOME_MESSAGE =
      new ConsultingTypeSettings(CONSULTING_TYPE_U25, false, null, null, true, null, false, null,
          false, null, null);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WITH_WELCOME_MESSAGE =
      new ConsultingTypeSettings(CONSULTING_TYPE_U25, true, MESSAGE_WITH_NON_REPLACED_USERNAME,
          null, true, null, false, null, false, null, null);
  public static final ConsultingTypeSettings CONSULTING_TYPE_SETTINGS_WIT_MONITORING =
      new ConsultingTypeSettings(CONSULTING_TYPE_U25, false, null, null, true, null, false, null,
          false, null, null);
}
