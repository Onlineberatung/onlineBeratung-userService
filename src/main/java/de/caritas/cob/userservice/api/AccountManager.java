package de.caritas.cob.userservice.api;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.admin.service.tenant.TenantService;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.Consultant.ConsultantBase;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.in.AccountManaging;
import de.caritas.cob.userservice.api.port.out.ConsultantAgencyRepository;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.port.out.SessionRepository;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import de.caritas.cob.userservice.api.service.agency.AgencyService;
import de.caritas.cob.userservice.api.service.appointment.AppointmentService;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountManager implements AccountManaging {

  private final ConsultantRepository consultantRepository;

  private final UserRepository userRepository;

  private final UserServiceMapper userServiceMapper;

  private final MessageClient messageClient;

  private final UsernameTranscoder usernameTranscoder;

  private final AgencyService agencyService;

  private final TenantService tenantService;

  private final ConsultantAgencyRepository consultantAgencyRepository;

  private final SessionRepository sessionRepository;

  private final AppointmentService appointmentService;

  private final PatchConsultantSaga patchConsultantSaga;

  @Override
  public Optional<Map<String, Object>> findConsultant(String id) {
    var userMap = new HashMap<String, Object>();
    consultantRepository
        .findByIdAndDeleteDateIsNull(id)
        .ifPresent(dbConsultant -> userMap.putAll(findByDbConsultant(dbConsultant)));

    return userMap.isEmpty() ? Optional.empty() : Optional.of(userMap);
  }

  @Override
  public Optional<Map<String, Object>> findConsultantByUsername(String username) {
    var dbConsultantRef = new AtomicReference<Consultant>();
    var transformedUsername = usernameTranscoder.transformedOf(username);

    consultantRepository
        .findByUsernameAndDeleteDateIsNull(username)
        .ifPresentOrElse(
            dbConsultantRef::set,
            () ->
                consultantRepository
                    .findByUsernameAndDeleteDateIsNull(transformedUsername)
                    .ifPresent(dbConsultantRef::set));

    var dbConsultant = dbConsultantRef.get();

    return isNull(dbConsultant) ? Optional.empty() : Optional.of(findByDbConsultant(dbConsultant));
  }

  public Map<String, Object> findConsultantsByInfix(
      String infix,
      boolean shouldFilterByAgencies,
      Collection<Long> agenciesToFilterConsultants,
      int pageNumber,
      int pageSize,
      String fieldName,
      boolean isAscending) {

    var direction = isAscending ? Direction.ASC : Direction.DESC;
    var pageRequest = PageRequest.of(pageNumber, pageSize, direction, fieldName);
    Page<ConsultantBase> consultantPage;
    if (!shouldFilterByAgencies) {
      consultantPage = consultantRepository.findAllByInfix(infix, pageRequest);
    } else {
      consultantPage =
          consultantRepository.findAllByInfixAndAgencyIds(
              infix, agenciesToFilterConsultants, pageRequest);
    }

    var consultantIds =
        consultantPage.stream().map(ConsultantBase::getId).collect(Collectors.toList());
    var fullConsultants = consultantRepository.findAllByIdIn(consultantIds);

    var consultingAgencies = consultantAgencyRepository.findByConsultantIdIn(consultantIds);
    var agencyIds = userServiceMapper.agencyIdsOf(consultingAgencies);
    var agencies = agencyService.getAgenciesWithoutCaching(agencyIds);

    var tenantIdsToNameMap =
        fullConsultants.stream()
            .filter(consultant -> consultant.getTenantId() != null)
            .collect(
                Collectors.toMap(
                    Consultant::getTenantId,
                    consultant ->
                        tenantService.getRestrictedTenantData(consultant.getTenantId()).getName(),
                    (existing, replacement) -> existing));

    return userServiceMapper.mapOf(
        consultantPage, fullConsultants, agencies, consultingAgencies, tenantIdsToNameMap);
  }

  @Override
  public boolean isTeamAdvisedBy(Long sessionId, String consultantId) {
    var session = sessionRepository.findById(sessionId).orElseThrow();

    return session.isTeamSession()
        && consultantAgencyRepository.existsByConsultantIdAndAgencyIdAndDeleteDateIsNull(
            consultantId, session.getAgencyId());
  }

  @Override
  public Optional<Map<String, Object>> patchUser(Map<String, Object> patchMap) {
    var id = (String) patchMap.get("id");
    var userMap = new HashMap<String, Object>();

    userRepository
        .findByUserIdAndDeleteDateIsNull(id)
        .ifPresentOrElse(
            user -> userMap.putAll(patchAdviceSeeker(user, patchMap)),
            () ->
                consultantRepository
                    .findByIdAndDeleteDateIsNull(id)
                    .ifPresent(
                        consultant -> userMap.putAll(patchConsultant(consultant, patchMap))));

    return userMap.isEmpty() ? Optional.empty() : Optional.of(userMap);
  }

  @Override
  public Optional<Map<String, Object>> findAdviceSeeker(String id) {
    var userMap = new HashMap<String, Object>();
    userRepository
        .findByUserIdAndDeleteDateIsNull(id)
        .ifPresent(user -> userMap.putAll(userServiceMapper.mapOf(user)));

    return userMap.isEmpty() ? Optional.empty() : Optional.of(userMap);
  }

  @Override
  public Optional<User> findAdviceSeekerByChatUserId(String chatUserId) {
    return userRepository.findByRcUserIdAndDeleteDateIsNull(chatUserId);
  }

  private Map<String, Object> patchAdviceSeeker(User adviceSeeker, Map<String, Object> patchMap) {
    var patchedAdviceSeeker = userServiceMapper.adviceSeekerOf(adviceSeeker, patchMap);
    var savedAdviceSeeker = userRepository.save(patchedAdviceSeeker);

    return userServiceMapper.mapOf(savedAdviceSeeker);
  }

  private Map<String, Object> patchConsultant(Consultant consultant, Map<String, Object> patchMap) {
    var patchedConsultant = userServiceMapper.consultantOf(consultant, patchMap);
    return patchConsultantSaga.executeTransactional(patchedConsultant, patchMap);
  }

  private Map<String, Object> findByDbConsultant(Consultant dbConsultant) {
    var userMap = new HashMap<String, Object>();

    messageClient
        .findUserAndAddToCache(dbConsultant.getRocketChatId())
        .ifPresentOrElse(
            chatUserMap -> userMap.putAll(userServiceMapper.mapOf(dbConsultant, chatUserMap)),
            throwPersistenceConflict(dbConsultant.getId(), dbConsultant.getRocketChatId()));

    return userMap;
  }

  private Runnable throwPersistenceConflict(String dbUserId, String chatUserId) {
    var message =
        String.format(
            "User (%s) found in database but not in Rocket.Chat (%s)", dbUserId, chatUserId);

    return () -> {
      throw new InternalServerErrorException(message);
    };
  }
}
