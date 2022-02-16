package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.port.in.AccountManaging;
import de.caritas.cob.userservice.api.repository.consultant.ConsultantRepository;
import de.caritas.cob.userservice.api.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountManager implements AccountManaging {

  private final ConsultantRepository consultantRepository;

  private final UserRepository userRepository;

  @Override
  public void saveEmail(String id, String email) {
    userRepository.findByUserIdAndDeleteDateIsNull(id).ifPresentOrElse(user -> {
      user.setEmail(email);
      userRepository.save(user);
    }, () -> consultantRepository.findByIdAndDeleteDateIsNull(id).ifPresent(consultant -> {
      consultant.setEmail(email);
      consultantRepository.save(consultant);
    }));
  }
}
