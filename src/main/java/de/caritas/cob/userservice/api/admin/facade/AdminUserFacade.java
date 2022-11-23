package de.caritas.cob.userservice.api.admin.facade;

import de.caritas.cob.userservice.api.adapters.web.dto.AdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Facade to encapsulate admin functions for consultants. */
@Service
@RequiredArgsConstructor
public class AdminUserFacade {

  public AdminDTO findAdminById(String userId) {
    // TODO idriss implement
    return new AdminDTO().addAgenciesItem(new AgencyAdminResponseDTO().id(1L));
  }
}
