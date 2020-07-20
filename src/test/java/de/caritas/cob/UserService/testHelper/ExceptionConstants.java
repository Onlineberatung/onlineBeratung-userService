package de.caritas.cob.UserService.testHelper;

import static de.caritas.cob.UserService.testHelper.TestConstants.MESSAGE;
import static de.caritas.cob.UserService.testHelper.TestConstants.RC_FEEDBACK_GROUP_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.RC_GROUP_ID;
import static de.caritas.cob.UserService.testHelper.TestConstants.SESSION;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import de.caritas.cob.UserService.api.container.CreateEnquiryExceptionInformation;
import de.caritas.cob.UserService.api.exception.CreateMonitoringException;
import de.caritas.cob.UserService.api.exception.EnquiryMessageException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatCreateGroupException;
import de.caritas.cob.UserService.api.exception.rocketChat.RocketChatPostMessageException;

public class ExceptionConstants {

  /**
   * Common exceptions
   */
  public static final Exception EXCEPTION = new Exception();
  @SuppressWarnings("serial")
  public static final DataAccessException DATA_ACCESS_EXCEPTION =
      new DataAccessException(MESSAGE) {};

  /*
   * Enquiry exceptions
   */
  public static final CreateEnquiryExceptionInformation CREATE_ENQUIRY_EXCEPTION_PARAMETER =
      CreateEnquiryExceptionInformation.builder().rcGroupId(RC_GROUP_ID)
          .rcFeedbackGroupId(RC_FEEDBACK_GROUP_ID).session(SESSION).build();
  public static final EnquiryMessageException ENQUIRY_MESSAGE_EXCEPTION =
      new EnquiryMessageException(EXCEPTION, CREATE_ENQUIRY_EXCEPTION_PARAMETER);

  /**
   * Monitoring exceptions
   */
  public static final CreateMonitoringException CREATE_MONITORING_EXCEPTION =
      new CreateMonitoringException(MESSAGE, EXCEPTION, CREATE_ENQUIRY_EXCEPTION_PARAMETER);

  /*
   * Rocket.Chat exceptions
   * 
   */
  public static final RocketChatCreateGroupException RC_CREATE_GROUP_EXCEPTION =
      new RocketChatCreateGroupException(new Exception());
  public static final RocketChatPostMessageException RC_POST_MESSAGE_EXCEPTION =
      new RocketChatPostMessageException(MESSAGE, CREATE_ENQUIRY_EXCEPTION_PARAMETER);

  /**
   * HttpStatusCode exception
   */
  @SuppressWarnings("serial")
  public static final HttpStatusCodeException HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR_EXCEPTION =
      new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR) {};
  @SuppressWarnings("serial")
  public static final HttpStatusCodeException HTTP_STATUS_CODE_UNAUTHORIZED_EXCEPTION =
      new HttpStatusCodeException(HttpStatus.UNAUTHORIZED) {};
}
