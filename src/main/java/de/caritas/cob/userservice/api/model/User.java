package de.caritas.cob.userservice.api.model;

import com.neovisionaries.i18n.LanguageCode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Represents a user
 */
@Entity
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EntityListeners(AuditingEntityListener.class)
@FilterDef(name = "tenantFilter", parameters = {@ParamDef(name = "tenantId", type = "long")})
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class User implements TenantAware {

  @Id
  @Column(name = "user_id", updatable = false, nullable = false)
  @Size(max = 36)
  @NonNull
  private String userId;

  @Column(name = "id_old", updatable = false)
  private Long oldId;

  @Column(name = "username", updatable = false, nullable = false)
  @Size(max = 255)
  @NonNull
  private String username;

  @Column(name = "email", nullable = false)
  @Size(max = 255)
  @NonNull
  private String email;

  @Column(name = "rc_user_id")
  private String rcUserId;

  @Column(name = "language_formal", nullable = false, columnDefinition = "tinyint")
  private boolean languageFormal;

  @OneToMany(mappedBy = "user")
  @Exclude
  private Set<Session> sessions;

  @OneToMany(mappedBy = "user")
  @Exclude
  private Set<UserAgency> userAgencies;

  @Column(name = "mobile_token")
  @Lob
  private String mobileToken;

  @OneToMany(mappedBy = "user")
  @Exclude
  private Set<UserMobileToken> userMobileTokens;

  @Column(name = "delete_date", columnDefinition = "datetime")
  private LocalDateTime deleteDate;

  @Column(name = "tenant_id")
  private Long tenantId;

  @CreatedDate
  @Column(name = "create_date", columnDefinition = "datetime")
  private LocalDateTime createDate;

  @LastModifiedDate
  @Column(name = "update_date", columnDefinition = "datetime")
  private LocalDateTime updateDate;

  @Column(name = "encourage_2fa", nullable = false, columnDefinition = "bit default true")
  private Boolean encourage2fa;

  @Enumerated(EnumType.STRING)
  @Column(length = 2, nullable = false, columnDefinition = "varchar(2) default 'de'")
  private LanguageCode languageCode;

  public User(@Size(max = 36) @NonNull String userId, Long oldId,
      @Size(max = 255) @NonNull String username, @Size(max = 255) @NonNull String email,
      boolean languageFormal) {
    this.userId = userId;
    this.oldId = oldId;
    this.username = username;
    this.email = email;
    this.languageFormal = languageFormal;
    setEncourage2fa(true);
    setLanguageCode(LanguageCode.de);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof User)) {
      return false;
    }
    User user = (User) o;
    return userId.equals(user.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId);
  }
}
