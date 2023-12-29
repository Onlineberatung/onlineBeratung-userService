package de.caritas.cob.userservice.api.testHelper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.ADDICTIVE_DRUGS_VALUE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGENCY_ID;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.AGE_VALUE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_DURATION;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_REPETITIVE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_START_DATE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_START_TIME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CHAT_TOPIC;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_U25;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.EMAIL;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.ENCODED_PASSWORD;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.GENDER_VALUE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.INVALID_AGE_VALUE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.INVALID_POSTCODE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.OTP;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.PASSWORD;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.POSTCODE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.RELATION_VALUE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.SECRET;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.STATE_VALUE;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.TERMS_ACCEPTED;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_TOO_LONG;
import static de.caritas.cob.userservice.api.testHelper.TestConstants.USERNAME_TOO_SHORT;

public class RequestBodyConstants {

  public static final String VALID_USER_REQUEST_BODY =
      "{\"username\": \""
          + USERNAME
          + "\","
          + "\"postcode\": \""
          + POSTCODE
          + "\", \"agencyId\": "
          + AGENCY_ID
          + ", \"password\": \""
          + PASSWORD
          + "\","
          + "\"email\": \""
          + EMAIL
          + "\", \"addictiveDrugs\": \""
          + ADDICTIVE_DRUGS_VALUE
          + "\","
          + "\"relation\": \""
          + RELATION_VALUE
          + "\", \"age\": \""
          + AGE_VALUE
          + "\", \"gender\": \""
          + GENDER_VALUE
          + "\","
          + "\"termsAccepted\": \""
          + TERMS_ACCEPTED
          + "\", \"consultingType\": \""
          + CONSULTING_TYPE_ID_SUCHT
          + "\"}";
  public static final String INVALID_USER_REQUEST_BODY_WITOUT_POSTCODE =
      "{\"username\": \""
          + USERNAME
          + "\","
          + "\"agencyId\": "
          + AGENCY_ID
          + ", \"password\": \""
          + PASSWORD
          + "\","
          + "\"email\": \""
          + EMAIL
          + "\", \"addictiveDrugs\": \""
          + ADDICTIVE_DRUGS_VALUE
          + "\","
          + "\"relation\": \""
          + RELATION_VALUE
          + "\", \"age\": \""
          + AGE_VALUE
          + "\", \"gender\": \""
          + GENDER_VALUE
          + "\","
          + "\"termsAccepted\": \""
          + TERMS_ACCEPTED
          + "\", \"consultingType\": \""
          + CONSULTING_TYPE_ID_SUCHT
          + "\"}";
  public static final String INVALID_USER_REQUEST_BODY_WITH_INVALID_POSTCODE =
      "{\"username\": \""
          + USERNAME
          + "\","
          + "\"postcode\": \""
          + INVALID_POSTCODE
          + "\", \"agencyId\": "
          + AGENCY_ID
          + ", \"password\": \""
          + PASSWORD
          + "\","
          + "\"email\": \""
          + EMAIL
          + "\", \"addictiveDrugs\": \""
          + ADDICTIVE_DRUGS_VALUE
          + "\","
          + "\"relation\": \""
          + RELATION_VALUE
          + "\", \"age\": \""
          + AGE_VALUE
          + "\", \"gender\": \""
          + GENDER_VALUE
          + "\","
          + "\"termsAccepted\": \""
          + TERMS_ACCEPTED
          + "\", \"consultingType\": \""
          + CONSULTING_TYPE_ID_SUCHT
          + "\"}";
  public static final String VALID_USER_REQUEST_BODY_WITH_ENCODED_PASSWORD =
      "{\"username\": \""
          + USERNAME
          + "\","
          + "\"postcode\": \""
          + POSTCODE
          + "\", \"agencyId\": "
          + AGENCY_ID
          + ", \"password\": \""
          + ENCODED_PASSWORD
          + "\","
          + "\"email\": \""
          + EMAIL
          + "\", \"addictiveDrugs\": \""
          + ADDICTIVE_DRUGS_VALUE
          + "\","
          + "\"relation\": \""
          + RELATION_VALUE
          + "\", \"age\": \""
          + AGE_VALUE
          + "\", \"gender\": \""
          + GENDER_VALUE
          + "\","
          + "\"termsAccepted\": \""
          + TERMS_ACCEPTED
          + "\", \"consultingType\": \""
          + CONSULTING_TYPE_ID_SUCHT
          + "\"}";
  public static final String INVALID_USER_REQUEST_BODY = "{\"in\": \"valid\"}";
  public static final String INVALID_U25_USER_REQUEST_BODY_AGE =
      "{\"username\": \""
          + USERNAME
          + "\",\" + \"\"postcode\": \"\" + POSTCODE + \"\","
          + "\"agencyId\": "
          + AGENCY_ID
          + ", \"agencyId\": "
          + AGENCY_ID
          + ", \"password\": \""
          + PASSWORD
          + "\", \"age\": \""
          + INVALID_AGE_VALUE
          + "\", "
          + "\"termsAccepted\": \""
          + TERMS_ACCEPTED
          + "\", \"consultingType\": \""
          + CONSULTING_TYPE_ID_U25
          + "\"}";
  public static final String INVALID_U25_USER_REQUEST_BODY_STATE =
      "{\"username\": \""
          + USERNAME
          + "\","
          + "\"agencyId\": "
          + AGENCY_ID
          + ", \"agencyId\": "
          + AGENCY_ID
          + ", \"password\": \""
          + PASSWORD
          + "\", \"age\": \""
          + AGE_VALUE
          + "\", "
          + "\"state\": \""
          + STATE_VALUE
          + "\", \"termsAccepted\": \""
          + TERMS_ACCEPTED
          + "\", \"consultingType\": \""
          + CONSULTING_TYPE_ID_U25
          + "\"}";
  public static final String VALID_U25_USER_REQUEST_BODY =
      "{\"username\": \""
          + USERNAME
          + "\", \"postcode\": \""
          + POSTCODE
          + "\", \"agencyId\": "
          + AGENCY_ID
          + ", \"agencyId\": "
          + AGENCY_ID
          + ", \"password\": \""
          + PASSWORD
          + "\", "
          + "\"age\": \""
          + AGE_VALUE
          + "\", \"state\": \""
          + STATE_VALUE
          + "\", \"termsAccepted\": \""
          + TERMS_ACCEPTED
          + "\", \"consultingType\": \""
          + CONSULTING_TYPE_ID_U25
          + "\"}";
  public static final String USER_REQUEST_BODY_WITH_USERNAME_TOO_SHORT =
      "{\"username\": \""
          + USERNAME_TOO_SHORT
          + "\","
          + "\"postcode\": \""
          + POSTCODE
          + "\", \"agencyId\": "
          + AGENCY_ID
          + ", \"password\": \""
          + PASSWORD
          + "\","
          + "\"email\": \""
          + EMAIL
          + "\", \"addictiveDrugs\": \""
          + ADDICTIVE_DRUGS_VALUE
          + "\","
          + "\"relation\": \""
          + RELATION_VALUE
          + "\", \"age\": \""
          + AGE_VALUE
          + "\", \"gender\": \""
          + GENDER_VALUE
          + "\","
          + "\"termsAccepted\": \""
          + TERMS_ACCEPTED
          + "\", \"consultingType\": \""
          + CONSULTING_TYPE_ID_SUCHT
          + "\"}";
  public static final String USER_REQUEST_BODY_WITH_USERNAME_TOO_LONG =
      "{\"username\": \""
          + USERNAME_TOO_LONG
          + "\","
          + "\"postcode\": \""
          + POSTCODE
          + "\", \"agencyId\": "
          + AGENCY_ID
          + ", \"password\": \""
          + PASSWORD
          + "\","
          + "\"email\": \""
          + EMAIL
          + "\", \"addictiveDrugs\": \""
          + ADDICTIVE_DRUGS_VALUE
          + "\","
          + "\"relation\": \""
          + RELATION_VALUE
          + "\", \"age\": \""
          + AGE_VALUE
          + "\", \"gender\": \""
          + GENDER_VALUE
          + "\","
          + "\"termsAccepted\": \""
          + TERMS_ACCEPTED
          + "\", \"consultingType\": \""
          + CONSULTING_TYPE_ID_SUCHT
          + "\"}";
  public static final String VALID_CREATE_CHAT_V1_BODY =
      "{ \"topic\": \""
          + CHAT_TOPIC
          + "\", \"startDate\": \""
          + CHAT_START_DATE
          + "\", "
          + "\"startTime\": \""
          + CHAT_START_TIME
          + "\", \"duration\": \""
          + CHAT_DURATION
          + "\", \"repetitive\": "
          + CHAT_REPETITIVE
          + " }";
  public static final String VALID_CREATE_CHAT_BODY_WITH_AGENCY_PLACEHOLDER =
      "{ \"topic\": \""
          + CHAT_TOPIC
          + "\", \"startDate\": \""
          + CHAT_START_DATE
          + "\", "
          + "\"startTime\": \""
          + CHAT_START_TIME
          + "\", \"duration\": \""
          + CHAT_DURATION
          + "\", \"repetitive\": "
          + CHAT_REPETITIVE
          + ", \"agencyId\": "
          + "${AGENCY_ID}"
          + " }";
  public static final String VALID_UPDATE_CHAT_BODY =
      "{ \"topic\": \""
          + CHAT_TOPIC
          + "\","
          + "  \"startDate\": \""
          + CHAT_START_DATE
          + "\", \"startTime\": \""
          + CHAT_START_TIME
          + "\","
          + "  \"duration\": "
          + CHAT_DURATION
          + ", \"repetitive\": "
          + CHAT_REPETITIVE
          + " }";
  public static final String INVALID_NEW_REGISTRATION_BODY_WITHOUT_POSTCODE =
      "{\"agencyId\": \"" + AGENCY_ID + "\", \"consultingType\": " + CONSULTING_TYPE_ID_U25 + "}";
  public static final String INVALID_NEW_REGISTRATION_BODY_WITH_INVALID_POSTCODE =
      "{\"postcode\": \""
          + INVALID_POSTCODE
          + "\",\"agencyId\": \""
          + AGENCY_ID
          + "\", \"consultingType\": "
          + CONSULTING_TYPE_ID_U25
          + "}";
  public static final String INVALID_NEW_REGISTRATION_BODY_WITHOUT_AGENCY_ID =
      "{\"postcode\": \"" + POSTCODE + "\", \"consultingType\": " + CONSULTING_TYPE_ID_U25 + "}";
  public static final String INVALID_NEW_REGISTRATION_BODY_WITHOUT_CONSULTING_TYPE =
      "{\"postcode\": \"" + POSTCODE + "\", \"agencyId\": " + AGENCY_ID + "}";
  public static final String VALID_NEW_REGISTRATION_BODY =
      "{\"postcode\": \""
          + POSTCODE
          + "\", \"agencyId\": "
          + AGENCY_ID
          + ", \"consultingType\": \""
          + CONSULTING_TYPE_ID_U25
          + "\"}";
  public static final String ACTIVATE_2FA_BODY =
      "{\"otp\": \"" + OTP + "\", \"secret\":" + "\"" + SECRET + "\"}";
}
