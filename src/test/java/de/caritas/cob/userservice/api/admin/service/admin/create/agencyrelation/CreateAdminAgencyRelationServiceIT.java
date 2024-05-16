package de.caritas.cob.userservice.api.admin.service.admin.create.agencyrelation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import de.caritas.cob.userservice.api.exception.httpresponses.NoContentException;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.AdminAgency;
import de.caritas.cob.userservice.api.port.out.AdminAgencyRepository;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.List;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class CreateAdminAgencyRelationServiceIT {

  private final String VALID_ADMIN_ID = "164be67d-4d1b-4d80-bb6b-0ee057a1c59e";

  @Autowired private CreateAdminAgencyRelationService createAdminAgencyRelationService;
  @Autowired private AdminAgencyRepository adminAgencyRepository;
  @Autowired private AdminRepository adminRepository;
  @MockBean private AgencyService agencyService;

  EasyRandom easyRandom = new EasyRandom();

  @Test
  public void create_Should_createExpectedCreatedAdmin_When_inputDataIsCorrect() {
    // given
    long agencyId = 12L;
    CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO = new CreateAdminAgencyRelationDTO();
    createAdminAgencyRelationDTO.setAgencyId(agencyId);
    AgencyDTO agencyDTO = easyRandom.nextObject(AgencyDTO.class);
    agencyDTO.setId(agencyId);
    when(agencyService.getAgencyWithoutCaching(agencyId)).thenReturn(agencyDTO);
    Admin admin = createAdminWithoutAgency();

    // when
    this.createAdminAgencyRelationService.create(admin.getId(), createAdminAgencyRelationDTO);

    // then
    List<AdminAgency> result = this.adminAgencyRepository.findByAdminId(admin.getId());
    assertThat(result, notNullValue());
    assertThat(result, hasSize(1));
  }

  @Test
  public void create_Should_throwNoContentException_When_adminDoesNotExist() {
    assertThrows(
        NoContentException.class,
        () -> {
          // given
          CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO =
              easyRandom.nextObject(CreateAdminAgencyRelationDTO.class);

          // then
          this.createAdminAgencyRelationService.create("invalid", createAdminAgencyRelationDTO);
        });
  }

  @Test
  public void create_Should_throwBadRequestException_When_agencyDoesNotExist() {
    assertThrows(
        BadRequestException.class,
        () -> {
          // given
          CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO =
              new CreateAdminAgencyRelationDTO();
          long agencyId = 99L;
          createAdminAgencyRelationDTO.setAgencyId(agencyId);
          when(agencyService.getAgencyWithoutCaching(agencyId))
              .thenThrow(new BadRequestException("Agency not found"));

          // then
          createAdminAgencyRelationService.create(VALID_ADMIN_ID, createAdminAgencyRelationDTO);
        });
  }

  private Admin createAdminWithoutAgency() {
    Admin admin = easyRandom.nextObject(Admin.class);
    admin.setType(Admin.AdminType.AGENCY);
    return this.adminRepository.save(admin);
  }
}
