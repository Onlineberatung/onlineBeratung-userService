package de.caritas.cob.userservice.api;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class AccountManagerTest {

  @InjectMocks AccountManager accountManager;

  @Mock ConsultantRepository consultantRepository;

  @Mock ConsultantAgencyRepository consultantAgencyRepository;

  @Mock UserServiceMapper userServiceMapper;

  @Mock AgencyService agencyService;

  @Mock TenantService tenantService;

  @Mock Page<Consultant.ConsultantBase> page;

  @Test
  void findConsultantsByInfix_Should_NotFilterByAgenciesIfAgencyListIsEmpty() {
    // given
    Mockito.when(
            consultantRepository.findAllByInfix(
                Mockito.eq("infix"), Mockito.any(PageRequest.class)))
        .thenReturn(page);

    // when
    accountManager.findConsultantsByInfix(
        "infix", false, Lists.newArrayList(), 1, 10, "email", true);

    // then
    Mockito.verify(consultantRepository)
        .findAllByInfix(Mockito.eq("infix"), Mockito.any(PageRequest.class));
  }

  @Test
  void findConsultantsByInfix_Should_FilterByAgenciesIfAgencyListIsNotEmpty() {
    // given
    Mockito.when(
            consultantRepository.findAllByInfixAndAgencyIds(
                Mockito.eq("infix"), Mockito.anyCollection(), Mockito.any(PageRequest.class)))
        .thenReturn(page);

    // when
    accountManager.findConsultantsByInfix(
        "infix", true, Lists.newArrayList(1L), 1, 10, "email", true);

    // then
    Mockito.verify(consultantRepository)
        .findAllByInfixAndAgencyIds(
            Mockito.eq("infix"),
            Mockito.eq(Lists.newArrayList(1L)),
            Mockito.any(PageRequest.class));
  }
}
