package de.caritas.cob.userservice.api.admin.service.admin.update.agencyrelation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.UserServiceApplication;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.AdminAgency;
import de.caritas.cob.userservice.api.port.out.AdminAgencyRepository;
import de.caritas.cob.userservice.api.port.out.AdminRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
public class SynchronizeAdminAgencyRelationIT {

  private final String VALID_ADMIN_ID = "164be67d-4d1b-4d80-bb6b-0ee057a1c59e";

  @Autowired private SynchronizeAdminAgencyRelation synchronizeAdminAgencyRelation;
  @Autowired private AdminAgencyRepository adminAgencyRepository;
  @Autowired private AdminRepository adminRepository;
  @MockBean private AgencyService agencyService;

  EasyRandom easyRandom = new EasyRandom();

  @Test
  public void
      synchronizeAdminAgenciesRelation_Should_addCorrectAgenciesForAdmin_When_relationsAreProvided() {
    // given
    long agencyId1 = 90L;
    long agencyId2 = 12L;

    List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOs =
        createNewAdminAgencyRelationDTOs(agencyId1, agencyId2);

    // when
    synchronizeAdminAgencyRelation.synchronizeAdminAgenciesRelation(
        VALID_ADMIN_ID, newAdminAgencyRelationDTOs);

    // then
    List<AdminAgency> result = this.adminAgencyRepository.findByAdminId(VALID_ADMIN_ID);
    assertThat(result, notNullValue());
    assertThat(result, hasSize(2));
    assertThat(
        result.stream().map(AdminAgency::getAgencyId).collect(Collectors.toList()),
        containsInAnyOrder(agencyId1, agencyId2));
  }

  @Test
  public void
      synchronizeAdminAgenciesRelation_Should_deleteAgenciesForAdmin_When_relationsAreProvided() {
    // given
    long agencyId2 = 12L;

    List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOs =
        createNewAdminAgencyRelationDTOs(agencyId2);

    // when
    synchronizeAdminAgencyRelation.synchronizeAdminAgenciesRelation(
        VALID_ADMIN_ID, newAdminAgencyRelationDTOs);

    // then
    List<AdminAgency> result = this.adminAgencyRepository.findByAdminId(VALID_ADMIN_ID);
    assertThat(result, notNullValue());
    assertThat(result, hasSize(1));
    assertThat(
        result.stream().map(AdminAgency::getAgencyId).collect(Collectors.toList()),
        containsInAnyOrder(agencyId2));
  }

  @Test
  public void
      synchronizeAdminAgenciesRelation_Should_syncAgenciesForAdmin_When_relationsAreProvided() {
    // given
    long agencyId1 = 13L;
    long agencyId2 = 14L;
    long agencyId3 = 15L;

    List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOs =
        createNewAdminAgencyRelationDTOs(agencyId1, agencyId2, agencyId3);

    // when
    synchronizeAdminAgencyRelation.synchronizeAdminAgenciesRelation(
        VALID_ADMIN_ID, newAdminAgencyRelationDTOs);

    // then
    List<AdminAgency> result = this.adminAgencyRepository.findByAdminId(VALID_ADMIN_ID);
    assertThat(result, notNullValue());
    assertThat(result, hasSize(3));
    assertThat(
        result.stream().map(AdminAgency::getAgencyId).collect(Collectors.toList()),
        containsInAnyOrder(agencyId1, agencyId2, agencyId3));
  }

  private List<CreateAdminAgencyRelationDTO> createNewAdminAgencyRelationDTOs(long... agencyIds) {
    List<CreateAdminAgencyRelationDTO> createAdminAgencyRelationDTOS = new ArrayList<>();
    Arrays.stream(agencyIds)
        .forEach(
            agencyId -> {
              CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO =
                  new CreateAdminAgencyRelationDTO();
              createAdminAgencyRelationDTO.setAgencyId(agencyId);
              AgencyDTO agencyDTO = easyRandom.nextObject(AgencyDTO.class);
              agencyDTO.setId(agencyId);
              when(agencyService.getAgencyWithoutCaching(agencyId)).thenReturn(agencyDTO);
              createAdminAgencyRelationDTOS.add(createAdminAgencyRelationDTO);
            });
    return createAdminAgencyRelationDTOS;
  }

  private Admin createAdminWithoutAgency() {
    Admin admin = easyRandom.nextObject(Admin.class);
    return this.adminRepository.save(admin);
  }
}
