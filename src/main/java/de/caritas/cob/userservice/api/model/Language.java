package de.caritas.cob.userservice.api.model;

import com.neovisionaries.i18n.LanguageCode;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table
@Getter
@Setter
@ToString
@Entity
@IdClass(LanguageId.class)
@NoArgsConstructor
@AllArgsConstructor
public class Language {

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      columnDefinition = "varchar(36)",
      name = "consultant_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "language_code_consultant_constraint"))
  @ToString.Exclude
  private Consultant consultant;

  @Id
  @Enumerated(EnumType.STRING)
  @Column(length = 2)
  private LanguageCode languageCode;
}
