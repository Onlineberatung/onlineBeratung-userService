package de.caritas.cob.userservice.api.scheduler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ScheduledLogger {

  @SneakyThrows
  @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
  public void logMethod(ProceedingJoinPoint joinPoint) {
    val schedulerName = joinPoint.getSignature().toShortString();
    try {
      log.info("{} Scheduler started", schedulerName);
      joinPoint.proceed();
    } finally {
      log.info("{} Scheduler completed", schedulerName);
    }
  }
}
