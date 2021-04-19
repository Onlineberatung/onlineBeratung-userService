package de.caritas.cob.userservice.api.manager.consultingtype;

import static de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManagerTest.countConsultingTypeSettings;
import static de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManagerTest.loadConsultingTypeSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.UserServiceApplication;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ConsultingTypeManagerIT {

  @Autowired
  private ConsultingTypeManager consultingTypeManager;

  @ParameterizedTest
  @MethodSource("generateConsultingIDs")
  void init_Should_InitializeConsultingTypeSettingFromJsonFile(int consultingType)
      throws Exception {
    ConsultingTypeSettings consultingTypeSettings = loadConsultingTypeSettings(consultingType);

    ConsultingTypeSettings result =
        consultingTypeManager.getConsultingTypeSettings(consultingType);

    assertSameValue(result::getConsultingId, consultingTypeSettings::getConsultingId);
    assertSameValue(result::isSendWelcomeMessage, consultingTypeSettings::isSendWelcomeMessage);
    assertSameValue(result::getWelcomeMessage, consultingTypeSettings::getWelcomeMessage);
    assertSameValue(result.getSessionDataInitializing()::isAddictiveDrugs,
        consultingTypeSettings.getSessionDataInitializing()::isAddictiveDrugs);
    assertSameValue(result.getSessionDataInitializing()::isAge,
        consultingTypeSettings.getSessionDataInitializing()::isAge);
    assertSameValue(result.getSessionDataInitializing()::isGender,
        consultingTypeSettings.getSessionDataInitializing()::isGender);
    assertSameValue(result.getSessionDataInitializing()::isRelation,
        consultingTypeSettings.getSessionDataInitializing()::isRelation);
    assertSameValue(result.getSessionDataInitializing()::isState,
        consultingTypeSettings.getSessionDataInitializing()::isState);
    assertSameValue(result::isMonitoring, consultingTypeSettings::isMonitoring);
    assertSameValue(result::isFeedbackChat, consultingTypeSettings::isFeedbackChat);
    assertSameValue(result.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant()::getAllTeamConsultants,
        consultingTypeSettings.getNotifications().getNewMessage().getTeamSession()
            .getToConsultant()::getAllTeamConsultants);
    assertSameValue(result::isLanguageFormal, consultingTypeSettings::isLanguageFormal);
    assertSameValue(result.getRegistration().getMandatoryFields()::isAge,
        consultingTypeSettings.getRegistration().getMandatoryFields()::isAge);
    assertSameValue(result.getRegistration().getMandatoryFields()::isState,
        consultingTypeSettings.getRegistration().getMandatoryFields()::isState);
  }

  private void assertSameValue(Supplier<Object> result, Supplier<Object> expected) {
    assertThat(result.get(), is(expected.get()));
  }

  static Stream<Integer> generateConsultingIDs() {
    return IntStream.range(0, countConsultingTypeSettings()).boxed();
  }

}
