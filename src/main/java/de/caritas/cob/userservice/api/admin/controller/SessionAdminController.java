package de.caritas.cob.userservice.api.admin.controller;

import de.caritas.cob.userservice.api.admin.hallink.RootDTOBuilder;
import de.caritas.cob.userservice.api.admin.service.SessionAdminService;
import de.caritas.cob.userservice.api.model.CreateConsultantDTO;
import de.caritas.cob.userservice.api.model.CreateConsultantResponseDTO;
import de.caritas.cob.userservice.api.model.Filter;
import de.caritas.cob.userservice.api.model.RootDTO;
import de.caritas.cob.userservice.api.model.SessionAdminResultDTO;
import de.caritas.cob.userservice.generated.api.admin.controller.UseradminApi;
import io.swagger.annotations.Api;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to handle all session admin requests.
 */
@RestController
@Api(tags = "admin-user-controller")
@RequiredArgsConstructor
public class SessionAdminController implements UseradminApi {

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
   * @param filter The filters to restrict results (optional)
   * @return an entity containing the filtered sessions
   */
  @Override
  public ResponseEntity<SessionAdminResultDTO> getSessions(@NotNull @Valid Integer page,
      @NotNull @Valid Integer perPage, @Valid Filter filter) {
    SessionAdminResultDTO sessionAdminResultDTO = this.sessionAdminService
        .findSessions(page, perPage, filter);
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
}
