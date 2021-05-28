package de.caritas.cob.userservice.config;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class ConsultingTypeCachingConfig {

  @Value("${cache.consulting.type.configuration.maxEntriesLocalHeap}")
  private long maxEntriesLocalHeap;

  @Value("${cache.consulting.type.configuration.eternal}")
  private boolean eternal;

  @Value("${cache.consulting.type.configuration.timeToIdleSeconds}")
  private long timeToIdleSeconds;

  @Value("${cache.consulting.type.configuration.timeToLiveSeconds}")
  private long timeToLiveSeconds;

  public static final String CONSULTING_TYPE_CACHE = "consultingTypeCache";

  @Bean(destroyMethod = "shutdown")
  public net.sf.ehcache.CacheManager consultingTypeEhCacheManager() {
    var cacheConfiguration = new CacheConfiguration();
    cacheConfiguration.setName(CONSULTING_TYPE_CACHE);
    cacheConfiguration.setMaxEntriesLocalHeap(maxEntriesLocalHeap);
    cacheConfiguration.setEternal(eternal);
    cacheConfiguration.setTimeToIdleSeconds(timeToIdleSeconds);
    cacheConfiguration.setTimeToLiveSeconds(timeToLiveSeconds);
    var config = new net.sf.ehcache.config.Configuration();
    config.addCache(cacheConfiguration);

    return net.sf.ehcache.CacheManager.newInstance(config);
  }

  @Bean
  public CacheManager consultingTypeCacheManager() {
    return new EhCacheCacheManager(consultingTypeEhCacheManager());
  }

}
