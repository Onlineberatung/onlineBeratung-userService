package de.caritas.cob.userservice.api.service.statistics.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.caritas.cob.userservice.api.helper.json.OffsetDateTimeToStringSerializer;
import de.caritas.cob.userservice.api.model.User;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.ArchiveSessionStatisticsEventMessage;
import de.caritas.cob.userservice.statisticsservice.generated.web.model.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.SESSION_ID;
import static org.assertj.core.api.Assertions.assertThat;


class ArchiveStatisticsEventTest {

    private ArchiveStatisticsEvent archiveStatisticsEvent;
    private static final Long TENANT_ID = 2L;
    private static final String USER_ID = "userId";
    private static final LocalDateTime END_DATE = LocalDateTime.now();

    @BeforeEach
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        User user = new User();
        user.setUserId(USER_ID);
        user.setTenantId(TENANT_ID);
        archiveStatisticsEvent =
                new ArchiveStatisticsEvent(user, SESSION_ID, END_DATE);
    }

    @Test
    public void getEventType_Should_ReturnEventTypeArchiveMessage() {

        assertThat(archiveStatisticsEvent.getEventType()).isEqualTo(EventType.ARCHIVE_SESSION);
    }

    @Test
    public void getPayload_Should_ReturnValidJsonPayload() throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(buildSimpleModule());

        ArchiveSessionStatisticsEventMessage archiveSessionStatisticsEventMessage = objectMapper.readValue(archiveStatisticsEvent.getPayload().get(), ArchiveSessionStatisticsEventMessage.class);

        Optional<String> result = archiveStatisticsEvent.getPayload();

        assertThat(result.isPresent()).isTrue();
        assertThat(archiveSessionStatisticsEventMessage.getSessionId()).isEqualTo(SESSION_ID);
        assertThat(archiveSessionStatisticsEventMessage.getTenantId()).isEqualTo(TENANT_ID);
        assertThat(archiveSessionStatisticsEventMessage.getUserId()).isEqualTo(USER_ID);
        assertThat(archiveSessionStatisticsEventMessage.getEndDate()).isEqualTo(END_DATE.truncatedTo(ChronoUnit.SECONDS) + "Z");
    }

    private static SimpleModule buildSimpleModule() {
        return new SimpleModule()
                .addSerializer(OffsetDateTime.class, new OffsetDateTimeToStringSerializer());
    }
}