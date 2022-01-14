package de.caritas.cob.userservice.api.repository.consultant;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neovisionaries.i18n.LanguageCode;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultantRepositoryIT {

  private Consultant consultant;
  private Consultant originalConsultant;

  @Autowired
  private ConsultantRepository underTest;

  @BeforeEach
  public void backup() {
    originalConsultant = underTest.findAll().iterator().next();
  }

  @AfterEach
  public void restore() {
    underTest.save(originalConsultant);
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

