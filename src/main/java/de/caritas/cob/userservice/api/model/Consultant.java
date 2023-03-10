package de.caritas.cob.userservice.api.model;

import static de.caritas.cob.userservice.api.model.Consultant.EMAIL_ANALYZER;
import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.neovisionaries.i18n.LanguageCode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.standard.ClassicTokenizerFactory;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Where;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.springframework.lang.Nullable;

/** Represents a consultant */
@Entity
@Table(
    name = "consultant",
    indexes = {
      @Index(
          columnList = "first_name, last_name, email, delete_date",
          name = "idx_first_name_last_name_email_delete_date",
          unique = true),
    })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Indexed
@AnalyzerDef(
    name = EMAIL_ANALYZER,
    tokenizer = @TokenizerDef(factory = ClassicTokenizerFactory.class),
    filters = {
      @TokenFilterDef(factory = LowerCaseFilterFactory.class),
    })
@FilterDef(
    name = "tenantFilter",
    parameters = {@ParamDef(name = "tenantId", type = "long")})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Consultant implements TenantAware, NotificationsAware {

  protected static final String EMAIL_ANALYZER = "emailAnalyzer";

  @Id
  @Column(name = "consultant_id", updatable = false, nullable = false)
  @Size(max = 36)
  @NonNull
  private String id;

  @Column(name = "rc_user_id", updatable = false)
  @Size(max = 17)
  @NonNull
  private String rocketChatId;

  @Column(name = "username", updatable = false, nullable = false)
  @Size(max = 255)
  @NonNull
  @Field
  @SortableField
  private String username;

  @Column(name = "first_name", nullable = false)
  @Size(max = 255)
  @NonNull
  @Field
  @SortableField
  private String firstName;

  @Column(name = "last_name", nullable = false)
  @Size(max = 255)
  @NonNull
  @Field
  @SortableField
  private String lastName;

  @Column(name = "email", nullable = false)
  @Size(max = 255)
  @NonNull
  @Field
  @Analyzer(definition = EMAIL_ANALYZER)
  @SortableField
  private String email;

  @Column(name = "is_absent", nullable = false, columnDefinition = "tinyint")
  @Field
  private boolean absent;

  @Column(name = "is_team_consultant", nullable = false, columnDefinition = "tinyint")
  private boolean teamConsultant;

  @Column(name = "absence_message")
  @Lob
  private String absenceMessage;

  @Column(name = "language_formal", nullable = false, columnDefinition = "tinyint")
  private boolean languageFormal;

  @OneToMany(mappedBy = "consultant", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Language> languages;

  @Column(name = "id_old", updatable = false)
  @Nullable
  private Long idOld;

  @OneToMany(mappedBy = "consultant")
  private Set<Session> sessions;

  @OneToMany(mappedBy = "consultant")
  @IndexedEmbedded
  @Where(clause = "delete_date IS NULL")
  private Set<ConsultantAgency> consultantAgencies;

  @OneToMany(mappedBy = "consultant")
  private Set<ConsultantMobileToken> consultantMobileTokens;

  @Column(name = "create_date")
  private LocalDateTime createDate;

  @Column(name = "update_date")
  private LocalDateTime updateDate;

  @Column(name = "delete_date")
  private LocalDateTime deleteDate;

  @Column(name = "encourage_2fa", nullable = false, columnDefinition = "bit default true")
  private Boolean encourage2fa;

  @Column(
      name = "notify_enquiries_repeating",
      nullable = false,
      columnDefinition = "bit default true")
  private Boolean notifyEnquiriesRepeating;

  @Column(
      name = "notify_new_chat_message_from_advice_seeker",
      nullable = false,
      columnDefinition = "bit default true")
  private Boolean notifyNewChatMessageFromAdviceSeeker;

  @Column(
      name = "notify_new_feedback_message_from_advice_seeker",
      nullable = false,
      columnDefinition = "bit default true")
  private Boolean notifyNewFeedbackMessageFromAdviceSeeker;

  @Column(name = "tenant_id")
  @Field
  private Long tenantId;

  @OneToMany(mappedBy = "consultant", cascade = CascadeType.ALL)
  private Set<Appointment> appointments;

  @Column(name = "status", length = 11)
  @Enumerated(EnumType.STRING)
  @Field
  private ConsultantStatus status = ConsultantStatus.IN_PROGRESS;

  @Column(name = "walk_through_enabled", columnDefinition = "tinyint", nullable = false)
  private Boolean walkThroughEnabled;

  @Enumerated(EnumType.STRING)
  @Column(length = 2, nullable = false, columnDefinition = "varchar(2) default 'de'")
  private LanguageCode languageCode;

  @Column(name = "terms_and_conditions_confirmation", columnDefinition = "datetime")
  private LocalDateTime termsAndConditionsConfirmation;

  @Column(name = "data_privacy_confirmation", columnDefinition = "datetime")
  private LocalDateTime dataPrivacyConfirmation;

  @Column(name = "notifications_enabled", columnDefinition = "tinyint", nullable = false)
  private boolean notificationsEnabled;

  @Column(name = "notifications_settings")
  private String notificationsSettings;

  @JsonIgnore
  public String getFullName() {
    return (this.firstName + " " + this.lastName).trim();
  }

  @JsonIgnore
  public void setLanguages(Set<Language> languages) {
    if (isNull(this.languages)) {
      this.languages = languages;
    } else {
      this.languages.clear();
      if (!isNull(languages)) {
        this.languages.addAll(languages);
      }
    }
  }

  @JsonIgnore
  public Set<Language> getLanguages() {
    if (isNull(languages) || languages.isEmpty()) {
      var defaultLanguage = new Language();
      defaultLanguage.setConsultant(this);
      defaultLanguage.setLanguageCode(LanguageCode.de);

      var set = new HashSet<Language>();
      set.add(defaultLanguage);

      return set;
    } else {

      return languages;
    }
  }

  @JsonIgnore
  public boolean isInAgency(long agencyId) {
    if (isNull(consultantAgencies)) {
      return false;
    }

    return consultantAgencies.stream()
        .map(ConsultantAgency::getAgencyId)
        .collect(Collectors.toSet())
        .contains(agencyId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Consultant)) {
      return false;
    }
    Consultant that = (Consultant) o;
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Consultant [id="
        + id
        + ", rocketChatId="
        + rocketChatId
        + ", username="
        + username
        + "]";
  }

  public interface ConsultantBase {

    String getId();

    String getFirstName();

    String getLastName();

    String getEmail();
  }
}
