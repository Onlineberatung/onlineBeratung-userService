package de.caritas.cob.userservice.api.manager.consultingtype;

import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.SessionDataInitializingDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SessionDataInitializing {

  private boolean addictiveDrugs;
  private boolean age;
  private boolean gender;
  private boolean relation;
  private boolean state;

  public static SessionDataInitializing convertSessionDataInitializingDTOtoSessionDataInitializing(
      SessionDataInitializingDTO sessionDataInitializingDTO) {

    return new SessionDataInitializing(sessionDataInitializingDTO.getAddictiveDrugs(),
        sessionDataInitializingDTO.getAge(), sessionDataInitializingDTO.getGender(),
        sessionDataInitializingDTO.getRelation(),
        sessionDataInitializingDTO.getState());
  }

}
