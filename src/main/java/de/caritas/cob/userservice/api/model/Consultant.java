package de.caritas.cob.userservice.api.model;

import static de.caritas.cob.userservice.api.model.Consultant.EMAIL_ANALYZER;
import static java.util.Objects.isNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.neovisionaries.i18n.LanguageCode;
import de.caritas.cob.userservice.api.repository.TenantAware;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
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
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.springframework.lang.Nullable;

/**
 * Represents a consultant
 */
@Entity
@Table(name = "consultant")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Indexed
@AnalyzerDef(name = EMAIL_ANALYZER,
    tokenizer = @TokenizerDef(factory = ClassicTokenizerFactory.class),
    filters = {
        @TokenFilterDef(factory = LowerCaseFilterFactory.class),
    })
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "tenantId", type = "long")})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Consultant implements TenantAware {

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
  private String username;

  @Column(name = "first_name", nullable = false)
  @Size(max = 255)
  @NonNull
  private String firstName;

  @Column(name = "last_name", nullable = false)
  @Size(max = 255)
  @NonNull
  @Field
  private String lastName;

  @Column(name = "email", nullable = false)
  @Size(max = 255)
  @NonNull
  @Field
  @Analyzer(definition = EMAIL_ANALYZER)
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

  @Column(name = "tenant_id")
  @Field
  private Long tenantId;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private ConsultantStatus status = ConsultantStatus.IN_PROGRESS;

  @Column(name = "walk_through_enabled", columnDefinition = "tinyint")
  private Boolean walkThroughEnabled;

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
    return "Consultant [id=" + id + ", rocketChatId=" + rocketChatId + ", username=" + username
        + "]";
  }
}
