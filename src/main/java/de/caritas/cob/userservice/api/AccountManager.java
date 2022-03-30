package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import de.caritas.cob.userservice.api.model.Consultant;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.api.port.in.AccountManaging;
import de.caritas.cob.userservice.api.port.out.ConsultantRepository;
import de.caritas.cob.userservice.api.port.out.MessageClient;
import de.caritas.cob.userservice.api.port.out.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountManager implements AccountManaging {

  private final ConsultantRepository consultantRepository;

  private final UserRepository userRepository;

  private final UserServiceMapper userServiceMapper;

  private final MessageClient messageClient;

  @Override
  public Optional<Map<String, Object>> findConsultant(String id) {
    var userMap = new HashMap<String, Object>();

    consultantRepository.findByIdAndDeleteDateIsNull(id).ifPresent(dbConsultant ->
        messageClient.findUser(dbConsultant.getRocketChatId()).ifPresentOrElse(
            chatUserMap -> userMap.putAll(userServiceMapper.mapOf(dbConsultant, chatUserMap)),
            throwPersistenceConflict(id, dbConsultant.getRocketChatId())
        ));

    return userMap.isEmpty() ? Optional.empty() : Optional.of(userMap);
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

  private Runnable throwPersistenceConflict(String dbUserId, String chatUserId) {
    var message = String.format(
        "User (%s) found in database but not in Rocket.Chat (%s)", dbUserId, chatUserId
    );

    return () -> {
      throw new InternalServerErrorException(message);
    };
  }
}
