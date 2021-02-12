package de.caritas.cob.userservice.api.admin.controller;

import de.caritas.cob.userservice.api.admin.facade.ConsultantAdminFacade;
import de.caritas.cob.userservice.api.admin.hallink.RootDTOBuilder;
import de.caritas.cob.userservice.api.admin.report.service.ViolationReportGenerator;
import de.caritas.cob.userservice.api.admin.service.ConsultingTypeAdminService;
import de.caritas.cob.userservice.api.admin.service.session.SessionAdminService;
import de.caritas.cob.userservice.api.model.AgencyTypeDTO;
import de.caritas.cob.userservice.api.model.ConsultantAdminResponseDTO;
import de.caritas.cob.userservice.api.model.ConsultantAgencyAdminResultDTO;
import de.caritas.cob.userservice.api.model.ConsultantFilter;
import de.caritas.cob.userservice.api.model.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.model.ConsultingTypeAdminResultDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantAgencyDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.RootDTO;
import de.caritas.cob.userservice.api.model.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.model.SessionFilter;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.ViolationDTO;
import de.caritas.cob.userservice.generated.api.admin.controller.UseradminApi;
import io.swagger.annotations.Api;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle all session admin requests.
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "admin-user-controller")
public class UserAdminController implements UseradminApi {

  private final @NonNull SessionAdminService sessionAdminService;
  private final @NonNull ConsultingTypeAdminService consultingTypeAdminService;
  private final @NonNull ViolationReportGenerator violationReportGenerator;
  private final @NonNull ConsultantAdminFacade consultantAdminFacade;

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
   * @param page          Number of page where to start in the query (1 = first page) (required)
   * @param perPage       Number of items which are being returned (required)
   * @param sessionFilter The filters to restrict results (optional)
   * @return an entity containing the filtered sessions
   */
  @Override
  public ResponseEntity<SessionAdminResultDTO> getSessions(@NotNull @Valid Integer page,
      @NotNull @Valid Integer perPage, @Valid SessionFilter sessionFilter) {
    SessionAdminResultDTO sessionAdminResultDTO = this.sessionAdminService
        .findSessions(page, perPage, sessionFilter);
    return ResponseEntity.ok(sessionAdminResultDTO);
  }

  /**
   * Entry point to retrieve all consulting types.
   *
   * @param page    Number of page where to start in the query (1 = first page) (required)
   * @param perPage Number of items which are being returned per page (required)
   * @return an entity containing the consulting types as {@link ConsultingTypeAdminResultDTO}
   */
  @Override
  public ResponseEntity<ConsultingTypeAdminResultDTO> getConsultingTypes(
      @NotNull @Valid Integer page, @NotNull @Valid Integer perPage) {
    ConsultingTypeAdminResultDTO consultingTypeAdminResultDTO = this.consultingTypeAdminService
        .findConsultingTypes(page, perPage);

    return ResponseEntity.ok(consultingTypeAdminResultDTO);
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
   * @param consultantId              Consultant Id (required)
   * @param createConsultantAgencyDTO (required)
   */
  @Override
  public ResponseEntity<Void> createConsultantAgency(@PathVariable String consultantId,
      @Valid CreateConsultantAgencyDTO createConsultantAgencyDTO) {
    this.consultantAdminFacade.createNewConsultantAgency(consultantId, createConsultantAgencyDTO);
    return new ResponseEntity<>(HttpStatus.CREATED);
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
    return null;
  }

  /**
   * Entry point to update a consultant.
   *
   * @param consultantId        consultant id (required)
   * @param updateConsultantDTO (required)
   * @return {@link ConsultantAdminResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantAdminResponseDTO> updateConsultant(
      @PathVariable String consultantId, @Valid UpdateConsultantDTO updateConsultantDTO) {
    return ResponseEntity
        .ok(this.consultantAdminFacade.updateConsultant(consultantId, updateConsultantDTO));
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
    ConsultantAdminResponseDTO responseDTO = this.consultantAdminFacade
        .findConsultant(consultantId);
    return ResponseEntity.ok(responseDTO);
  }

  /**
   * Entry point to retrieve consultants.
   *
   * @param page             Number of page where to start in the query (1 &#x3D; first page)
   *                         (required)
   * @param perPage          Number of items which are being returned per page (required)
   * @param consultantFilter The filter parameters to search for. If no filter is set all consultant
   *                         are being returned. (optional)
   * @return an entity containing the filtered sessions
   */
  @Override
  public ResponseEntity<ConsultantSearchResultDTO> getConsultants(@NotNull @Valid Integer page,
      @NotNull @Valid Integer perPage, @Valid ConsultantFilter consultantFilter) {
    ConsultantSearchResultDTO resultDTO =
        this.consultantAdminFacade.findFilteredConsultants(page, perPage, consultantFilter);
    return ResponseEntity.ok(resultDTO);
  }

  /**
   * GET /useradmin/consultant/{consultantId}/agencies: Returns all Agencies.
   *
   * @param consultantId Consultant Id (required)
   * @return {@link ConsultantAdminResponseDTO}
   */
  @Override
  public ResponseEntity<ConsultantAgencyAdminResultDTO> getConsultantAgency(
      @PathVariable String consultantId) {
    ConsultantAgencyAdminResultDTO consultantAgencies = this.consultantAdminFacade
        .findConsultantAgencies(consultantId);
    return ResponseEntity.ok(consultantAgencies);
  }

  /**
   * Entry point to handle consultant data when agency type changes.
   *
   * @param agencyId      the id of the changed agency
   * @param agencyTypeDTO contains the target type
   */
  @Override
  public ResponseEntity<Void> changeAgencyType(Long agencyId, @Valid AgencyTypeDTO agencyTypeDTO) {
    this.consultantAdminFacade.changeAgencyType(agencyId, agencyTypeDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
