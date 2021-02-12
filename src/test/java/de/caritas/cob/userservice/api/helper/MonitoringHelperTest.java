package de.caritas.cob.userservice.api.helper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import de.caritas.cob.userservice.api.model.monitoring.MonitoringDTO;
import de.caritas.cob.userservice.api.repository.monitoring.Monitoring;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringType;
import de.caritas.cob.userservice.api.repository.monitoringoption.MonitoringOption;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;

@RunWith(MockitoJUnitRunner.class)
public class MonitoringHelperTest {

  @Spy
  @InjectMocks
  private MonitoringHelper monitoringHelper;

  private final Long SESSION_ID = 123L;
  private final MonitoringDTO SORTED_MONITORING_DTO = new MonitoringDTO();
  private final String ACLOHOL = "alcohol";
  private final String DRUGS = "drugs";
  private final String OTHERS = "others";
  private final ConsultingType TYPE_SUCHT = ConsultingType.SUCHT;
  private final List<Monitoring> SORTED_MONITORING_LIST = new ArrayList<Monitoring>();
  private final List<Monitoring> UNSORTED_MONITORING_LIST = new ArrayList<Monitoring>();
  private final List<MonitoringOption> DRUGS_MONITORING_OPTION_LIST =
      new ArrayList<MonitoringOption>();
  private final Monitoring ALCOHOL_MONITORING =
      new Monitoring(SESSION_ID, MonitoringType.ADDICTIVE_DRUGS, ACLOHOL, true);
  private final Monitoring DRUGS_MONITORING = new Monitoring(SESSION_ID,
      MonitoringType.ADDICTIVE_DRUGS, DRUGS, null, DRUGS_MONITORING_OPTION_LIST);
  private final MonitoringOption OTHERS_MONITORING_OPTION = new MonitoringOption(SESSION_ID,
      MonitoringType.ADDICTIVE_DRUGS, DRUGS, OTHERS, false, DRUGS_MONITORING);

  @Before
  public void setUp() {
    LinkedHashMap<String, Object> drugsMap = new LinkedHashMap<String, Object>();
    drugsMap.put(OTHERS, false);
    LinkedHashMap<String, Object> sortedAddictiveDrugsMap = new LinkedHashMap<String, Object>();
    sortedAddictiveDrugsMap.put(ACLOHOL, true);
    sortedAddictiveDrugsMap.put(DRUGS, drugsMap);
    SORTED_MONITORING_DTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(),
        sortedAddictiveDrugsMap);

    DRUGS_MONITORING_OPTION_LIST.add(OTHERS_MONITORING_OPTION);
    SORTED_MONITORING_LIST.add(ALCOHOL_MONITORING);
    SORTED_MONITORING_LIST.add(DRUGS_MONITORING);

    UNSORTED_MONITORING_LIST.add(DRUGS_MONITORING);
    UNSORTED_MONITORING_LIST.add(ALCOHOL_MONITORING);
  }

  @Test
  public void createMonitoringList_Should_ReturnCorrectMonitoringList_WhenCalled() {

    List<Monitoring> monitoringList =
        monitoringHelper.createMonitoringList(SORTED_MONITORING_DTO, SESSION_ID);

    assertEquals(monitoringList.get(0).getMonitoringOptionList(),
        SORTED_MONITORING_LIST.get(0).getMonitoringOptionList());
  }

  @SuppressWarnings("unlikely-arg-type")
  @Test
  public void sortMonitoringMap_Should_ReturnCorrectlySortedMap_WhenCalled() {

    doReturn(SORTED_MONITORING_DTO).when(monitoringHelper).getMonitoringInitalList(Mockito.any());

    LinkedHashMap<String, Object> drugsMap = new LinkedHashMap<String, Object>();
    drugsMap.put(OTHERS, false);
    LinkedHashMap<String, Object> unsortedAddictiveDrugsMap = new LinkedHashMap<String, Object>();
    unsortedAddictiveDrugsMap.put(DRUGS, drugsMap);
    unsortedAddictiveDrugsMap.put(ACLOHOL, true);
    LinkedHashMap<String, Object> unsortedMap = new LinkedHashMap<String, Object>();
    unsortedMap.put(MonitoringType.ADDICTIVE_DRUGS.getKey(), unsortedAddictiveDrugsMap);

    LinkedHashMap<String, Object> sortedMap =
        monitoringHelper.sortMonitoringMap(unsortedMap, TYPE_SUCHT);

    assertEquals(sortedMap.get(TYPE_SUCHT),
        SORTED_MONITORING_LIST.get(0).getMonitoringOptionList());
  }

}
