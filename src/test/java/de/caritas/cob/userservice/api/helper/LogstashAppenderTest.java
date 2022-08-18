package de.caritas.cob.userservice.api.helper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;
import java.io.IOException;
import java.io.OutputStream;
import lombok.Setter;
import net.logstash.logback.encoder.StreamingEncoder;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogstashAppenderTest {

  private static final int LOG_BUFFER_SIZE = 2;

  @InjectMocks TestableLogstashAppender logstashAppender = getTestableLogstashAppender();

  private TestableLogstashAppender getTestableLogstashAppender() {
    TestableLogstashAppender testableLogstashAppender = new TestableLogstashAppender();
    testableLogstashAppender.setLogBufferSize(LOG_BUFFER_SIZE);
    return testableLogstashAppender;
  }

  @Mock CloseableHttpClient closeableHttpClientMock;

  @Mock CustomStreamingEncoder streamingEncoder;

  @Mock ILoggingEvent loggingEvent;

  @Test
  void append_Should_callLogbackEndpointIfNumberOfLogsExceedBufferSize()
      throws IOException, InterruptedException {
    // when
    logstashAppender.append(loggingEvent);
    logstashAppender.append(loggingEvent);
    logstashAppender.append(loggingEvent);
    // then
    verify(streamingEncoder, timeout(1000).times(3))
        .encode(eq(loggingEvent), any(OutputStream.class));
    verify(closeableHttpClientMock, timeout(1000).times(2)).execute(any(HttpPut.class));
  }

  @Test
  void append_Should_Not_CallLogbackEndpointIfEnvironmentVariableNotSet() throws IOException {
    // given
    logstashAppender = getTestableLogstashAppender();
    logstashAppender.setLogstashHost(null);
    // when
    logstashAppender.append(loggingEvent);
    // then
    verifyNoInteractions(streamingEncoder);
    verifyNoInteractions(closeableHttpClientMock);
  }

  @Setter
  private class TestableLogstashAppender extends LogstashAppender {

    private String logstashHost = "http://logstash.default";

    protected String getLogstashHost() {
      return logstashHost;
    }

    @Override
    protected CloseableHttpClient getHttpClient() {
      return closeableHttpClientMock;
    }
  }

  private interface CustomStreamingEncoder extends Encoder, StreamingEncoder {}
}
