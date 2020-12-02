package de.caritas.cob.userservice.api.admin.controller;

import de.caritas.cob.userservice.api.admin.hallink.RootDTOBuilder;
import de.caritas.cob.userservice.api.admin.service.SessionAdminService;
import de.caritas.cob.userservice.api.model.ConsultantFilter;
import de.caritas.cob.userservice.api.model.ConsultantSearchResultDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.GetConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.RootDTO;
import de.caritas.cob.userservice.api.model.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.model.SessionFilter;
import de.caritas.cob.userservice.api.model.UpdateConsultantDTO;
import de.caritas.cob.userservice.api.model.UpdateConsultantResponseDTO;
import de.caritas.cob.userservice.generated.api.admin.controller.UseradminApi;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle all session admin requests.
 */
@RestController
@Api(tags = "admin-user-controller")
@RequiredArgsConstructor
public class UserAdminController implements UseradminApi {

  private final @NonNull SessionAdminService sessionAdminService;

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
  public ResponseEntity<SessionAdminResultDTO> getSessions(@NotNull @Valid Integer page,
      @NotNull @Valid Integer perPage, @Valid SessionFilter sessionFilter) {
    SessionAdminResultDTO sessionAdminResultDTO = this.sessionAdminService
        .findSessions(page, perPage, sessionFilter);
    return ResponseEntity.ok(sessionAdminResultDTO);
  }

  /**
   * Entry point to create a new consultant.
   *
   * @param createConsultantDTO  (required)
   * @return {@link CreateConsultantResponseDTO}
   */
  @Override
  public ResponseEntity<CreateConsultantResponseDTO> createConsultant(
      @Valid CreateConsultantDTO createConsultantDTO) {
    return null;
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
   * @param consultantId consultant id (required)
   * @param updateConsultantDTO  (required)
   * @return {@link UpdateConsultantResponseDTO}
   */
  @Override
  public ResponseEntity<UpdateConsultantResponseDTO> updateConsultant(@PathVariable  String consultantId,
      @Valid UpdateConsultantDTO updateConsultantDTO) {
    return null;
  }

  /**
   * Entry point to get a specific consultant.
   *
   * @param consultantId consultant id (required)
   * @return {@link GetConsultantResponseDTO}
   */
  @Override
  public ResponseEntity<GetConsultantResponseDTO> getConsultant(String consultantId) {
    return null;
  }

  /**
   * Entry point to retrieve consultants.
   *
   * @param page Number of page where to start in the query (1 &#x3D; first page) (required)
   * @param perPage Number of items which are being returned per page (required)
   * @param consultantFilter The filter parameters to search for. If no filter is set all consultant are being returned. (optional)
   * @return an entity containing the filtered sessions
   */
  @Override
  public ResponseEntity<ConsultantSearchResultDTO> getConsultants(@NotNull @Valid Integer page,
      @NotNull @Valid Integer perPage, @Valid ConsultantFilter consultantFilter) {
    return null;
  }
}
