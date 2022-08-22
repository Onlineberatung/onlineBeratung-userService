package de.caritas.cob.userservice.api.admin.service.consultant.update;

import com.google.api.client.util.Sets;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Language;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import java.util.Set;
import org.jeasy.random.EasyRandom;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@TestPropertySource(properties = "multitenancy.enabled=true")
@Transactional
public class ConsultantUpdateServiceTenantAwareIT extends ConsultantUpdateServiceBase {

  protected String VALID_CONSULTANT_ID = "0b3b1cc6-be98-4787-aa56-212259d811b8";

  private final EasyRandom easyRandom = new EasyRandom();

  @Autowired ConsultantRepository consultantRepository;

  @Autowired ConsultantAgencyRepository consultantAgencyRepository;
  private Set<String> consultantsToRemove = Sets.newHashSet();

  @Before
  public void beforeTests() {
    TenantContext.setCurrentTenant(1L);
  }

  @After
  public void afterTests() {
    consultantsToRemove.stream().forEach(id -> consultantRepository.deleteById(id));
    TenantContext.clear();
  }

  @Test
  public void updateConsultant_Should_returnUpdatedPersistedConsultant_When_inputDataIsValid() {
    givenAValidConsultantPersisted(VALID_CONSULTANT_ID);
    super.updateConsultant_Should_returnUpdatedPersistedConsultant_When_inputDataIsValid();
  }

  @Test
  public void updateConsultant_Should_throwCustomResponseException_When_absenceIsInvalid() {
    givenAValidConsultantPersisted(VALID_CONSULTANT_ID);
    super.updateConsultant_Should_throwCustomResponseException_When_absenceIsInvalid();
  }

  @Test
  public void updateConsultant_Should_throwCustomResponseException_When_newEmailIsInvalid() {
    givenAValidConsultantPersisted(VALID_CONSULTANT_ID);
    super.updateConsultant_Should_throwCustomResponseException_When_newEmailIsInvalid();
  }

  protected String getValidConsultantId() {
    return VALID_CONSULTANT_ID;
  }

  private Consultant givenAValidConsultantPersisted(String id, boolean isTeamConsultant) {
    Consultant consultant = givenAValidConsultant(id, isTeamConsultant);
    consultant.setLanguages(Set.of(new Language(consultant, LanguageCode.getByCode("de"))));
    consultant = consultantRepository.save(consultant);
    assignConsultantToAgency(consultant);
    return consultant;
  }

  private Consultant givenAValidConsultantPersisted(String id) {
    return givenAValidConsultantPersisted(id, false);
  }

  private void assignConsultantToAgency(Consultant consultant) {

    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setAgencyId(1L);
    consultantAgency.setTenantId(1L);
    consultantAgency.setConsultant(consultant);
    consultantAgencyRepository.save(consultantAgency);
  }

  private Consultant givenAValidConsultant(String id, boolean isTeamConsultant) {
    Consultant consultant = new Consultant();
    consultant.setAppointments(null);
    consultant.setTenantId(1L);
    consultant.setId(id);
    consultant.setConsultantAgencies(Sets.newHashSet());
    consultant.setUsername(easyRandom.nextObject(String.class));
    consultant.setFirstName(easyRandom.nextObject(String.class));
    consultant.setLastName(easyRandom.nextObject(String.class));
    consultant.setEmail(easyRandom.nextObject(String.class));
    consultant.setEncourage2fa(true);
    consultant.setNotifyEnquiriesRepeating(true);
    consultant.setNotifyNewChatMessageFromAdviceSeeker(true);
    consultant.setNotifyNewFeedbackMessageFromAdviceSeeker(true);
    consultant.setWalkThroughEnabled(true);
    consultant.setTeamConsultant(isTeamConsultant);
    consultant.setConsultantMobileTokens(Sets.newHashSet());
    consultant.setLanguageCode(LanguageCode.de);

    return consultant;
  }
}
