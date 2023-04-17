package de.caritas.cob.userservice.api.manager.consultingtype;

import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.SessionDataInitializingDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SessionDataInitializing {

  private boolean age;
  private boolean state;

  public static SessionDataInitializing convertSessionDataInitializingDTOtoSessionDataInitializing(
      SessionDataInitializingDTO sessionDataInitializingDTO) {

    return new SessionDataInitializing(
        isTrue(sessionDataInitializingDTO.getAge()),
        isTrue(sessionDataInitializingDTO.getState()));
  }
}
