package de.caritas.cob.userservice.api.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.service.securityheader.SecurityHeaderSupplier;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.ConsultingTypeControllerApi;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.BasicConsultingTypeResponseDTO;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ConsultingTypeServiceTest {

  @InjectMocks
  private ConsultingTypeService consultingTypeService;

  @Mock
  private ConsultingTypeControllerApi consultingTypeControllerApi;

  @Mock
  private SecurityHeaderSupplier securityHeaderSupplier;

  @Test
  public void ConsultingTypeService_Should_Return_A_IdList_From_BasicConsultingTypeResponseDTO() {
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

}
