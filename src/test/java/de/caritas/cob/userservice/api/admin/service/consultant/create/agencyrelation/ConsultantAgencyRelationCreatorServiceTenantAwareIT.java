package de.caritas.cob.userservice.api.admin.service.consultant.create.agencyrelation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.util.Lists;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.facade.RocketChatFacade;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.model.Language;
import de.caritas.cob.userservice.api.model.Session;
import de.caritas.cob.userservice.api.model.Session.SessionStatus;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.model.UserAgency;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserAgencyRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.HashSet;
import java.util.List;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = "multitenancy.enabled=true")
@Transactional(propagation = Propagation.NEVER)
public class ConsultantAgencyRelationCreatorServiceTenantAwareIT {

  private final EasyRandom easyRandom = new EasyRandom();

  @Autowired private ConsultantAgencyRelationCreatorService consultantAgencyRelationCreatorService;

  @Autowired private ConsultantRepository consultantRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private UserAgencyRepository userAgencyRepository;

  @Autowired private ConsultantAgencyRepository consultantAgencyRepository;

  @Autowired private SessionRepository sessionRepository;

  @MockBean private AgencyService agencyService;

  @MockBean private IdentityClient identityClient;

  @MockBean private RocketChatFacade rocketChatFacade;

  @MockBean private ConsultingTypeManager consultingTypeManager;

  @Before
  public void beforeTests() {
    TenantContext.setCurrentTenant(1L);
  }

  @After
  public void afterTests() {
    TenantContext.clear();
  }

  @Test
  public void
      createNewConsultantAgency_Should_addConsultantToEnquiriesRocketChatGroups_When_ParamsAreValidAndMultitenancyEnabled() {

    Consultant consultant = createConsultantWithoutAgencyAndSession();

    CreateConsultantAgencyDTO createConsultantAgencyDTO = new CreateConsultantAgencyDTO();
    createConsultantAgencyDTO.setAgencyId(15L);
    createConsultantAgencyDTO.setRoleSetKey("valid-role-set");

    when(identityClient.userHasRole(eq(consultant.getId()), any())).thenReturn(true);

    AgencyDTO agencyDTO = new AgencyDTO();
    agencyDTO.setId(15L);
    agencyDTO.setTeamAgency(false);
    agencyDTO.setConsultingType(0);
    when(agencyService.getAgencyWithoutCaching(15L)).thenReturn(agencyDTO);

    Session enquirySessionWithoutConsultant =
        createSessionWithoutConsultant(agencyDTO.getId(), SessionStatus.NEW);

    final var consultingTypeResponse =
        easyRandom.nextObject(ExtendedConsultingTypeResponseDTO.class);
    when(consultingTypeManager.getConsultingTypeSettings(0)).thenReturn(consultingTypeResponse);

    this.consultantAgencyRelationCreatorService.createNewConsultantAgency(
        consultant.getId(), createConsultantAgencyDTO);

    verify(rocketChatFacade, timeout(10000))
        .addUserToRocketChatGroup(
            consultant.getRocketChatId(), enquirySessionWithoutConsultant.getGroupId());

    verify(rocketChatFacade, timeout(10000))
        .addUserToRocketChatGroup(
            consultant.getRocketChatId(), enquirySessionWithoutConsultant.getFeedbackGroupId());

    List<ConsultantAgency> result =
        this.consultantAgencyRepository.findByConsultantIdAndDeleteDateIsNull(consultant.getId());

    assertThat(result, notNullValue());
    assertThat(result, hasSize(1));
    assertEquals(1, enquirySessionWithoutConsultant.getTenantId());

    List<ConsultantAgency> agenciesForConsultant =
        this.consultantAgencyRepository.findByConsultantId(consultant.getId());
    assertEquals(1, agenciesForConsultant.get(0).getTenantId());
  }

  private Consultant createConsultantWithoutAgencyAndSession() {
    Consultant consultant = easyRandom.nextObject(Consultant.class);
    consultant.setAppointments(null);
    consultant.setTenantId(1L);
    consultant.setConsultantAgencies(null);
    consultant.setSessions(null);
    consultant.setConsultantMobileTokens(null);
    consultant.setRocketChatId("RocketChatId");
    consultant.setDeleteDate(null);
    Set<Language> language = new HashSet<>();
    Language lang = new Language();
    lang.setLanguageCode(LanguageCode.de);
    lang.setConsultant(consultant);
    language.add(lang);
    consultant.setLanguages(language);
    return this.consultantRepository.save(consultant);
  }

  private Session createSessionWithoutConsultant(Long agencyId, SessionStatus sessionStatus) {
    User user = easyRandom.nextObject(User.class);
    user.setSessions(null);
    user.setUserMobileTokens(null);
    user.setUserAgencies(null);
    this.userRepository.save(user);

    UserAgency userAgency = new UserAgency();
    userAgency.setAgencyId(agencyId);
    userAgency.setUser(user);
    this.userAgencyRepository.save(userAgency);

    Session session = new Session();
    session.setStatus(sessionStatus);
    session.setPostcode("12345");
    session.setId(1L);
    session.setConsultant(null);
    session.setUser(user);
    session.setAgencyId(agencyId);
    session.setTeamSession(true);
    session.setSessionTopics(Lists.newArrayList());
    session.setLanguageCode(LanguageCode.de);
    session.setIsConsultantDirectlySet(false);

    return this.sessionRepository.save(session);
  }
}
