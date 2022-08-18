package de.caritas.cob.userservice.api.admin.report.service;

import static de.caritas.cob.userservice.api.helper.CustomLocalDateTime.nowInUtc;
import static java.time.format.DateTimeFormatter.ofPattern;

import de.caritas.cob.userservice.agencyadminserivce.generated.web.model.AgencyAdminResponseDTO;
import de.caritas.cob.userservice.api.adapters.web.dto.ViolationDTO;
import de.caritas.cob.userservice.api.admin.report.model.ViolationReportRule;
import de.caritas.cob.userservice.api.admin.report.registry.ViolationRuleRegistry;
import de.caritas.cob.userservice.api.admin.service.agency.AgencyAdminService;
import io.swagger.util.Json;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

/** Generator for all {@link ViolationReportRule} beans. */
@Service
@RequiredArgsConstructor
public class ViolationReportGenerator {

  private static final String VIOLATION_REPORT_BASE_PATH = "report/violation_report_";
  private static final DateTimeFormatter DATE_TIME_FORMAT = ofPattern("yyyy-MM-dd--hh-mm");

  private final @NonNull ViolationRuleRegistry violationRuleRegistry;
  private final @NonNull AgencyAdminService agencyAdminService;

  /**
   * Generates a list of all located known violations.
   *
   * @return all found {@link ViolationDTO} objects
   */
  @SneakyThrows
  public List<ViolationDTO> generateReport() {
    List<AgencyAdminResponseDTO> allAgencies = this.agencyAdminService.retrieveAllAgencies();
    List<ViolationDTO> violations =
        this.violationRuleRegistry.getViolationReportRules(allAgencies).stream()
            .map(ViolationReportRule::generateViolations)
            .flatMap(Collection::parallelStream)
            .collect(Collectors.toList());

    String violationJson = Json.pretty().writeValueAsString(violations);
    Files.write(
        buildFilePath(), violationJson.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

    return violations;
  }

  private Path buildFilePath() throws IOException {
    String path = VIOLATION_REPORT_BASE_PATH + nowInUtc().format(DATE_TIME_FORMAT) + ".json";
    if (!Paths.get(path).getParent().toFile().exists()) {
      Files.createDirectory(Paths.get(path).getParent());
    }
    return Paths.get(path);
  }
}
