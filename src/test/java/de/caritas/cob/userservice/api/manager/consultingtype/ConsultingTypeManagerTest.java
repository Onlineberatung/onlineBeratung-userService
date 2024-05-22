package de.caritas.cob.userservice.api.manager.consultingtype;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.service.ConsultingTypeService;
import de.caritas.cob.userservice.api.tenant.TenantContext;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class ConsultingTypeManagerTest {

  @InjectMocks private ConsultingTypeManager consultingTypeManager;

  @Mock private ConsultingTypeService consultingTypeService;

  @Test
  void
      getConsultantTypeSettings_Should_Throw_MissingConsultingTypeException_When_RestClientException() {
    when(consultingTypeService.getExtendedConsultingTypeResponseDTO(anyInt()))
        .thenThrow(new RestClientException(""));

    assertThrows(
        MissingConsultingTypeException.class,
        () -> consultingTypeManager.getConsultingTypeSettings(1));
  }

  @Test
  void getConsultantTypeSettings_Should_Return_ExtendedConsultingTypeResponseDTO()
      throws MissingConsultingTypeException {
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
        new ExtendedConsultingTypeResponseDTO();
    when(consultingTypeService.getExtendedConsultingTypeResponseDTO(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    assertEquals(
        extendedConsultingTypeResponseDTO,
        consultingTypeManager.getConsultingTypeSettings(anyInt()));
  }

  @Test
  void isConsultantBoundedToAgency_Should_Return_True() throws MissingConsultingTypeException {
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
        new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setConsultantBoundedToConsultingType(true);
    when(consultingTypeService.getExtendedConsultingTypeResponseDTO(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    assertTrue(consultingTypeManager.isConsultantBoundedToAgency(anyInt()));
  }

  @Test
  void isConsultantBoundedToAgency_Should_Return_False() throws MissingConsultingTypeException {
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO =
        new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setConsultantBoundedToConsultingType(false);
    when(consultingTypeService.getExtendedConsultingTypeResponseDTO(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    assertFalse(consultingTypeManager.isConsultantBoundedToAgency(anyInt()));
  }

  @Test
  void getAllConsultingTypeIds_Should_Return_The_Same_List() throws MissingConsultingTypeException {
    Long tenant1 = 1L;
    Long tenant2 = 2L;
    when(consultingTypeService.getAllConsultingTypeIds(null))
        .thenReturn(List.of(1, 2, 3, 4, 5, 6, 7, 8));
    when(consultingTypeService.getAllConsultingTypeIds(tenant1)).thenReturn(List.of(1, 2, 3, 4));
    when(consultingTypeService.getAllConsultingTypeIds(tenant2)).thenReturn(List.of(5, 6, 7, 8));

    TenantContext.clear();
    assertEquals(consultingTypeManager.getAllConsultingTypeIds(), List.of(1, 2, 3, 4, 5, 6, 7, 8));
    TenantContext.setCurrentTenant(tenant1);
    assertEquals(consultingTypeManager.getAllConsultingTypeIds(), List.of(1, 2, 3, 4));
    TenantContext.setCurrentTenant(tenant2);
    assertEquals(consultingTypeManager.getAllConsultingTypeIds(), List.of(5, 6, 7, 8));
    TenantContext.clear();
  }
}
