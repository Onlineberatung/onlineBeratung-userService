package de.caritas.cob.userservice.api.admin.service.admin.delete;

import static org.mockito.Mockito.verify;

import de.caritas.cob.userservice.api.port.out.AdminAgencyRepository;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DeleteAdminServiceTest {

  private final String VALID_ADMIN_ID = "164be67d-4d1b-4d80-bb6b-0ee057a1c59e";

  @InjectMocks private DeleteAdminService deleteAdminService;
  @Mock private AdminRepository adminRepository;
  @Mock private AdminAgencyRepository adminAgencyRepository;
  @Mock private IdentityClient identityClient;

  @Test
  public void deleteAgencyAdmin_Should_deleteAdminAndRelation_When_adminIdIsProvided() {
    // given
    // when
    deleteAdminService.deleteAgencyAdmin(VALID_ADMIN_ID);

    // then
    verify(adminAgencyRepository).deleteByAdminId(VALID_ADMIN_ID);
    verify(identityClient).deleteUser(VALID_ADMIN_ID);
    verify(adminRepository).deleteById(VALID_ADMIN_ID);
  }
}
