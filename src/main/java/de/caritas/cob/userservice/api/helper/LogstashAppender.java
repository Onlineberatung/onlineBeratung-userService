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

    if (!isLogstashEnvVariableSet()) {
      return;
    }

    String json = serializeToJson((T) event);
    try (CloseableHttpClient client = getHttpClient()) {
      client.execute(prepareHttpPutRequest(json));
    } catch (IOException e) {
      handleIOException(e);
    }
  }

  CloseableHttpClient getHttpClient() {
    return HttpClients.createDefault();
  }

  private boolean isLogstashEnvVariableSet() {
    this.logstashHost = getLogstashHost();
    if (logstashHost == null) {
      logToStandardError("Logstash env variable (LOGSTASH_HOST) not set, skipping logging to logstash");
      return false;
    }
    return true;
  }

  protected String getLogstashHost() {
    return System.getenv("LOGSTASH_HOST");
  }

  private HttpPut prepareHttpPutRequest(String json) throws UnsupportedEncodingException {
    HttpPut httpPut = new HttpPut(logstashHost);
    StringEntity entity = new StringEntity(json);
    httpPut.setEntity(entity);
    httpPut.setHeader("Content-type", "application/json");
    return httpPut;
  }

  private void handleIOException(IOException e) {
    logToStandardError("IO Exception during http call to logstash endpoint");
    e.printStackTrace();
  }

  private String serializeToJson(T event) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    this.encode(event, outputStream);
    return outputStream.toString();
  }

  private void encode(T event, OutputStream outputStream) {
    if (this.encoder instanceof StreamingEncoder) {
      try {
        ((StreamingEncoder) this.encoder).encode(event, outputStream);
      } catch (Exception e) {
        logToStandardError("Encoder exception occurred. Logs may not be delivered to logstash");
        e.printStackTrace();
      }
    } else {
      logToStandardError("Encoder is not an instance of streaming encoder. ");
    }
  }

  private void logToStandardError(String msg) {
    System.err.println(msg);
  }

  public void setEncoder(Encoder<T> encoder) {
    this.encoder = encoder;
  }
}
