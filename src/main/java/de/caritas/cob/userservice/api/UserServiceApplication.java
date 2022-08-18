package de.caritas.cob.userservice.api;

import de.caritas.cob.userservice.api.config.CsrfSecurityProperties;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({CsrfSecurityProperties.class})
public class UserServiceApplication {

  @Value("${thread.executor.corePoolSize}")
  private int THREAD_CORE_POOL_SIZE;

  @Value("${thread.executor.maxPoolSize}")
  private int THREAD_MAX_POOL_SIZE;

  @Value("${thread.executor.queueCapacity}")
  private int THREAD_QUEUE_CAPACITY;

  @Value("${thread.executor.threadNamePrefix}")
  private String THREAD_NAME_PREFIX;

  public static void main(String[] args) {
    SpringApplication.run(UserServiceApplication.class, args);
  }

  @Bean
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    /*
     * This will create 10 threads at the time of initialization. If all 10 threads are busy and new
     * task comes up, then It will keep tasks in queue. If queue is full it will create 11th thread
     * and will go till 15. Then will throw TaskRejected Exception.
     */
    executor.setCorePoolSize(THREAD_CORE_POOL_SIZE);
    executor.setMaxPoolSize(THREAD_MAX_POOL_SIZE);
    executor.setQueueCapacity(THREAD_QUEUE_CAPACITY);
    executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
    executor.initialize();
    return executor;
  }
}
