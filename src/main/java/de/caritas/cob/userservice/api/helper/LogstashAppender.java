package de.caritas.cob.userservice.api.helper;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import net.logstash.logback.encoder.StreamingEncoder;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class LogstashAppender<T extends DeferredProcessingAware> extends
    AppenderBase<ILoggingEvent> {

  private String logstashHost;
  private Encoder<T> encoder;

  @Override
  protected void append(ILoggingEvent event) {

    this.logstashHost = System.getenv("LOGSTASH_HOST");
    if (logstashHost == null) {
      logToStandardOutput("logstash env variable not found, skipping logging");
      return;
    }

    String json = serializeToJson((T) event);
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      client.execute(prepareHttpPutRequest(json));
    } catch (IOException e) {
      handleException(e);
    }
  }

  private HttpPut prepareHttpPutRequest(String json) throws UnsupportedEncodingException {
    HttpPut httpPut = new HttpPut(logstashHost);
    StringEntity entity = new StringEntity(json);
    httpPut.setEntity(entity);
    httpPut.setHeader("Content-type", "application/json");
    return httpPut;
  }

  private void handleException(IOException e) {
    logToStandardOutput("IO Exception during http call to logstash endpoint");
    e.printStackTrace();
  }

  private String serializeToJson(T event) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    this.encode(event, outputStream);
    return outputStream.toString();
  }

  private void encode(T event, OutputStream outputStream) {
    if (this.encoder instanceof StreamingEncoder) {
      logToStandardOutput("Encoding message stream");
      try {
        ((StreamingEncoder) this.encoder).encode(event, outputStream);
      } catch (Exception e) {
        logToStandardOutput("Encoder exception occurred. Logs may not be delivered to logstash");
        e.printStackTrace();
      }
    }
  }

  private void logToStandardOutput(String msg) {
    System.out.println(msg);
  }

  public void setEncoder(Encoder<T> encoder) {
    this.encoder = encoder;
  }
}
