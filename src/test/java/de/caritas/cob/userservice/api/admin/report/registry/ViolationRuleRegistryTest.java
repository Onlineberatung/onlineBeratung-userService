package de.caritas.cob.userservice.api.admin.report.registry;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.admin.report.model.AgencyDependedViolationReportRule;
import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ViolationRuleRegistryTest {

  @InjectMocks private ViolationRuleRegistry violationRuleRegistry;

  @Mock private ApplicationContext applicationContext;

  @Mock private AgencyDependedViolationReportRule agencyDependedViolationReportRule;

  @BeforeEach
  void setup() {
    Map<String, ViolationReportRule> mockedBeans = mock(Map.class);
    List<ViolationReportRule> mockedRules =
        asList(mock(ViolationReportRule.class), agencyDependedViolationReportRule);
    when(mockedBeans.values()).thenReturn(mockedRules);
    when(this.applicationContext.getBeansOfType(ViolationReportRule.class)).thenReturn(mockedBeans);
  }

  @Test
  void getViolationReportRules_Should_returnRegistratedRules_When_regitryIsInitialized() {
    this.violationRuleRegistry.initialize();

    List<ViolationReportRule> registeredRules =
        this.violationRuleRegistry.getViolationReportRules(emptyList());

    assertThat(registeredRules, hasSize(2));
    verify(agencyDependedViolationReportRule, times(1)).setAllAgencies(anyList());
  }

  @Test
  void getViolationReportRules_Should_returnEmptyList_When_regitryIsNotInitialized() {
    List<ViolationReportRule> registeredRules =
        this.violationRuleRegistry.getViolationReportRules(emptyList());

    assertThat(registeredRules, hasSize(0));
    verifyNoInteractions(agencyDependedViolationReportRule);
  }
}
