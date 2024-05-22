package de.caritas.cob.userservice.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.config.apiclient.ConsultingTypeServiceApiControllerFactory;
import de.caritas.cob.userservice.api.service.httpheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.api.service.httpheader.TenantHeaderSupplier;
import de.caritas.cob.userservice.consultingtypeservice.generated.ApiClient;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.BasicConsultingTypeResponseDTO;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class ConsultingTypeServiceTest {

  @InjectMocks private ConsultingTypeService consultingTypeService;

  @Mock private ConsultingTypeControllerApi consultingTypeControllerApi;

  @Mock private SecurityHeaderSupplier securityHeaderSupplier;

  @Mock private ServletRequestAttributes requestAttributes;

  @Mock private HttpServletRequest httpServletRequest;

  @Mock private Enumeration<String> headers;

  @Mock private TenantHeaderSupplier tenantHeaderSupplier;

  @Mock private ConsultingTypeServiceApiControllerFactory consultingTypeServiceApiControllerFactory;

  @BeforeEach
  void setUp() {
    when(consultingTypeServiceApiControllerFactory.createControllerApi())
        .thenReturn(consultingTypeControllerApi);
    when(consultingTypeControllerApi.getApiClient()).thenReturn(new ApiClient());
  }

  @Test
  void getAllConsultingTypeIds_Should_Return_expectedIdList_From_BasicConsultingTypeResponseDTO() {
    when(consultingTypeServiceApiControllerFactory.createControllerApi())
        .thenReturn(consultingTypeControllerApi);

    int size = 15;
    var randomBasicConsultingTypeResponseDTOList =
        generateRandomExtendedConsultingTypeResponseDTOList(size);
    when(consultingTypeControllerApi.getBasicConsultingTypeList())
        .thenReturn(randomBasicConsultingTypeResponseDTOList);
    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(new HttpHeaders());

    List<Integer> consultingTypeIds = consultingTypeService.getAllConsultingTypeIds(null);

    assertEquals(consultingTypeIds.size(), size);
    assertEquals(
        randomBasicConsultingTypeResponseDTOList.stream()
            .map(BasicConsultingTypeResponseDTO::getId)
            .collect(Collectors.toList()),
        consultingTypeIds);
    resetRequestAttributes();
  }

  private BasicConsultingTypeResponseDTO generateExtendedConsultingTypeResponseDTO(int id) {
    return new BasicConsultingTypeResponseDTO().id(id);
  }

  private List<BasicConsultingTypeResponseDTO> generateRandomExtendedConsultingTypeResponseDTOList(
      int size) {
    return new Random()
        .ints(size, Integer.MIN_VALUE, Integer.MAX_VALUE)
        .mapToObj(this::generateExtendedConsultingTypeResponseDTO)
        .collect(Collectors.toList());
  }

  @Test
  void getExtendedConsultingTypeResponseDTO_Should_callConsultingTypeController_When_idExists() {

    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(new HttpHeaders());

    this.consultingTypeService.getExtendedConsultingTypeResponseDTO(1);

    verify(this.consultingTypeControllerApi, times(1)).getExtendedConsultingTypeById(1);
    resetRequestAttributes();
  }

  private void resetRequestAttributes() {
    RequestContextHolder.setRequestAttributes(null);
  }
}
