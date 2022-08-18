package de.caritas.cob.userservice.api.conversation.registry;

import de.caritas.cob.userservice.api.conversation.model.ConversationListType;
import de.caritas.cob.userservice.api.conversation.provider.ConversationListProvider;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Registry for all {@link ConversationListProvider} implementations. */
@Component
@RequiredArgsConstructor
public class ConversationListProviderRegistry {

  private final @NonNull ApplicationContext applicationContext;
  private List<ConversationListProvider> conversationListProviders;

  /** Initializes all {@link ConversationListProvider} instances. */
  @EventListener(ApplicationReadyEvent.class)
  public void initializeConversationSuppliers() {
    this.conversationListProviders =
        new ArrayList<>(
            this.applicationContext.getBeansOfType(ConversationListProvider.class).values());
  }

  /**
   * Retrieves the {@link ConversationListProvider} by given {@link ConversationListType}.
   *
   * @param conversationType the requested {@link ConversationListType}
   * @return the according {@link ConversationListProvider}
   */
  public ConversationListProvider findByConversationType(ConversationListType conversationType) {
    return this.conversationListProviders.stream()
        .filter(provider -> provider.providedType().equals(conversationType))
        .findFirst()
        .orElseThrow();
  }
}
