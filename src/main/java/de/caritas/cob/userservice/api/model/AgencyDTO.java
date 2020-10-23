package de.caritas.cob.userservice.api.model;

import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ApiModel(value = "Agency")
public class AgencyDTO {

  @ApiModelProperty(example = "153918", position = 0)
  private Long id;

  @ApiModelProperty(example = "Alkohol-Beratung", position = 1)
  private String name;

  @ApiModelProperty(example = "53113", position = 2)
  private String postcode;

  @ApiModelProperty(example = "Bonn", position = 3)
  private String city;

  @ApiModelProperty(example = "Our agency provides help for the following topics: Lorem ipsum..",
      position = 4)
  private String description;

  @ApiModelProperty(example = "false", position = 5)
  private boolean teamAgency;

  @ApiModelProperty(example = "false", position = 6)
  private boolean offline;

  @ApiModelProperty(example = "0", position = 7)
  private ConsultingType consultingType;
}
