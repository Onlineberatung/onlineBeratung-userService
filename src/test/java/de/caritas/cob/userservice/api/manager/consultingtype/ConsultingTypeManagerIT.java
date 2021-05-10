package de.caritas.cob.userservice.api.manager.consultingtype;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.UserServiceApplication;
import de.caritas.cob.userservice.api.exception.MissingConsultingTypeException;
import de.caritas.cob.userservice.api.service.ConsultingTypeService;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class ConsultingTypeManagerIT {

  @Autowired
  private ConsultingTypeManager consultingTypeManager;

  @MockBean
  private ConsultingTypeService consultingTypeService;

  @Test
  public void getConsultantTypeSettings_Should_Throw_MissingConsultingTypeException_When_RestClientException() {
    when(consultingTypeService.getExtendedConsultingTypeResponseDTO(anyInt()))
        .thenThrow(new RestClientException(""));

    assertThrows(MissingConsultingTypeException.class,
        () -> consultingTypeManager.getConsultingTypeSettings(anyInt()));
  }

  @Test
  public void getConsultantTypeSettings_Should_Return_ExtendedConsultingTypeResponseDTO()
      throws MissingConsultingTypeException {
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO = new ExtendedConsultingTypeResponseDTO();
    when(consultingTypeService.getExtendedConsultingTypeResponseDTO(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    assertTrue(extendedConsultingTypeResponseDTO
        .equals(consultingTypeManager.getConsultingTypeSettings(anyInt())));

  }

  @Test
  public void isConsultantBoundedToAgency_Should_Return_True()
      throws MissingConsultingTypeException {
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO = new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setConsultantBoundedToConsultingType(true);
    when(consultingTypeService.getExtendedConsultingTypeResponseDTO(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    assertTrue(consultingTypeManager.isConsultantBoundedToAgency(anyInt()));
  }

  @Test
  public void isConsultantBoundedToAgency_Should_Return_False()
      throws MissingConsultingTypeException {
    ExtendedConsultingTypeResponseDTO extendedConsultingTypeResponseDTO = new ExtendedConsultingTypeResponseDTO();
    extendedConsultingTypeResponseDTO.setConsultantBoundedToConsultingType(false);
    when(consultingTypeService.getExtendedConsultingTypeResponseDTO(anyInt()))
        .thenReturn(extendedConsultingTypeResponseDTO);

    assertFalse(consultingTypeManager.isConsultantBoundedToAgency(anyInt()));
  }

  @Test
  public void getAllConsultingTypeIds_Should_Return_The_Same_List()
      throws MissingConsultingTypeException {
    when(consultingTypeService.getAllConsultingTypeIds()).thenReturn(List.of(1, 2, 3, 4));

    assertTrue(consultingTypeManager.getAllConsultingTypeIds().equals(List.of(1, 2, 3, 4)));
  }
}
