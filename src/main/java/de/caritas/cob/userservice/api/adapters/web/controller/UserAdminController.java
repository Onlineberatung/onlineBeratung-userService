package de.caritas.cob.userservice.api.adapters.web.controller;

import com.google.common.collect.Lists;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.AdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyAdminSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyConsultantResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AgencyTypeDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.AskerResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantAgencyResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAdminAgencyRelationDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.CreateConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.RootDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.SessionFilter;
import de.caritas.cob.userservice.api.adapters.web.dto.Sort;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAdminConsultantDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.UpdateAgencyAdminDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.adapters.web.mapping.AdminAgencyDtoMapper;
import de.caritas.cob.userservice.api.admin.facade.AdminAgencyFacade;
import de.caritas.cob.userservice.api.admin.facade.ConsultantAdminFacade;
import de.caritas.cob.userservice.api.admin.facade.UserAdminFacade;
import de.caritas.cob.userservice.api.admin.hallink.RootDTOBuilder;
import de.caritas.cob.userservice.api.admin.report.service.ViolationReportGenerator;
import de.caritas.cob.userservice.api.admin.service.session.SessionAdminService;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import de.caritas.cob.userservice.generated.api.adapters.web.controller.UseradminApi;
import io.swagger.annotations.Api;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Controller to handle all session admin requests. */
@RestController
@RequiredArgsConstructor
@Api(tags = "admin-user-controller")
public class UserAdminController implements UseradminApi {

  private final @NonNull SessionAdminService sessionAdminService;
  private final @NonNull ViolationReportGenerator violationReportGenerator;
  private final @NonNull ConsultantAdminFacade consultantAdminFacade;
  private final @NonNull UserAdminFacade userAdminFacade;
  private final @NonNull AdminAgencyFacade adminAgencyFacade;
  private final @NonNull AppointmentService appointmentService;
  private final @NonNull AdminAgencyDtoMapper adminAgencyDtoMapper;

  /**
   * Creates the root hal based navigation entity.
   *
   * @return an entity containing the available navigation hal links
   */
  @Override
  public ResponseEntity<RootDTO> getRoot() {
    RootDTO rootDTO = new RootDTOBuilder().buildRootDTO();
    return ResponseEntity.ok(rootDTO);
  }

  /**
   * Entry point to retrieve sessions.
   *
   * @param page Number of page where to start in the query (1 = first page) (required)
   * @param perPage Number of items which are being returned (required)
   * @param sessionFilter The filters to restrict results (optional)
   * @return an entity containing the filtered sessions
   */
  @Override
  public ResponseEntity<SessionAdminResultDTO> getSessions(
      @NotNull @Valid Integer page,
      @NotNull @Valid Integer perPage,
      @Valid SessionFilter sessionFilter) {
    SessionAdminResultDTO sessionAdminResultDTO =
        this.sessionAdminService.findSessions(page, perPage, sessionFilter);
    return ResponseEntity.ok(sessionAdminResultDTO);
  }

  /**
   * Entry point to create a new consultant.
   *
   * @param createConsultantDTO (required)
   * @return {@link ConsultantAdminResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantAdminResponseDTO> createConsultant(
      @Valid CreateConsultantDTO createConsultantDTO) {
    return ResponseEntity.ok(this.consultantAdminFacade.createNewConsultant(createConsultantDTO));
  }

  /**
   * GET /useradmin/report : Returns an generated report containing data integration violations.
   * [Authorization: Role: user-admin].
   *
   * @return generated {@link ViolationDTO} list
   */
  @Override
  public ResponseEntity<List<ViolationDTO>> generateViolationReport() {
    return ResponseEntity.ok(this.violationReportGenerator.generateReport());
  }

  /**
   * Entry point to create a new consultant [Authorization: Role: user-admin].
   *
   * @param consultantId Consultant Id (required)
   * @param createConsultantAgencyDTO (required)
   */
  @Override
  public ResponseEntity<Void> createConsultantAgency(
      @PathVariable String consultantId,
      @Valid CreateConsultantAgencyDTO createConsultantAgencyDTO) {
    consultantAdminFacade.checkPermissionsToAssignedAgencies(
        Lists.newArrayList(createConsultantAgencyDTO));
    this.consultantAdminFacade.createNewConsultantAgency(consultantId, createConsultantAgencyDTO);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Void> setConsultantAgencies(
      String consultantId, List<CreateConsultantAgencyDTO> agencyList) {
    var notFilteredAgencyList = new ArrayList<>(agencyList);
    consultantAdminFacade.checkPermissionsToAssignedAgencies(agencyList);
    appointmentService.syncAgencies(consultantId, notFilteredAgencyList);
    var agencyIdsForDeletions =
        consultantAdminFacade.filterAgencyListForDeletion(consultantId, agencyList);
    consultantAdminFacade.markConsultantAgenciesForDeletion(consultantId, agencyIdsForDeletions);
    consultantAdminFacade.filterAgencyListForCreation(consultantId, agencyList);
    consultantAdminFacade.prepareConsultantAgencyRelation(consultantId, agencyList);
    consultantAdminFacade.completeConsultantAgencyAssigment(consultantId, agencyList);
    return ResponseEntity.ok().build();
  }

  /**
   * Entry point to delete a consultant agency relation.
   *
   * @param consultantId Consultant Id (required)
   * @param agencyId Agency Id (required)
   */
  @Override
  public ResponseEntity<Void> deleteConsultantAgency(String consultantId, Long agencyId) {
    this.consultantAdminFacade.markConsultantAgencyForDeletion(consultantId, agencyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Entry point to mark a consultant for deletion.
   *
   * @param consultantId consultant id (required)
   */
  @Override
  public ResponseEntity<Void> markConsultantForDeletion(@PathVariable String consultantId) {
    this.consultantAdminFacade.markConsultantForDeletion(consultantId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Entry point to update a consultant.
   *
   * @param consultantId consultant id (required)
   * @param updateConsultantDTO (required)
   * @return {@link ConsultantAdminResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantAdminResponseDTO> updateConsultant(
      @PathVariable String consultantId, @Valid UpdateAdminConsultantDTO updateConsultantDTO) {
    return ResponseEntity.ok(
        this.consultantAdminFacade.updateConsultant(consultantId, updateConsultantDTO));
  }

  /**
   * Entry point to get a specific consultant.
   *
   * @param consultantId consultant id (required)
   * @return {@link ConsultantAdminResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantAdminResponseDTO> getConsultant(
      @PathVariable String consultantId) {
    ConsultantAdminResponseDTO responseDTO =
        this.consultantAdminFacade.findConsultant(consultantId);
    return ResponseEntity.ok(responseDTO);
  }

  /**
   * Entry point to retrieve consultants.
   *
   * @param page Number of page where to start in the query (1 &#x3D; first page) (required)
   * @param perPage Number of items which are being returned per page (required)
   * @param consultantFilter The filter parameters to search for. If no filter is set all consultant
   *     are being returned. (optional)
   * @return an entity containing the filtered sessions
   */
  @Override
  public ResponseEntity<ConsultantSearchResultDTO> getConsultants(
      @NotNull @Valid Integer page,
      @NotNull @Valid Integer perPage,
      @Valid ConsultantFilter consultantFilter,
      @Valid Sort sort) {
    var resultDTO =
        this.consultantAdminFacade.findFilteredConsultants(page, perPage, consultantFilter, sort);
    return ResponseEntity.ok(resultDTO);
  }

  /**
   * GET /useradmin/agencies/{agencyId}/consultants: Returns all consultants for the agency.
   *
   * @param agencyId Agency Id (required)
   * @return {@link AgencyConsultantResponseDTO}
   */
  @Override
  public ResponseEntity<AgencyConsultantResponseDTO> getAgencyConsultants(String agencyId) {
    var resultDTO = this.consultantAdminFacade.findConsultantsForAgency(agencyId);
    return ResponseEntity.ok(resultDTO);
  }

  /**
   * GET /useradmin/consultant/{consultantId}/agencies: Returns all Agencies for the consultant with
   * given id.
   *
   * @param consultantId Consultant Id (required)
   * @return {@link ConsultantAgencyResponseDTO}s
   */
  @Override
  public ResponseEntity<ConsultantAgencyResponseDTO> getConsultantAgencies(
      @PathVariable String consultantId) {
    var consultantAgencies = this.consultantAdminFacade.findConsultantAgencies(consultantId);
    return ResponseEntity.ok(consultantAgencies);
  }

  /**
   * Entry point to handle consultant data when agency type changes.
   *
   * @param agencyId the id of the changed agency
   * @param agencyTypeDTO contains the target type
   */
  @Override
  public ResponseEntity<Void> changeAgencyType(Long agencyId, @Valid AgencyTypeDTO agencyTypeDTO) {
    this.consultantAdminFacade.changeAgencyType(agencyId, agencyTypeDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  /**
   * Entry point to mark a asker for deletion.
   *
   * @param askerId asker id (required)
   */
  @Override
  public ResponseEntity<Void> markAskerForDeletion(String askerId) {
    this.userAdminFacade.markAskerForDeletion(askerId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<AskerResponseDTO> getAsker(String askerId) {
    AskerResponseDTO response = this.userAdminFacade.getAsker(askerId);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<AdminResponseDTO> createAgencyAdmin(
      final CreateAgencyAdminDTO createAgencyAdminDTO) {
    return ResponseEntity.ok(this.adminAgencyFacade.createNewAdminAgency(createAgencyAdminDTO));
  }

  @Override
  public ResponseEntity<AdminResponseDTO> getAgencyAdmin(final String adminId) {
    return new ResponseEntity<>(this.adminAgencyFacade.findAgencyAdmin(adminId), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<List<Long>> getAdminAgencies(@PathVariable String adminId) {
    var adminAgencies = this.adminAgencyFacade.findAdminUserAgencyIds(adminId);
    return ResponseEntity.ok(adminAgencies);
  }

  @Override
  public ResponseEntity<AgencyAdminSearchResultDTO> getAgencyAdmins(
      final Integer page, final Integer perPage, final AdminFilter filter, final Sort sort) {
    return new ResponseEntity<>(
        this.adminAgencyFacade.findFilteredAdminsAgency(page, perPage, filter, sort),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deleteAgencyAdmin(final String adminId) {
    this.adminAgencyFacade.deleteAgencyAdmin(adminId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<AdminResponseDTO> updateAgencyAdmin(
      final String adminId, final UpdateAgencyAdminDTO updateAgencyAdminDTO) {
    return new ResponseEntity<>(
        this.adminAgencyFacade.updateAgencyAdmin(adminId, updateAgencyAdminDTO), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> createAdminAgencyRelation(
      final String adminId, final CreateAdminAgencyRelationDTO createAdminAgencyRelationDTO) {
    this.adminAgencyFacade.createNewAdminAgencyRelation(adminId, createAdminAgencyRelationDTO);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<Void> deleteAdminAgencyRelation(final String adminId, final Long agencyId) {
    this.adminAgencyFacade.deleteAdminAgencyRelation(adminId, agencyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> setAdminAgenciesRelation(
      final String adminId, final List<CreateAdminAgencyRelationDTO> newAdminAgencyRelationDTOs) {
    this.adminAgencyFacade.setAdminAgenciesRelation(adminId, newAdminAgencyRelationDTOs);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<AgencyAdminSearchResultDTO> searchAgencyAdmins(
      String query, Integer page, Integer perPage, String field, String order) {
    var decodedInfix = URLDecoder.decode(query, StandardCharsets.UTF_8).trim();
    var isAscending = order.equalsIgnoreCase("asc");
    var mappedField = adminAgencyDtoMapper.mappedFieldOf(field);
    var resultMap =
        adminAgencyFacade.findAgencyAdminsByInfix(
            decodedInfix, page - 1, perPage, mappedField, isAscending);
    var result =
        adminAgencyDtoMapper.agencyAdminSearchResultOf(
            resultMap, query, page, perPage, field, order);

    return ResponseEntity.ok(result);
  }
}
