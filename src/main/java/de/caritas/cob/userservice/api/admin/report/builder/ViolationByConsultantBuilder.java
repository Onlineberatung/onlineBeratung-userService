package de.caritas.cob.userservice.api.admin.report.builder;

import static java.util.Objects.nonNull;

import de.caritas.cob.userservice.api.adapters.web.dto.AdditionalInformationDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO.ViolationTypeEnum;
import de.caritas.cob.userservice.api.model.Consultant;

/** Builder to create a {@link ViolationDTO} based on a {@link Consultant}. */
public class ViolationByConsultantBuilder {

  private final Consultant consultant;
  private String reason;

  private ViolationByConsultantBuilder(Consultant consultant) {
    this.consultant = nonNull(consultant) ? consultant : new Consultant();
  }

  /**
   * Creates the {@link ViolationByConsultantBuilder} instance.
   *
   * @param consultant the required {@link Consultant}
   * @return the {@link ViolationByConsultantBuilder} instance
   */
  public static ViolationByConsultantBuilder getInstance(Consultant consultant) {
    return new ViolationByConsultantBuilder(consultant);
  }

  /**
   * Sets the custom violation reason.
   *
   * @param reason the violation reason
   * @return the current {@link ViolationByConsultantBuilder}
   */
  public ViolationByConsultantBuilder withReason(String reason) {
    this.reason = reason;
    return this;
  }

  /**
   * Creates the {@link ViolationDTO}.
   *
   * @return the generated {@link ViolationDTO}
   */
  public ViolationDTO build() {
    return new ViolationDTO()
        .violationType(ViolationTypeEnum.CONSULTANT)
        .identifier(this.consultant.getId())
        .reason(this.reason)
        .addAdditionalInformationItem(additionalInformation("Username", consultant.getUsername()))
        .addAdditionalInformationItem(additionalInformation("Email", consultant.getEmail()));
  }

  private AdditionalInformationDTO additionalInformation(String key, String value) {
    return new AdditionalInformationDTO().name(key).value(value);
  }
}
