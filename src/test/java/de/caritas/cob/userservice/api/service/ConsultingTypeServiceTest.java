package de.caritas.cob.userservice.api.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.BasicConsultingTypeResponseDTO;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RunWith(SpringRunner.class)
public class ConsultingTypeServiceTest {

  @InjectMocks
  private ConsultingTypeService consultingTypeService;

  @Mock
  private ConsultingTypeControllerApi consultingTypeControllerApi;

  @Mock
  private SecurityHeaderSupplier securityHeaderSupplier;

  @Mock
  private ServletRequestAttributes requestAttributes;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private Enumeration<String> headers;


  @Test
  public void ConsultingTypeService_Should_Return_expectedIdList_From_BasicConsultingTypeResponseDTO() {
    givenRequestContextIsSet();
    int size = 15;
    var randomBasicConsultingTypeResponseDTOList = generateRandomExtendedConsultingTypeResponseDTOList(
        size);
    when(consultingTypeControllerApi.getBasicConsultingTypeList())
        .thenReturn(randomBasicConsultingTypeResponseDTOList);
    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(new HttpHeaders());

    List<Integer> consultingTypeIds = consultingTypeService.getAllConsultingTypeIds();

    assertEquals(consultingTypeIds.size(), size);
    assertEquals(randomBasicConsultingTypeResponseDTOList.stream().map(
            BasicConsultingTypeResponseDTO::getId)
        .collect(Collectors.toList()), consultingTypeIds);
    resetRequestAttributes();
  }

  private BasicConsultingTypeResponseDTO generateExtendedConsultingTypeResponseDTO(int id) {
    return new BasicConsultingTypeResponseDTO().id(id);
  }

  private List<BasicConsultingTypeResponseDTO> generateRandomExtendedConsultingTypeResponseDTOList(
      int size) {
    return new Random().ints(size, Integer.MIN_VALUE, Integer.MAX_VALUE)
        .mapToObj(this::generateExtendedConsultingTypeResponseDTO).collect(
            Collectors.toList());
  }

  @Test
  public void getExtendedConsultingTypeResponseDTO_Should_callConsultingTypeController_When_idExists() {
    givenRequestContextIsSet();
    when(securityHeaderSupplier.getCsrfHttpHeaders()).thenReturn(new HttpHeaders());

    this.consultingTypeService.getExtendedConsultingTypeResponseDTO(1);

    verify(this.consultingTypeControllerApi, times(1)).getExtendedConsultingTypeById(1);
    resetRequestAttributes();
  }

  private void resetRequestAttributes() {
    RequestContextHolder.setRequestAttributes(null);
  }

  private void givenRequestContextIsSet() {
    when(requestAttributes.getRequest()).thenReturn(httpServletRequest);
    when(httpServletRequest.getHeaderNames()).thenReturn(headers);
    RequestContextHolder.setRequestAttributes(requestAttributes);
  }


}
