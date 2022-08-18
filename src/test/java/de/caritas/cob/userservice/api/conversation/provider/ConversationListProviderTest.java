package de.caritas.cob.userservice.api.conversation.provider;

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.caritas.cob.userservice.api.adapters.web.dto.ConsultantSessionListResponseDTO;
import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.model.PageableListRequest;
import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConversationListProviderTest {

  private final ConversationListProvider conversationListProvider =
      new ConversationListProvider() {
        @Override
        public ConsultantSessionListResponseDTO buildConversations(
            PageableListRequest pageableListRequest) {
          return null;
        }

        @Override
        public ConversationListType providedType() {
          return null;
        }
      };

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -999})
  void obtainPageByOffsetAndCount_Should_throwInternalServerErrorException_When_countIsLowerThanOne(
      int invalidCount) {
    PageableListRequest listRequest = PageableListRequest.builder().count(invalidCount).build();

    assertThrows(
        InternalServerErrorException.class,
        () -> conversationListProvider.obtainPageByOffsetAndCount(listRequest));
  }
}
