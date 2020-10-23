package de.caritas.cob.userservice.api.helper;

import static org.apache.commons.lang3.BooleanUtils.isTrue;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import de.caritas.cob.userservice.api.model.chat.ChatDTO;
import de.caritas.cob.userservice.api.repository.chat.Chat;
import de.caritas.cob.userservice.api.repository.chat.ChatInterval;
import de.caritas.cob.userservice.api.repository.chatAgency.ChatAgency;
import de.caritas.cob.userservice.api.repository.consultant.Consultant;
import de.caritas.cob.userservice.api.repository.consultantAgency.ConsultantAgency;
import de.caritas.cob.userservice.api.repository.session.ConsultingType;
import de.caritas.cob.userservice.api.repository.user.User;
import de.caritas.cob.userservice.api.repository.userAgency.UserAgency;

/**
 * 
 * Helper for chat
 *
 */
@Component
public class ChatHelper {

  /**
   * Check if the list of chat agencies contains on of the agencies of a consultant
   * 
   * @param chat
   * @param consultant
   * @return
   */
  public boolean isChatAgenciesContainConsultantAgency(Chat chat, Consultant consultant) {
    return chat.getChatAgencies().stream().map(ChatAgency::getAgencyId)
        .anyMatch(consultant.getConsultantAgencies().stream().map(ConsultantAgency::getAgencyId)
            .collect(Collectors.toSet())::contains);
  }

  /**
   * 
   * Check if the list of chat agencies contains on of the agencies of a user
   * 
   * @param chat
   * @param user
   * @return
   */
  public boolean isChatAgenciesContainUserAgency(Chat chat, User user) {
    return chat.getChatAgencies().stream().map(ChatAgency::getAgencyId)
        .anyMatch(user.getUserAgencies().stream().map(UserAgency::getAgencyId)
            .collect(Collectors.toSet())::contains);
  }

  /**
   * Get a {@link Chat} from a {@link ChatDTO}
   * 
   * @param chatDTO
   * @param consultant
   * @return
   */
  public Chat convertChatDTOtoChat(ChatDTO chatDTO, Consultant consultant) {
    LocalDateTime startDate = LocalDateTime.of(chatDTO.getStartDate(), chatDTO.getStartTime());
    // As of now only Kreuzbund is using the chat. Therefore insert consulting type "kreuzbund".
    // Also the repetition interval can only be weekly atm.
    return new Chat(chatDTO.getTopic(), ConsultingType.KREUZBUND, startDate, startDate,
        chatDTO.getDuration(), isTrue(chatDTO.isRepetitive()),
        isTrue(chatDTO.isRepetitive()) ? ChatInterval.WEEKLY : null, consultant);
  }

}
