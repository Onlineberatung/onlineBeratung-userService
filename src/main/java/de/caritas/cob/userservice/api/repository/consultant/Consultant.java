package de.caritas.cob.userservice.api.repository.consultant;

import static de.caritas.cob.userservice.api.repository.consultant.Consultant.EMAIL_ANALYZER;

import de.caritas.cob.userservice.api.repository.consultantagency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.Session;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
import org.hibernate.annotations.Type;
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
public class Consultant {

  protected static final String EMAIL_ANALYZER = "emailAnalyzer";

  @Id
  @Column(name = "consultant_id", updatable = false, nullable = false)
  @Size(max = 36)
  @NonNull
  private String id;

  @Column(name = "rc_user_id", updatable = false, nullable = false)
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

  @Column(name = "is_absent", nullable = false)
  @Type(type = "org.hibernate.type.NumericBooleanType")
  @Field
  private boolean absent;

  @Column(name = "is_team_consultant", nullable = false)
  @Type(type = "org.hibernate.type.NumericBooleanType")
  private boolean teamConsultant;

  @Column(name = "absence_message", updatable = true, nullable = true)
  private String absenceMessage;

  @Column(name = "language_formal", updatable = true, nullable = false)
  @Type(type = "org.hibernate.type.NumericBooleanType")
  private boolean languageFormal;

  @Column(name = "id_old", updatable = false, nullable = true)
  @Nullable
  private Long idOld;

  @OneToMany(mappedBy = "consultant")
  private Set<Session> sessions;

  @OneToMany(mappedBy = "consultant")
  @IndexedEmbedded
  @Where(clause = "delete_date IS NULL")
  private Set<ConsultantAgency> consultantAgencies;

  @Column(name = "create_date")
  private LocalDateTime createDate;

  @Column(name = "update_date")
  private LocalDateTime updateDate;

  @Column(name = "delete_date")
  private LocalDateTime deleteDate;

  public String getFullName() {
    return (this.firstName + " " + this.lastName).trim();
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
