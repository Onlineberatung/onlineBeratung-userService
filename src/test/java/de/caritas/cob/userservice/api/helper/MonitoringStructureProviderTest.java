package de.caritas.cob.userservice.api.helper;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.CONSULTING_TYPE_ID_SUCHT;
import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.adapters.web.dto.MonitoringDTO;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManager;
import de.caritas.cob.userservice.api.model.Monitoring;
import de.caritas.cob.userservice.api.model.Monitoring.MonitoringType;
import de.caritas.cob.userservice.api.model.MonitoringOption;
import de.caritas.cob.userservice.consultingtypeservice.generated.web.model.ExtendedConsultingTypeResponseDTO;
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
public class MonitoringStructureProviderTest {

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
  private static final String CONSULTING = "consulting";
  private static final String CONVEYANCE = "conveyance";
  private static final String SELF_HELP = "selfHelp";
  private static final String INFORMATION = "information";

  @Spy @InjectMocks private MonitoringStructureProvider monitoringStructureProvider;

  @Mock private ConsultingTypeManager consultingTypeManager;

  private final List<Monitoring> SORTED_MONITORING_LIST = new ArrayList<>();
  private final List<Monitoring> UNSORTED_MONITORING_LIST = new ArrayList<>();
  private final List<MonitoringOption> DRUGS_MONITORING_OPTION_LIST = new ArrayList<>();
  private final Monitoring ALCOHOL_MONITORING =
      new Monitoring(SESSION_ID, MonitoringType.ADDICTIVE_DRUGS, ALCOHOL, true);
  private final Monitoring DRUGS_MONITORING =
      new Monitoring(
          SESSION_ID, MonitoringType.ADDICTIVE_DRUGS, DRUGS, null, DRUGS_MONITORING_OPTION_LIST);
  private final MonitoringOption OTHERS_MONITORING_OPTION =
      new MonitoringOption(
          SESSION_ID, MonitoringType.ADDICTIVE_DRUGS, DRUGS, OTHERS, false, DRUGS_MONITORING);

  @Before
  public void setUp() {
    LinkedHashMap<String, Object> drugsMap = new LinkedHashMap<String, Object>();
    drugsMap.put(OTHERS, false);
    LinkedHashMap<String, Object> sortedAddictiveDrugsMap = new LinkedHashMap<String, Object>();
    sortedAddictiveDrugsMap.put(ALCOHOL, true);
    sortedAddictiveDrugsMap.put(DRUGS, drugsMap);
    SORTED_MONITORING_DTO.addProperties(
        MonitoringType.ADDICTIVE_DRUGS.getKey(), sortedAddictiveDrugsMap);

    DRUGS_MONITORING_OPTION_LIST.add(OTHERS_MONITORING_OPTION);
    SORTED_MONITORING_LIST.add(ALCOHOL_MONITORING);
    SORTED_MONITORING_LIST.add(DRUGS_MONITORING);

    UNSORTED_MONITORING_LIST.add(DRUGS_MONITORING);
    UNSORTED_MONITORING_LIST.add(ALCOHOL_MONITORING);
  }

  @Test
  public void createMonitoringList_Should_ReturnCorrectMonitoringList_WhenCalled() {

    List<Monitoring> monitoringList =
        monitoringStructureProvider.createMonitoringList(SORTED_MONITORING_DTO, SESSION_ID);

    assertEquals(
        monitoringList.get(0).getMonitoringOptionList(),
        SORTED_MONITORING_LIST.get(0).getMonitoringOptionList());
  }

  @Test
  public void createMonitoringList_should_returnEmptyList_When_monitoringDtoIsNull() {
    List<Monitoring> monitoringList = monitoringStructureProvider.createMonitoringList(null, null);

    assertThat(monitoringList, hasSize(0));
  }

  @Test
  public void createMonitoringList_should_returnEmptyList_When_monitoringDtoIsEmpty() {
    List<Monitoring> monitoringList =
        monitoringStructureProvider.createMonitoringList(new MonitoringDTO(), 1L);

    assertThat(monitoringList, hasSize(0));
  }

  @Test
  public void createMonitoringList_should_returnEmptyList_When_rootEntryValueIsNotAMap() {
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), false);
    List<Monitoring> monitoringList =
        monitoringStructureProvider.createMonitoringList(monitoringDTO, 1L);

    assertThat(monitoringList, hasSize(0));
  }

  @Test
  public void createMonitoringList_should_returnEmptyList_When_secondLevelMapIsEmptyp() {
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), emptyMap());
    List<Monitoring> monitoringList =
        monitoringStructureProvider.createMonitoringList(monitoringDTO, 1L);

    assertThat(monitoringList, hasSize(0));
  }

  @Test
  public void createMonitoringList_Should_ReturnCorrectComplexMonitoringHirarchy_When_Called() {
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    Map<String, Object> addictiveDrugsMap =
        MonitoringStructureBuilder.getInstance()
            .addEntry(ALCOHOL, true)
            .addSubLevel(
                DRUGS,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(CANNABIS, true)
                    .addEntry(COCAINE_CRACK, true)
                    .addEntry(OTHERS, false)
                    .getMonitoringStructure())
            .addEntry(LEGAL_HIGHS, false)
            .addSubLevel(
                GAMBLING,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(ONLINE, true)
                    .addEntry(OFFLINE, false)
                    .getMonitoringStructure())
            .getMonitoringStructure();
    Map<String, Object> interventionMap =
        MonitoringStructureBuilder.getInstance()
            .addEntry(CONSULTING, false)
            .addSubLevel(
                CONVEYANCE,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(SELF_HELP, true)
                    .addEntry(OTHERS, false)
                    .getMonitoringStructure())
            .addEntry(INFORMATION, false)
            .getMonitoringStructure();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), addictiveDrugsMap);
    monitoringDTO.addProperties(MonitoringType.INTERVENTION.getKey(), interventionMap);

    List<Monitoring> monitoringList =
        monitoringStructureProvider.createMonitoringList(monitoringDTO, SESSION_ID);

    assertThat(monitoringList, hasSize(7));
    assertDirectTypesOnIndex(monitoringList, 0, MonitoringType.ADDICTIVE_DRUGS, ALCOHOL, true);
    assertThat(monitoringList.get(1).getMonitoringType(), is(MonitoringType.ADDICTIVE_DRUGS));
    assertThat(monitoringList.get(1).getMonitoringOptionList(), hasSize(3));
    assertKeyValuePair(monitoringList, 1, 0, CANNABIS, true);
    assertKeyValuePair(monitoringList, 1, 1, COCAINE_CRACK, true);
    assertKeyValuePair(monitoringList, 1, 2, OTHERS, false);
    assertThat(monitoringList.get(1).getKey(), is(DRUGS));
    assertThat(monitoringList.get(1).getValue(), nullValue());
    assertDirectTypesOnIndex(monitoringList, 2, MonitoringType.ADDICTIVE_DRUGS, LEGAL_HIGHS, false);
    assertThat(monitoringList.get(3).getMonitoringType(), is(MonitoringType.ADDICTIVE_DRUGS));
    assertThat(monitoringList.get(3).getMonitoringOptionList(), hasSize(2));
    assertKeyValuePair(monitoringList, 3, 0, ONLINE, true);
    assertKeyValuePair(monitoringList, 3, 1, OFFLINE, false);
    assertThat(monitoringList.get(3).getKey(), is(GAMBLING));
    assertThat(monitoringList.get(3).getValue(), nullValue());
    assertDirectTypesOnIndex(monitoringList, 4, MonitoringType.INTERVENTION, CONSULTING, false);
    assertThat(monitoringList.get(5).getMonitoringType(), is(MonitoringType.INTERVENTION));
    assertThat(monitoringList.get(5).getMonitoringOptionList(), hasSize(2));
    assertKeyValuePair(monitoringList, 5, 0, SELF_HELP, true);
    assertKeyValuePair(monitoringList, 5, 1, OTHERS, false);
    assertThat(monitoringList.get(5).getKey(), is(CONVEYANCE));
    assertThat(monitoringList.get(5).getValue(), nullValue());
    assertDirectTypesOnIndex(monitoringList, 6, MonitoringType.INTERVENTION, INFORMATION, false);
  }

  private void assertDirectTypesOnIndex(
      List<Monitoring> monitoringList,
      int index,
      MonitoringType monitoringType,
      String expectedKey,
      boolean expectedvalue) {
    assertThat(monitoringList.get(index).getMonitoringType(), is(monitoringType));
    assertThat(monitoringList.get(index).getMonitoringOptionList(), nullValue());
    assertThat(monitoringList.get(index).getKey(), is(expectedKey));
    assertThat(monitoringList.get(index).getValue(), is(expectedvalue));
  }

  private void assertKeyValuePair(
      List<Monitoring> monitoringList,
      int firstLevelIndex,
      int secondLevelIndex,
      String expectedKey,
      boolean expectedValue) {
    assertThat(
        monitoringList
            .get(firstLevelIndex)
            .getMonitoringOptionList()
            .get(secondLevelIndex)
            .getKey(),
        is(expectedKey));
    assertThat(
        monitoringList
            .get(firstLevelIndex)
            .getMonitoringOptionList()
            .get(secondLevelIndex)
            .getValue(),
        is(expectedValue));
  }

  @Test
  public void
      getMonitoringInitialList_Should_returnExpectedMonitoring_When_consultingTypeHAsMonitoring() {
    ExtendedConsultingTypeResponseDTO settings = mock(ExtendedConsultingTypeResponseDTO.class);
    var monitoringDTO =
        new de.caritas.cob.userservice.consultingtypeservice.generated.web.model.MonitoringDTO();
    monitoringDTO.setMonitoringTemplateFile("/monitoring/sucht.json");
    when(settings.getMonitoring()).thenReturn(monitoringDTO);
    when(this.consultingTypeManager.getConsultingTypeSettings(anyInt())).thenReturn(settings);

    MonitoringDTO monitoringInitalList =
        this.monitoringStructureProvider.getMonitoringInitialList(0);

    assertThat(monitoringInitalList, notNullValue());
    assertThat(monitoringInitalList.getProperties().entrySet(), hasSize(2));
  }

  @Test(expected = InternalServerErrorException.class)
  public void
      getMonitoringInitialList_Should_throwInternalServerErrorException_When_monitoringFilePathIsNull() {
    ExtendedConsultingTypeResponseDTO settings = mock(ExtendedConsultingTypeResponseDTO.class);
    var monitoringDTO =
        new de.caritas.cob.userservice.consultingtypeservice.generated.web.model.MonitoringDTO();
    monitoringDTO.setMonitoringTemplateFile(null);
    when(settings.getMonitoring()).thenReturn(monitoringDTO);
    when(this.consultingTypeManager.getConsultingTypeSettings(anyInt())).thenReturn(settings);

    this.monitoringStructureProvider.getMonitoringInitialList(0);
  }

  @Test
  public void sortMonitoringMap_Should_ReturnCorrectlySortedMap_WhenCalled() {
    doReturn(SORTED_MONITORING_DTO)
        .when(monitoringStructureProvider)
        .getMonitoringInitialList(anyInt());

    LinkedHashMap<String, Object> drugsMap = new LinkedHashMap<String, Object>();
    drugsMap.put(OTHERS, false);
    LinkedHashMap<String, Object> unsortedAddictiveDrugsMap = new LinkedHashMap<String, Object>();
    unsortedAddictiveDrugsMap.put(DRUGS, drugsMap);
    unsortedAddictiveDrugsMap.put(ALCOHOL, true);
    LinkedHashMap<String, Object> unsortedMap = new LinkedHashMap<String, Object>();
    unsortedMap.put(MonitoringType.ADDICTIVE_DRUGS.getKey(), unsortedAddictiveDrugsMap);

    Map<String, Object> sortedMap = monitoringStructureProvider.sortMonitoringMap(unsortedMap, 0);

    assertEquals(
        sortedMap.get(String.valueOf(CONSULTING_TYPE_ID_SUCHT)),
        SORTED_MONITORING_LIST.get(0).getMonitoringOptionList());
  }

  @Test
  public void
      sortMonitoringMap_Should_removeEntryInSortedMap_When_entryDoesNotExistInChildLevelUnsortedMap() {
    Map<String, Object> sortedMap =
        MonitoringStructureBuilder.getInstance()
            .addEntry(ALCOHOL, false)
            .addSubLevel(
                DRUGS,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(OTHERS, false)
                    .getMonitoringStructure())
            .getMonitoringStructure();
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), sortedMap);
    doReturn(monitoringDTO)
        .when(monitoringStructureProvider)
        .getMonitoringInitialList(Mockito.anyInt());
    Map<String, Object> unsortedMap =
        MonitoringStructureBuilder.getInstance()
            .addSubLevel(
                ADDICTIVE_DRUGS,
                MonitoringStructureBuilder.getInstance()
                    .addSubLevel(DRUGS, new LinkedHashMap<>())
                    .addEntry(ALCOHOL, true)
                    .getMonitoringStructure())
            .getMonitoringStructure();

    Map<String, Object> sortedResultMap =
        monitoringStructureProvider.sortMonitoringMap(unsortedMap, 0);

    assertThat(sortedResultMap, notNullValue());
    Map<String, Object> rootResult = (Map<String, Object>) sortedResultMap.get(ADDICTIVE_DRUGS);
    assertThat(rootResult.get(ALCOHOL), is(true));
    Map<String, Object> drugs = (Map<String, Object>) rootResult.get(DRUGS);
    assertThat(drugs.get(OTHERS), is(false));
  }

  @Test
  public void sortMonitoringMap_Should_preferValuesOfJsonMap_When_entryHasOtherStructureElement() {
    Map<String, Object> sortedMap =
        MonitoringStructureBuilder.getInstance()
            .addEntry(ALCOHOL, false)
            .addSubLevel(
                DRUGS,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(OTHERS, false)
                    .getMonitoringStructure())
            .getMonitoringStructure();
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), sortedMap);
    doReturn(monitoringDTO)
        .when(monitoringStructureProvider)
        .getMonitoringInitialList(Mockito.anyInt());
    Map<String, Object> unsortedMap =
        MonitoringStructureBuilder.getInstance()
            .addSubLevel(
                ADDICTIVE_DRUGS,
                MonitoringStructureBuilder.getInstance()
                    .addSubLevel(
                        DRUGS,
                        MonitoringStructureBuilder.getInstance()
                            .addEntry(CANNABIS, true)
                            .getMonitoringStructure())
                    .getMonitoringStructure())
            .getMonitoringStructure();

    Map<String, Object> sortedResultMap =
        monitoringStructureProvider.sortMonitoringMap(unsortedMap, 0);

    assertThat(sortedResultMap, notNullValue());
    Map<String, Object> rootResult = (Map<String, Object>) sortedResultMap.get(ADDICTIVE_DRUGS);
    assertThat(rootResult.get(ALCOHOL), is(false));
    Map<String, Object> drugs = (Map<String, Object>) rootResult.get(DRUGS);
    assertThat(drugs.get(OTHERS), is(false));
  }

  @Test
  public void
      sortMonitoringMap_Should_addDefaultValueForMissingUnsortedProperty_When_monitoringStructureHasEntriesOnChildLevelWhichAreNotAvailableInUnsortedMap() {
    Map<String, Object> sortedMap =
        MonitoringStructureBuilder.getInstance()
            .addEntry(ALCOHOL, false)
            .addSubLevel(
                DRUGS,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(CANNABIS, false)
                    .addEntry(COCAINE_CRACK, false)
                    .addEntry(OTHERS, false)
                    .getMonitoringStructure())
            .addEntry(LEGAL_HIGHS, false)
            .addSubLevel(
                GAMBLING,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(ONLINE, false)
                    .addEntry(OFFLINE, false)
                    .getMonitoringStructure())
            .getMonitoringStructure();
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), sortedMap);
    doReturn(monitoringDTO)
        .when(monitoringStructureProvider)
        .getMonitoringInitialList(Mockito.anyInt());
    Map<String, Object> unsortedMap =
        MonitoringStructureBuilder.getInstance()
            .addSubLevel(
                ADDICTIVE_DRUGS,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(LEGAL_HIGHS, true)
                    .addSubLevel(
                        DRUGS,
                        MonitoringStructureBuilder.getInstance()
                            .addEntry(COCAINE_CRACK, true)
                            .addEntry(OTHERS, false)
                            .addEntry(CANNABIS, true)
                            .getMonitoringStructure())
                    .addEntry(ALCOHOL, false)
                    .getMonitoringStructure())
            .getMonitoringStructure();

    Map<String, Object> sortedResultMap =
        monitoringStructureProvider.sortMonitoringMap(unsortedMap, 0);

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
    Map<String, Object> sortedMap =
        MonitoringStructureBuilder.getInstance()
            .addEntry(ALCOHOL, false)
            .addSubLevel(
                DRUGS,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(CANNABIS, false)
                    .addEntry(COCAINE_CRACK, false)
                    .addEntry(OTHERS, false)
                    .getMonitoringStructure())
            .addEntry(LEGAL_HIGHS, false)
            .addSubLevel(
                GAMBLING,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(ONLINE, false)
                    .addEntry(OFFLINE, false)
                    .getMonitoringStructure())
            .getMonitoringStructure();
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), sortedMap);
    doReturn(monitoringDTO)
        .when(monitoringStructureProvider)
        .getMonitoringInitialList(Mockito.anyInt());
    Map<String, Object> unsortedMap =
        MonitoringStructureBuilder.getInstance()
            .addSubLevel(
                ADDICTIVE_DRUGS, MonitoringStructureBuilder.getInstance().getMonitoringStructure())
            .getMonitoringStructure();

    Map<String, Object> sortedResultMap =
        monitoringStructureProvider.sortMonitoringMap(unsortedMap, 0);

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

  @Test
  public void sortMonitoringMap_Should_setValuesOfSameOtherKeyDependingOnTheirStructurePosition() {
    Map<String, Object> sortedMap =
        MonitoringStructureBuilder.getInstance()
            .addSubLevel(
                DRUGS,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(OTHERS, false)
                    .getMonitoringStructure())
            .addEntry(OTHERS, false)
            .addSubLevel(
                GAMBLING,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(OTHERS, false)
                    .getMonitoringStructure())
            .addSubLevel(
                LEGAL_HIGHS,
                MonitoringStructureBuilder.getInstance()
                    .addEntry(OTHERS, false)
                    .getMonitoringStructure())
            .getMonitoringStructure();
    MonitoringDTO monitoringDTO = new MonitoringDTO();
    monitoringDTO.addProperties(MonitoringType.ADDICTIVE_DRUGS.getKey(), sortedMap);
    doReturn(monitoringDTO)
        .when(monitoringStructureProvider)
        .getMonitoringInitialList(Mockito.anyInt());
    Map<String, Object> unsortedMap =
        MonitoringStructureBuilder.getInstance()
            .addSubLevel(
                ADDICTIVE_DRUGS,
                MonitoringStructureBuilder.getInstance()
                    .addSubLevel(
                        DRUGS,
                        MonitoringStructureBuilder.getInstance()
                            .addEntry(OTHERS, true)
                            .getMonitoringStructure())
                    .addEntry(OTHERS, true)
                    .addSubLevel(
                        GAMBLING,
                        MonitoringStructureBuilder.getInstance()
                            .addEntry(OTHERS, false)
                            .getMonitoringStructure())
                    .addSubLevel(
                        LEGAL_HIGHS,
                        MonitoringStructureBuilder.getInstance()
                            .addEntry(OTHERS, true)
                            .getMonitoringStructure())
                    .getMonitoringStructure())
            .getMonitoringStructure();

    Map<String, Object> sortedResultMap =
        monitoringStructureProvider.sortMonitoringMap(unsortedMap, 0);

    assertThat(sortedResultMap, notNullValue());
    Map<String, Object> rootResult = (Map<String, Object>) sortedResultMap.get(ADDICTIVE_DRUGS);
    assertThat(rootResult.get(OTHERS), is(true));
    Map<String, Object> drugs = (Map<String, Object>) rootResult.get(DRUGS);
    assertThat(drugs.get(OTHERS), is(true));
    Map<String, Object> gambling = (Map<String, Object>) rootResult.get(GAMBLING);
    assertThat(gambling.get(OTHERS), is(false));
    Map<String, Object> legalHighs = (Map<String, Object>) rootResult.get(LEGAL_HIGHS);
    assertThat(legalHighs.get(OTHERS), is(true));
  }
}
