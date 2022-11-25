package de.caritas.cob.userservice.api;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.ConsultantAgency;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AccountManagerTest {

  private static final long AGENCY_TO_FILTER = 1L;
  private static final long ANOTHER_AGENCY = 2L;
  @InjectMocks AccountManager accountManager;

  @Mock ConsultantRepository consultantRepository;

  @Mock ConsultantAgencyRepository consultantAgencyRepository;

  @Mock UserServiceMapper userServiceMapper;

  @Mock AgencyService agencyService;

  @Mock Page<Consultant.ConsultantBase> page;

  @Test
  void findConsultantsByInfix_Should_NotFilterByAgenciesIfAgencyListIsEmpty() {
    // given
    Mockito.when(
            consultantRepository.findAllByInfix(
                Mockito.eq("infix"), Mockito.any(PageRequest.class)))
        .thenReturn(page);

    // when
    accountManager.findConsultantsByInfix("infix", Lists.newArrayList(), 1, 10, "email", true);

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
    Mockito.when(consultantAgencyRepository.findByConsultantIdIn(Mockito.anyList()))
            .thenReturn(Lists.newArrayList(givenConsultantAgencyWithAgencyId(AGENCY_TO_FILTER), givenConsultantAgencyWithAgencyId(ANOTHER_AGENCY)));

    // when
    accountManager.findConsultantsByInfix("infix", Lists.newArrayList(AGENCY_TO_FILTER), 1, 10, "email", true);

    // then
    Mockito.verify(consultantRepository)
        .findAllByInfixAndAgencyIds(
            Mockito.eq("infix"),
            Mockito.eq(Lists.newArrayList(AGENCY_TO_FILTER)),
            Mockito.any(PageRequest.class));

    ArgumentCaptor<List<ConsultantAgency.ConsultantAgencyBase>> captor = ArgumentCaptor.forClass(List.class);
    Mockito.verify(userServiceMapper).mapOf(Mockito.any(), Mockito.eq(Lists.newArrayList()), Mockito.eq(Lists.newArrayList()), captor.capture());
    List<Long> consultantAgencyIds = captor.getValue().stream().map(ConsultantAgency.ConsultantAgencyBase::getAgencyId).collect(Collectors.toList());
    assertThat(consultantAgencyIds).hasSize(1);
    assertThat(consultantAgencyIds).containsOnly(1L);
  }

  private ConsultantAgency.ConsultantAgencyBase givenConsultantAgencyWithAgencyId(Long agencyId) {
    ConsultantAgency consultantAgency = new ConsultantAgency();
    consultantAgency.setAgencyId(agencyId);
    return new ConsultantAgency.ConsultantAgencyBase() {
      @Override
      public Long getId() {
        return consultantAgency.getId();
      }

      @Override
      public Long getAgencyId() {
        return consultantAgency.getAgencyId();
      }

      @Override
      public String getConsultantId() {
        return consultantAgency.getConsultant().getId();
      }

      @Override
      public LocalDateTime getDeleteDate() {
        return consultantAgency.getDeleteDate();
      }
    };
  }
}
