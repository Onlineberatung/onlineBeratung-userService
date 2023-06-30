package de.caritas.cob.userservice.api.helper;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.port.out.IdentityClientConfig;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHelper {

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile(
          "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
              + "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$");

  public static final int USERNAME_MIN_LENGTH = 5;
  public static final int USERNAME_MAX_LENGTH = 30;
  public static final String VALID_POSTCODE_REGEX = "^[0-9]{5}$";
  public static final long AGENCY_ID_MIN = 0;
  public static final long AGENCY_ID_MAX = Long.MAX_VALUE;
  public static final String AGE_REGEXP = "[0-9]+|";
  public static final String STATE_REGEXP = "[0-9]|1[0-6]";
  public static final String TERMS_ACCEPTED_REGEXP = "true|TRUE";
  public static final String CONSULTING_TYPE_REGEXP = "[0-9]+|";

  public static final String REFERER_REGEXP = "[a-zA-Z0-9]{1,8}";
  public static final int CHAT_MIN_DURATION = 30;
  public static final int CHAT_MAX_DURATION = 180;
  public static final int CHAT_TOPIC_MIN_LENGTH = 3;
  public static final int CHAT_TOPIC_MAX_LENGTH = 50;

  private final UsernameTranscoder usernameTranscoder;
  private final IdentityClientConfig identityClientConfig;

  /**
   * Generates a random password which complies with the Keycloak policy.
   *
   * @return a random generated password
   */
  public String getRandomPassword() {
    List<CharacterRule> rules =
        Arrays.asList(
            // at least one upper-case character
            new CharacterRule(EnglishCharacterData.UpperCase, 1),
            // at least one lower-case character
            new CharacterRule(EnglishCharacterData.LowerCase, 1),
            // at least one digit character
            new CharacterRule(EnglishCharacterData.Digit, 1),
            // at least one special character
            new CharacterRule(
                new CharacterData() {
                  @Override
                  public String getErrorCode() {
                    return "ERR_SPECIAL";
                  }

                  @Override
                  public String getCharacters() {
                    return "!()$%&";
                  }
                },
                1));
    var generator = new PasswordGenerator();
    // Generated password is 8 characters long, which complies with policy
    return generator.generatePassword(10, rules);
  }

  /**
   * Generates the dummy email for a Keycloak user.
   *
   * @param userId the prefix for the dummy email
   * @return the generated dummy email address
   */
  public String getDummyEmail(String userId) {
    return userId + identityClientConfig.getEmailDummySuffix();
  }

  /**
   * Checks if the given username is between minimum and maximum char length.
   *
   * @param username the username to validate
   * @return true if username is valid
   */
  public boolean isUsernameValid(String username) {
    username = this.usernameTranscoder.decodeUsername(username);
    return username.length() >= USERNAME_MIN_LENGTH && username.length() <= USERNAME_MAX_LENGTH;
  }

  public boolean isValidEmail(String email) {
    return nonNull(email) && EMAIL_PATTERN.matcher(email).matches();
  }

  /**
   * Returns true if the given usernames match.
   *
   * @param firstUsername encoded or decoded first username to compare
   * @param secondUsername encoded or decoded second username to compare
   * @return true if usernames matches
   */
  public boolean doUsernamesMatch(String firstUsername, String secondUsername) {
    return StringUtils.equals(
        this.usernameTranscoder.encodeUsername(firstUsername).toLowerCase(),
        this.usernameTranscoder.encodeUsername(secondUsername).toLowerCase());
  }
}
