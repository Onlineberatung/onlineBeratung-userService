package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.port.in.AccountManaging;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
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

  private final UserMapper userMapper;

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

  private Map<String, Object> patchAdviceSeeker(User adviceSeeker, Map<String, Object> patchMap) {
    if (patchMap.containsKey("email")) {
      adviceSeeker.setEmail((String) patchMap.get("email"));
    }
    if (patchMap.containsKey("encourage2fa")) {
      adviceSeeker.setEncourage2fa((Boolean) patchMap.get("encourage2fa"));
    }
    var savedAdviceSeeker = userRepository.save(adviceSeeker);

    return userMapper.mapOf(savedAdviceSeeker);
  }

  private Map<String, Object> patchConsultant(Consultant consultant, Map<String, Object> patchMap) {
    if (patchMap.containsKey("email")) {
      consultant.setEmail((String) patchMap.get("email"));
    }
    if (patchMap.containsKey("firstName")) {
      consultant.setFirstName((String) patchMap.get("firstName"));
    }
    if (patchMap.containsKey("lastName")) {
      consultant.setFirstName((String) patchMap.get("lastName"));
    }
    if (patchMap.containsKey("encourage2fa")) {
      consultant.setEncourage2fa((Boolean) patchMap.get("encourage2fa"));
    }
    var savedConsultant = consultantRepository.save(consultant);

    return userMapper.mapOf(savedConsultant);
  }
}
