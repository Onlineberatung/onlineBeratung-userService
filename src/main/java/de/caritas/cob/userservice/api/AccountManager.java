package de.caritas.cob.userservice.api;

import static java.util.Objects.isNull;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.helper.UsernameTranscoder;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.in.AccountManaging;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Override
  public Optional<Map<String, Object>> findConsultant(String id) {
    var userMap = new HashMap<String, Object>();
    consultantRepository.findByIdAndDeleteDateIsNull(id).ifPresent(dbConsultant ->
        userMap.putAll(findByDbConsultant(dbConsultant))
    );

    return userMap.isEmpty() ? Optional.empty() : Optional.of(userMap);
  }

  @Override
  public Optional<Map<String, Object>> findConsultantByUsername(String username) {
    var dbConsultantRef = new AtomicReference<Consultant>();
    var transformedUsername = usernameTranscoder.transformedOf(username);

    consultantRepository.findByUsernameAndDeleteDateIsNull(username)
        .ifPresentOrElse(dbConsultantRef::set, () ->
            consultantRepository.findByUsernameAndDeleteDateIsNull(transformedUsername)
                .ifPresent(dbConsultantRef::set)
        );

    var dbConsultant = dbConsultantRef.get();

    return isNull(dbConsultant)
        ? Optional.empty()
        : Optional.of(findByDbConsultant(dbConsultant));
  }

  public Map<String, Object> findConsultantsByInfix(
      String infix, int pageNumber, int pageSize, String fieldName, boolean isAscending) {

    var direction = isAscending ? Direction.ASC : Direction.DESC;
    var pageRequest = PageRequest.of(pageNumber, pageSize, direction, fieldName);
    var consultantPage = consultantRepository.findAllByInfix(infix, pageRequest);

    return userServiceMapper.mapOf(consultantPage);
  }

  @Override
  public Optional<Map<String, Object>> patchUser(Map<String, Object> patchMap) {
    var id = (String) patchMap.get("id");
    var userMap = new HashMap<String, Object>();

    userRepository.findByUserIdAndDeleteDateIsNull(id).ifPresentOrElse(
        user -> userMap.putAll(patchAdviceSeeker(user, patchMap)),
        () -> consultantRepository.findByIdAndDeleteDateIsNull(id).ifPresent(
            consultant -> userMap.putAll(patchConsultant(consultant, patchMap))
        )
    );

    return userMap.isEmpty() ? Optional.empty() : Optional.of(userMap);
  }

  @Override
  public boolean existsAdviceSeeker(String id) {
    return findAdviceSeeker(id).isPresent();
  }

  @Override
  public Optional<User> findAdviceSeeker(String id) {
    return userRepository.findByUserIdAndDeleteDateIsNull(id);
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
    var savedConsultant = consultantRepository.save(patchedConsultant);

    userServiceMapper.displayNameOf(patchMap).ifPresent(displayName ->
        messageClient.updateUser(savedConsultant.getRocketChatId(), displayName)
    );

    return userServiceMapper.mapOf(savedConsultant, patchMap);
  }

  private Map<String, Object> findByDbConsultant(Consultant dbConsultant) {
    var userMap = new HashMap<String, Object>();

    messageClient.findUser(dbConsultant.getRocketChatId()).ifPresentOrElse(
        chatUserMap -> userMap.putAll(userServiceMapper.mapOf(dbConsultant, chatUserMap)),
        throwPersistenceConflict(dbConsultant.getId(), dbConsultant.getRocketChatId())
    );

    return userMap;
  }

  private Runnable throwPersistenceConflict(String dbUserId, String chatUserId) {
    var message = String.format(
        "User (%s) found in database but not in Rocket.Chat (%s)", dbUserId, chatUserId
    );

    return () -> {
      throw new InternalServerErrorException(message);
    };
  }
}
