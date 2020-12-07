package de.caritas.cob.userservice.api.admin.controller;

import de.caritas.cob.userservice.api.admin.hallink.RootDTOBuilder;
import de.caritas.cob.userservice.api.admin.service.ConsultingTypeAdminService;
import de.caritas.cob.userservice.api.admin.report.service.ViolationReportGenerator;
import de.caritas.cob.userservice.api.admin.service.SessionAdminService;
import de.caritas.cob.userservice.api.model.ConsultingTypeAdminResultDTO;
import de.caritas.cob.userservice.api.model.Filter;
import de.caritas.cob.userservice.api.model.RootDTO;
import de.caritas.cob.userservice.api.model.SessionAdminResultDTO;
import de.caritas.cob.userservice.api.model.ViolationDTO;
import de.caritas.cob.userservice.generated.api.admin.controller.UseradminApi;
import io.swagger.annotations.Api;
import java.util.List;
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
@RequiredArgsConstructor
@Api(tags = "admin-user-controller")
public class UserAdminController implements UseradminApi {

  private final @NonNull SessionAdminService sessionAdminService;
  private final @NonNull ConsultingTypeAdminService consultingTypeAdminService;
  private final @NonNull ViolationReportGenerator violationReportGenerator;

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
   * GET /useradmin/report : Returns an generated report containing data integration violations.
   * [Authorization: Role: user-admin].
   *
   * @return OK - successfull operation (status code 200) or UNAUTHORIZED - no/invalid
   * role/authorization (status code 401) or INTERNAL SERVER ERROR - server encountered unexpected
   * condition (status code 500)
   */
  @Override
  public ResponseEntity<List<ViolationDTO>> generateViolationReport() {
    return ResponseEntity.ok(this.violationReportGenerator.generateReport());
  }
}
