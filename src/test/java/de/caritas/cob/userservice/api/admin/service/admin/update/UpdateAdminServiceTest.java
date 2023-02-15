package de.caritas.cob.userservice.api.admin.service.admin.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UserDTO;
import de.caritas.cob.userservice.api.admin.service.admin.search.RetrieveAdminService;
import de.caritas.cob.userservice.api.admin.service.consultant.validation.UserAccountInputValidator;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.Admin.AdminType;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.port.out.IdentityClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateAdminServiceTest {

  @Mock private IdentityClient identityClient;
  @Mock private UserAccountInputValidator userAccountInputValidator;
  @Mock private AdminRepository adminRepository;
  @Mock private RetrieveAdminService retrieveAdminService;
  @Captor private ArgumentCaptor<UserDTO> userDTOCaptor;

  private UpdateAdminService updateAdminService;

  @BeforeEach
  void setUp() {
    updateAdminService =
        new UpdateAdminService(
            identityClient, userAccountInputValidator, adminRepository, retrieveAdminService);
  }

  @Test
  void updateAgencyAdmin_Should_notUpdateAdmin_When_adminEntityHasTenantIdEqualZero() {
    // given
    Admin admin = mock(Admin.class);
    when(admin.getTenantId()).thenReturn(0L);
    when(retrieveAdminService.findAdmin(anyString(), eq(AdminType.AGENCY))).thenReturn(admin);

    // when, then
    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> updateAdminService.updateAgencyAdmin("adminId", null));

    assertEquals("Admin has tenant id 0", exception.getMessage());
    verify(identityClient, never()).updateUserData(any(), any(), any(), any());
    verify(adminRepository, never()).save(any());
  }

  @Test
  void updateAgencyAdmin_Should_updateAdmin_When_adminEntityHasTenantIdNull() {
    // given
    UpdateAgencyAdminDTO updateAgencyAdminDTO = mock(UpdateAgencyAdminDTO.class);
    Admin admin = mock(Admin.class);
    when(admin.getTenantId()).thenReturn(null);
    when(retrieveAdminService.findAdmin(anyString(), eq(AdminType.AGENCY))).thenReturn(admin);

    // when
    updateAdminService.updateAgencyAdmin("adminId", updateAgencyAdminDTO);

    // then
    verify(identityClient).updateUserData(any(), userDTOCaptor.capture(), any(), any());
    assertNull(userDTOCaptor.getValue().getTenantId());
    verify(adminRepository).save(admin);
  }

  @Test
  void updateAgencyAdmin_Should_updateAdmin_When_adminEntityHasTenantDifferentFromZero() {
    // given
    UpdateAgencyAdminDTO updateAgencyAdminDTO = mock(UpdateAgencyAdminDTO.class);
    Admin admin = mock(Admin.class);
    when(admin.getTenantId()).thenReturn(2L);
    when(retrieveAdminService.findAdmin(anyString(), eq(AdminType.AGENCY))).thenReturn(admin);

    // when
    updateAdminService.updateAgencyAdmin("adminId", updateAgencyAdminDTO);

    // then
    verify(identityClient).updateUserData(any(), userDTOCaptor.capture(), any(), any());
    assertEquals(2, userDTOCaptor.getValue().getTenantId());
    verify(adminRepository).save(admin);
  }
}
