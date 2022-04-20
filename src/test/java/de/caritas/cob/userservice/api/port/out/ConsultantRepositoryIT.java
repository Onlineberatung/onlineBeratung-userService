package de.caritas.cob.userservice.api.port.out;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.model.Appointment;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Language;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import org.apache.commons.lang3.RandomStringUtils;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
public class ConsultantRepositoryIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  private Consultant consultant;
  private Consultant originalConsultant;

  private List<String> matchingIds = new ArrayList<>();

  private List<String> nonMatchingIds = new ArrayList<>();

  @Autowired
  private ConsultantRepository underTest;

  @Autowired
  private AppointmentRepository appointmentRepository;

  @BeforeEach
  public void backup() {
    originalConsultant = underTest.findAll().iterator().next();
  }

  @AfterEach
  public void restoreAndReset() {
    underTest.save(originalConsultant);
    consultant = null;
    originalConsultant = null;
    matchingIds.forEach(id -> underTest.deleteById(id));
    matchingIds = new ArrayList<>();
    nonMatchingIds.forEach(id -> underTest.deleteById(id));
    nonMatchingIds = new ArrayList<>();
  }

  @Test
  public void deleteShouldDeleteConsultantAndAppointment() {
    givenACreatedConsultantWithAnAppointment();

    assertTrue(underTest.existsById(consultant.getId()));
    var appointment = consultant.getAppointments().iterator().next();
    assertTrue(appointmentRepository.existsById(appointment.getId()));
    var countConsultants = underTest.count();

    underTest.delete(consultant);

    assertEquals(countConsultants - 1, underTest.count());
    assertFalse(underTest.existsById(consultant.getId()));
    assertFalse(appointmentRepository.existsById(appointment.getId()));
  }

  @Test
  public void saveShouldSaveConsultantWithDefaultLanguage() {
    givenAnExistingConsultantSpeaking();

    var languages = consultant.getLanguages();
    assertNotNull(languages);
    assertEquals(1, languages.size());
    assertEquals(LanguageCode.de, languages.iterator().next().getLanguageCode());
  }

  @Test
  public void saveShouldSaveConsultantWithOneLanguage() {
    givenAnExistingConsultantSpeaking(LanguageCode.en);

    var optionalConsultant = underTest.findById(consultant.getId());

    assertTrue(optionalConsultant.isPresent());
    var languages = optionalConsultant.get().getLanguages();
    assertNotNull(languages);
    assertEquals(1, languages.size());
    assertEquals(LanguageCode.en, languages.iterator().next().getLanguageCode());
  }

  @Test
  public void saveShouldSaveConsultantWithMultipleLanguages() {
    givenAnExistingConsultantSpeaking(LanguageCode.en, LanguageCode.tr);

    var optionalConsultant = underTest.findById(consultant.getId());

    assertTrue(optionalConsultant.isPresent());
    var languages = optionalConsultant.get().getLanguages();
    assertNotNull(languages);
    assertEquals(2, languages.size());

    var expectedLanguages = Set.of(LanguageCode.en, LanguageCode.tr);
    var areLanguagesIn = languages.stream()
        .map(Language::getLanguageCode)
        .allMatch(expectedLanguages::contains);
    assertTrue(areLanguagesIn);
  }

  @Test
  public void saveShouldSaveLessLanguages() {
    givenAnExistingConsultantSpeaking(LanguageCode.en, LanguageCode.tr);

    var optionalConsultant = underTest.findById(consultant.getId());
    assertTrue(optionalConsultant.isPresent());
    var consultantToChange = optionalConsultant.get();
    assertEquals(2, consultantToChange.getLanguages().size());

    consultantToChange.getLanguages().remove(consultantToChange.getLanguages().iterator().next());
    underTest.save(consultantToChange);

    var consultantAfter = underTest.findById(consultant.getId());
    assertTrue(consultantAfter.isPresent());
    assertEquals(1, consultantAfter.get().getLanguages().size());
  }

  @Test
  public void saveShouldEmptyLanguagesToDefault() {
    givenAnExistingConsultantSpeaking(LanguageCode.en, LanguageCode.tr);

    var optionalConsultant = underTest.findById(consultant.getId());
    assertTrue(optionalConsultant.isPresent());
    var consultantToChange = optionalConsultant.get();
    assertEquals(2, consultantToChange.getLanguages().size());

    consultantToChange.getLanguages().clear();
    underTest.save(consultantToChange);

    var consultantAfter = underTest.findById(consultant.getId());
    assertTrue(consultantAfter.isPresent());
    var languages = consultantAfter.get().getLanguages();
    assertEquals(1, languages.size());
    assertEquals(LanguageCode.de, languages.iterator().next().getLanguageCode());
  }

  @Test
  public void saveShouldNotChangeTheDefaultBehaviourOnManualSettingDefault() {
    givenAnExistingConsultantSpeaking();

    var optionalConsultant = underTest.findById(consultant.getId());
    assertTrue(optionalConsultant.isPresent());
    var consultantToChange = optionalConsultant.get();
    assertEquals(1, consultantToChange.getLanguages().size());

    var german = new Language();
    german.setConsultant(consultant);
    german.setLanguageCode(LanguageCode.de);
    var languages = new HashSet<Language>();
    languages.add(german);
    consultantToChange.setLanguages(languages);
    underTest.save(consultantToChange);

    var consultantAfter = underTest.findById(consultant.getId());
    assertTrue(consultantAfter.isPresent());
    var languagesFound = consultantAfter.get().getLanguages();
    assertEquals(1, languagesFound.size());
    assertEquals(LanguageCode.de, languagesFound.iterator().next().getLanguageCode());

    consultantToChange.getLanguages().clear();
    underTest.save(consultantToChange);

    var consultantLater = underTest.findById(consultant.getId());
    assertTrue(consultantLater.isPresent());
    var languagesThen = consultantAfter.get().getLanguages();
    assertEquals(1, languagesThen.size());
    assertEquals(LanguageCode.de, languagesThen.iterator().next().getLanguageCode());
  }

  @Test
  void findAllByInfixShouldFindConsultantWithMatchingInfixes() {
    var infix = RandomStringUtils.randomAlphanumeric(4);
    var firstNameMatching = easyRandom.nextInt(20) + 5;
    givenConsultantsMatchingFirstName(firstNameMatching, infix);
    var lastNameMatching = easyRandom.nextInt(20) + 5;
    givenConsultantsMatchingLastName(lastNameMatching, infix);
    var emailMatching = easyRandom.nextInt(20) + 5;
    givenConsultantsMatchingEmail(emailMatching, infix);
    var notMatching = easyRandom.nextInt(20) + 5;
    givenConsultantsNotMatching(notMatching, infix);

    var foundConsultants = underTest.findAllByInfix(infix, Pageable.unpaged());

    int allMatching = firstNameMatching + lastNameMatching + emailMatching;
    assertEquals(allMatching, foundConsultants.size());
    assertEquals(allMatching, matchingIds.size());
    foundConsultants.forEach(consultant ->
        assertTrue(matchingIds.contains(consultant.getId()))
    );
  }

  @Test
  void findAllByInfixShouldReturnEmptyResultIfNoneMatching() {
    var infix = RandomStringUtils.randomAlphanumeric(4);
    var notMatching = easyRandom.nextInt(20) + 5;
    givenConsultantsNotMatching(notMatching, infix);

    var foundConsultants = underTest.findAllByInfix(infix, Pageable.unpaged());

    assertEquals(0, foundConsultants.size());
    assertEquals(0, matchingIds.size());
  }

  @Test
  void findAllByInfixShouldBeLimitedIfLimitGiven() {
    var infix = RandomStringUtils.randomAlphanumeric(16);
    var limit = easyRandom.nextInt(100);
    givenConsultantsMatchingEmail(limit + 1, infix);

    var pageRequest = PageRequest.of(0, limit);
    var foundConsultants = underTest.findAllByInfix(infix, pageRequest);

    assertEquals(limit, foundConsultants.size());
    assertEquals(limit + 1, matchingIds.size());
    foundConsultants.forEach(consultant ->
        assertTrue(matchingIds.contains(consultant.getId()))
    );
  }

  @Test
  void findAllByInfixShouldBeSortedByLastNameDescIfSortGiven() {
    var infix = RandomStringUtils.randomAlphanumeric(16);
    var limit = easyRandom.nextInt(100) + 1;
    givenConsultantsMatchingEmail(limit, infix);

    var sort = Sort.by("lastName").descending();
    var pageRequest = PageRequest.of(0, limit, sort);
    var foundConsultants = underTest.findAllByInfix(infix, pageRequest);

    assertEquals(limit, foundConsultants.size());
    assertEquals(limit, matchingIds.size());
    var previousLastName = foundConsultants.get(0).getLastName();
    for (var foundConsultant : foundConsultants) {
      assertTrue(matchingIds.contains(foundConsultant.getId()));
      assertTrue(previousLastName.compareTo(foundConsultant.getLastName()) >= 0);
      previousLastName = foundConsultant.getLastName();
    }
  }

  @Test
  void findAllByInfixShouldBeSortedByFirstNameAscIfSortGiven() {
    var infix = RandomStringUtils.randomAlphanumeric(16);
    var limit = easyRandom.nextInt(100);
    givenConsultantsMatchingEmail(limit, infix);

    var sort = Sort.by("firstName").ascending();
    var pageRequest = PageRequest.of(0, limit, sort);
    var foundConsultants = underTest.findAllByInfix(infix, pageRequest);

    assertEquals(limit, foundConsultants.size());
    assertEquals(limit, matchingIds.size());
    var previousFirstName = foundConsultants.get(0).getFirstName();
    for (var foundConsultant : foundConsultants) {
      assertTrue(matchingIds.contains(foundConsultant.getId()));
      assertTrue(previousFirstName.compareTo(foundConsultant.getFirstName()) <= 0);
      previousFirstName = foundConsultant.getFirstName();
    }
  }

  @Test
  void findAllByInfixShouldSearchCaseInsensitive() {
    var infix = RandomStringUtils.randomAlphanumeric(4);
    var transformedInfix = easyRandom.nextBoolean() ? infix.toLowerCase() : infix.toUpperCase();
    var firstNameMatching = easyRandom.nextInt(20) + 5;
    givenConsultantsMatchingFirstName(firstNameMatching, transformedInfix);
    var lastNameMatching = easyRandom.nextInt(20) + 5;
    givenConsultantsMatchingLastName(lastNameMatching, transformedInfix);
    var emailMatching = easyRandom.nextInt(20) + 5;
    givenConsultantsMatchingEmail(emailMatching, transformedInfix);
    var notMatching = easyRandom.nextInt(20) + 5;
    givenConsultantsNotMatching(notMatching, transformedInfix);

    var foundConsultants = underTest.findAllByInfix(infix, Pageable.unpaged());

    int allMatching = firstNameMatching + lastNameMatching + emailMatching;
    assertEquals(allMatching, foundConsultants.size());
    assertEquals(allMatching, matchingIds.size());
    foundConsultants.forEach(consultant ->
        assertTrue(matchingIds.contains(consultant.getId()))
    );
  }

  private void givenConsultantsMatchingFirstName(@PositiveOrZero int count,
      @NotBlank String infix) {
    while (count-- > 0) {
      var dbConsultant = underTest.findAll().iterator().next();
      var consultant = new Consultant();
      BeanUtils.copyProperties(dbConsultant, consultant);
      consultant.setId(UUID.randomUUID().toString());
      consultant.setUsername(RandomStringUtils.randomAlphabetic(8));
      consultant.setRocketChatId(RandomStringUtils.randomAlphabetic(8));
      consultant.setFirstName(aStringWithInfix(infix));
      consultant.setLastName(aStringWithoutInfix(infix));
      consultant.setEmail(aValidEmailWithoutInfix(infix));

      underTest.save(consultant);
      matchingIds.add(consultant.getId());
    }
  }

  private void givenConsultantsMatchingLastName(@PositiveOrZero int count, @NotBlank String infix) {
    while (count-- > 0) {
      var dbConsultant = underTest.findAll().iterator().next();
      var consultant = new Consultant();
      BeanUtils.copyProperties(dbConsultant, consultant);
      consultant.setId(UUID.randomUUID().toString());
      consultant.setUsername(RandomStringUtils.randomAlphabetic(8));
      consultant.setRocketChatId(RandomStringUtils.randomAlphabetic(8));
      consultant.setFirstName(aStringWithoutInfix(infix));
      consultant.setLastName(aStringWithInfix(infix));
      consultant.setEmail(aValidEmailWithoutInfix(infix));

      underTest.save(consultant);
      matchingIds.add(consultant.getId());
    }
  }

  private void givenConsultantsMatchingEmail(@PositiveOrZero int count, @NotBlank String infix) {
    while (count-- > 0) {
      var dbConsultant = underTest.findAll().iterator().next();
      var consultant = new Consultant();
      BeanUtils.copyProperties(dbConsultant, consultant);
      consultant.setId(UUID.randomUUID().toString());
      consultant.setUsername(RandomStringUtils.randomAlphabetic(8));
      consultant.setRocketChatId(RandomStringUtils.randomAlphabetic(8));
      consultant.setFirstName(aStringWithoutInfix(infix));
      consultant.setLastName(aStringWithoutInfix(infix));
      consultant.setEmail(aValidEmailWithInfix(infix));

      underTest.save(consultant);
      matchingIds.add(consultant.getId());
    }
  }

  private void givenConsultantsNotMatching(@PositiveOrZero int count, @NotBlank String infix) {
    while (count-- > 0) {
      var dbConsultant = underTest.findAll().iterator().next();
      var consultant = new Consultant();
      BeanUtils.copyProperties(dbConsultant, consultant);
      consultant.setId(UUID.randomUUID().toString());
      consultant.setUsername(RandomStringUtils.randomAlphabetic(8));
      consultant.setRocketChatId(RandomStringUtils.randomAlphabetic(8));
      consultant.setFirstName(aStringWithoutInfix(infix));
      consultant.setLastName(aStringWithoutInfix(infix));
      consultant.setEmail(aValidEmailWithoutInfix(infix));

      underTest.save(consultant);
      nonMatchingIds.add(consultant.getId());
    }
  }

  private void givenACreatedConsultantWithAnAppointment() {
    var dbConsultant = underTest.findAll().iterator().next();
    consultant = new Consultant();
    BeanUtils.copyProperties(dbConsultant, consultant);
    consultant.setId(UUID.randomUUID().toString());
    consultant.setUsername(RandomStringUtils.randomAlphabetic(8));
    consultant.setEmail(aValidEmail());
    consultant.setRocketChatId(RandomStringUtils.randomAlphabetic(8));
    underTest.save(consultant);

    var appointment = easyRandom.nextObject(Appointment.class);
    appointment.setConsultant(consultant);
    appointment.setId(null);
    var desc = appointment.getDescription();
    if (desc.length() > 300) {
      appointment.setDescription(desc.substring(0, 300));
    }

    consultant.setAppointments(Set.of(appointment));
    underTest.save(consultant);

    consultant = underTest.findById(consultant.getId()).orElseThrow();
  }

  private String aValidEmail() {
    return RandomStringUtils.randomAlphabetic(8)
        + "@"
        + RandomStringUtils.randomAlphabetic(8)
        + "."
        + (easyRandom.nextBoolean() ? "de" : "com");
  }

  private String aValidEmailWithoutInfix(String infix) {
    var email = infix;
    while (email.contains(infix)) {
      email = aValidEmail();
    }

    return email;
  }

  private String aValidEmailWithInfix(String infix) {
    return RandomStringUtils.randomAlphabetic(3)
        + infix
        + RandomStringUtils.randomAlphabetic(3)
        + "@"
        + RandomStringUtils.randomAlphabetic(8)
        + "."
        + (easyRandom.nextBoolean() ? "de" : "com");
  }

  private String aStringWithoutInfix(String infix) {
    var str = infix;
    while (str.contains(infix)) {
      str = RandomStringUtils.randomAlphanumeric(8);
    }

    return str;
  }

  private String aStringWithInfix(String infix) {
    return RandomStringUtils.randomAlphabetic(4)
        + infix
        + RandomStringUtils.randomAlphabetic(4);
  }

  private void givenAnExistingConsultantSpeaking(LanguageCode... languageCodes) {
    consultant = underTest.findAll().iterator().next();

    if (nonNull(languageCodes) && languageCodes.length > 0) {
      var languages = new HashSet<Language>();
      for (var languageCode : languageCodes) {
        var language = new Language(consultant, languageCode);
        languages.add(language);
      }
      consultant.setLanguages(languages);
      underTest.save(consultant);
    }
  }
}

