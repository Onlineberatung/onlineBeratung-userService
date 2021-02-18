package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.repository.session.ConsultingType.SUCHT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.monitoring.MonitoringDTO;
import de.caritas.cob.userservice.api.repository.monitoring.Monitoring;
import de.caritas.cob.userservice.api.repository.monitoring.MonitoringType;
import de.caritas.cob.userservice.api.repository.monitoringoption.MonitoringOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MonitoringHelperTest {

  private static final Long SESSION_ID = 123L;
  private static final MonitoringDTO SORTED_MONITORING_DTO = new MonitoringDTO();
  private static final String ALCOHOL = "alcohol";
  private static final String DRUGS = "drugs";
  private static final String OTHERS = "others";
  private static final String ADDICTIVE_DRUGS = "addictiveDrugs";
  private static final String CANNABIS = "cannabis";
  private static final String COCAINE_CRACK = "cocaineCrack";
  private static final String LEGAL_HIGHS = "legalHighs";
  private static final String GAMBLING = "gambling";
  private static final String ONLINE = "online";
  private static final String OFFLINE = "offline";

  @Spy
  @InjectMocks
  private MonitoringHelper monitoringHelper;

  @Mock
  private ConsultingTypeManager consultingTypeManager;

  private final List<Monitoring> SORTED_MONITORING_LIST = new ArrayList<Monitoring>();
  private final List<Monitoring> UNSORTED_MONITORING_LIST = new ArrayList<Monitoring>();
  private final List<MonitoringOption> DRUGS_MONITORING_OPTION_LIST =
      new ArrayList<MonitoringOption>();
  private final Monitoring ALCOHOL_MONITORING =
      new Monitoring(SESSION_ID, MonitoringType.ADDICTIVE_DRUGS, ALCOHOL, true);
  private final Monitoring DRUGS_MONITORING = new Monitoring(SESSION_ID,
      MonitoringType.ADDICTIVE_DRUGS, DRUGS, null, DRUGS_MONITORING_OPTION_LIST);
  private final MonitoringOption OTHERS_MONITORING_OPTION = new MonitoringOption(SESSION_ID,
      MonitoringType.ADDICTIVE_DRUGS, DRUGS, OTHERS, false, DRUGS_MONITORING);

  @Before
  public void setUp() {
    LinkedHashMap<String, Object> drugsMap = new LinkedHashMap<String, Object>();
    drugsMap.put(OTHERS, false);
    LinkedHashMap<String, Object> sortedAddictiveDrugsMap = new LinkedHashMap<String, Object>();
    sortedAddictiveDrugsMap.put(ALCOHOL, true);
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
    unsortedAddictiveDrugsMap.put(ALCOHOL, true);
    LinkedHashMap<String, Object> unsortedMap = new LinkedHashMap<String, Object>();
    unsortedMap.put(MonitoringType.ADDICTIVE_DRUGS.getKey(), unsortedAddictiveDrugsMap);

    Map<String, Object> sortedMap = monitoringHelper.sortMonitoringMap(unsortedMap, SUCHT);

    assertEquals(sortedMap.get(SUCHT),
        SORTED_MONITORING_LIST.get(0).getMonitoringOptionList());
  }

  @Test
  public void sortMonitoringMap_Should_removeEntryInSortedMap_When_entryDoesNotExistInChildLevelUnsortedMap() {
    Map<String, Object> sortedMap = MonitoringStructureBuilder.getInstance()
        .addEntry(ALCOHOL, false)
        .addSubLevel(DRUGS, MonitoringStructureBuilder.getInstance()
            .addEntry(OTHERS, false)
            .getMonitoringStructure())
        .getMonitoringStructure();
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), sortedMap);
    doReturn(monitoringDTO).when(monitoringHelper).getMonitoringInitalList(Mockito.any());
    Map<String, Object> unsortedMap = MonitoringStructureBuilder.getInstance()
        .addSubLevel(ADDICTIVE_DRUGS, MonitoringStructureBuilder.getInstance()
            .addSubLevel(DRUGS, new LinkedHashMap<>())
            .addEntry(ALCOHOL, true)
            .getMonitoringStructure())
        .getMonitoringStructure();

    Map<String, Object> sortedResultMap = monitoringHelper.sortMonitoringMap(unsortedMap, SUCHT);

    assertThat(sortedResultMap, notNullValue());
    Map<String, Object> rootResult = (Map<String, Object>) sortedResultMap.get(ADDICTIVE_DRUGS);
    assertThat(rootResult.get(ALCOHOL), is(true));
    Map<String, Object> drugs = (Map<String, Object>) rootResult.get(DRUGS);
    assertThat(drugs.get(OTHERS), is(false));
  }

  @Test
  public void sortMonitoringMap_Should_preferValuesOfJsonMap_When_entryHasOtherStructureElement() {
    Map<String, Object> sortedMap = MonitoringStructureBuilder.getInstance()
        .addEntry(ALCOHOL, false)
        .addSubLevel(DRUGS, MonitoringStructureBuilder.getInstance()
            .addEntry(OTHERS, false)
            .getMonitoringStructure())
        .getMonitoringStructure();
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), sortedMap);
    doReturn(monitoringDTO).when(monitoringHelper).getMonitoringInitalList(Mockito.any());
    Map<String, Object> unsortedMap = MonitoringStructureBuilder.getInstance()
        .addSubLevel(ADDICTIVE_DRUGS, MonitoringStructureBuilder.getInstance()
            .addSubLevel(DRUGS, MonitoringStructureBuilder.getInstance()
                .addEntry(CANNABIS, true)
                .getMonitoringStructure())
            .getMonitoringStructure())
        .getMonitoringStructure();

    Map<String, Object> sortedResultMap = monitoringHelper.sortMonitoringMap(unsortedMap, SUCHT);

    assertThat(sortedResultMap, notNullValue());
    Map<String, Object> rootResult = (Map<String, Object>) sortedResultMap.get(ADDICTIVE_DRUGS);
    assertThat(rootResult.get(ALCOHOL), is(false));
    Map<String, Object> drugs = (Map<String, Object>) rootResult.get(DRUGS);
    assertThat(drugs.get(OTHERS), is(false));
  }

  @Test
  public void sortMonitoringMap_Should_addDefaultValueForMissingUnsortedProperty_When_monitoringStructureHasEntriesOnChildLevelWhichAreNotAvailableInUnsortedMap() {
    Map<String, Object> sortedMap = MonitoringStructureBuilder.getInstance()
        .addEntry(ALCOHOL, false)
        .addSubLevel(DRUGS, MonitoringStructureBuilder.getInstance()
            .addEntry(CANNABIS, false)
            .addEntry(COCAINE_CRACK, false)
            .addEntry(OTHERS, false)
            .getMonitoringStructure())
        .addEntry(LEGAL_HIGHS, false)
        .addSubLevel(GAMBLING, MonitoringStructureBuilder.getInstance()
            .addEntry(ONLINE, false)
            .addEntry(OFFLINE, false)
            .getMonitoringStructure())
        .getMonitoringStructure();
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), sortedMap);
    doReturn(monitoringDTO).when(monitoringHelper).getMonitoringInitalList(Mockito.any());
    Map<String, Object> unsortedMap = MonitoringStructureBuilder.getInstance()
        .addSubLevel(ADDICTIVE_DRUGS, MonitoringStructureBuilder.getInstance()
            .addEntry(LEGAL_HIGHS, true)
            .addSubLevel(DRUGS, MonitoringStructureBuilder.getInstance()
                .addEntry(COCAINE_CRACK, true)
                .addEntry(OTHERS, false)
                .addEntry(CANNABIS, true)
                .getMonitoringStructure())
            .addEntry(ALCOHOL, false)
            .getMonitoringStructure())
        .getMonitoringStructure();

    Map<String, Object> sortedResultMap = monitoringHelper.sortMonitoringMap(unsortedMap, SUCHT);

    assertThat(sortedResultMap, notNullValue());
    Map<String, Object> rootResult = (Map<String, Object>) sortedResultMap.get(ADDICTIVE_DRUGS);
    assertThat(rootResult.get(ALCOHOL), is(false));
    assertThat(rootResult.get(LEGAL_HIGHS), is(true));
    Map<String, Object> drugs = (Map<String, Object>) rootResult.get(DRUGS);
    assertThat(drugs.get(CANNABIS), is(true));
    assertThat(drugs.get(COCAINE_CRACK), is(true));
    assertThat(drugs.get(OTHERS), is(false));
    Map<String, Object> gambling = (Map<String, Object>) rootResult.get(GAMBLING);
    assertThat(gambling.get(ONLINE), is(false));
    assertThat(gambling.get(OFFLINE), is(false));
  }

  @Test
  public void sortMonitoringMap_Should_addDefaultValuesForAllMissingUnsortedProperties() {
    Map<String, Object> sortedMap = MonitoringStructureBuilder.getInstance()
        .addEntry(ALCOHOL, false)
        .addSubLevel(DRUGS, MonitoringStructureBuilder.getInstance()
            .addEntry(CANNABIS, false)
            .addEntry(COCAINE_CRACK, false)
            .addEntry(OTHERS, false)
            .getMonitoringStructure())
        .addEntry(LEGAL_HIGHS, false)
        .addSubLevel(GAMBLING, MonitoringStructureBuilder.getInstance()
            .addEntry(ONLINE, false)
            .addEntry(OFFLINE, false)
            .getMonitoringStructure())
        .getMonitoringStructure();
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), sortedMap);
    doReturn(monitoringDTO).when(monitoringHelper).getMonitoringInitalList(Mockito.any());
    Map<String, Object> unsortedMap = MonitoringStructureBuilder.getInstance()
        .addSubLevel(ADDICTIVE_DRUGS, MonitoringStructureBuilder.getInstance()
            .getMonitoringStructure())
        .getMonitoringStructure();

    Map<String, Object> sortedResultMap = monitoringHelper.sortMonitoringMap(unsortedMap, SUCHT);

    assertThat(sortedResultMap, notNullValue());
    Map<String, Object> rootResult = (Map<String, Object>) sortedResultMap.get(ADDICTIVE_DRUGS);
    assertThat(rootResult.get(ALCOHOL), is(false));
    assertThat(rootResult.get(LEGAL_HIGHS), is(false));
    Map<String, Object> drugs = (Map<String, Object>) rootResult.get(DRUGS);
    assertThat(drugs.get(CANNABIS), is(false));
    assertThat(drugs.get(COCAINE_CRACK), is(false));
    assertThat(drugs.get(OTHERS), is(false));
    Map<String, Object> gambling = (Map<String, Object>) rootResult.get(GAMBLING);
    assertThat(gambling.get(ONLINE), is(false));
    assertThat(gambling.get(OFFLINE), is(false));
  }

}
