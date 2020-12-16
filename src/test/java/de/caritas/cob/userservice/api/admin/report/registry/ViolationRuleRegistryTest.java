package de.caritas.cob.userservice.api.admin.report.registry;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class ViolationRuleRegistryTest {

  @InjectMocks
  private ViolationRuleRegistry violationRuleRegistry;

  @Mock
  private ApplicationContext applicationContext;

  @Before
  public void setup() {
    Map<String, ViolationReportRule> mockedBeans = mock(Map.class);
    List<ViolationReportRule> mockedRules = asList(mock(ViolationReportRule.class),
        mock(ViolationReportRule.class));
    when(mockedBeans.values()).thenReturn(mockedRules);
    when(this.applicationContext.getBeansOfType(ViolationReportRule.class)).thenReturn(mockedBeans);
  }

  @Test
  public void getViolationReportRules_Should_returnRegistratedRules_When_regitryIsInitialized() {
    this.violationRuleRegistry.initialize();

    List<ViolationReportRule> registeredRules = this.violationRuleRegistry
        .getViolationReportRules();

    assertThat(registeredRules, hasSize(2));
  }

  @Test
  public void getViolationReportRules_Should_returnEmptyList_When_regitryIsNotInitialized() {
    List<ViolationReportRule> registeredRules = this.violationRuleRegistry
        .getViolationReportRules();

    assertThat(registeredRules, hasSize(0));
  }

}
