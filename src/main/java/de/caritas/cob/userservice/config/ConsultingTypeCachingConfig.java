/*package de.caritas.cob.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.sf.ehcache.config.CacheConfiguration;

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
  public net.sf.ehcache.CacheManager ehCacheManager() {
    CacheConfiguration cacheConfiguration = new CacheConfiguration();
    cacheConfiguration.setName(CONSULTING_TYPE_CACHE);
    cacheConfiguration.setMaxEntriesLocalHeap(maxEntriesLocalHeap);
    cacheConfiguration.setEternal(eternal);
    cacheConfiguration.setTimeToIdleSeconds(timeToIdleSeconds);
    cacheConfiguration.setTimeToLiveSeconds(timeToLiveSeconds);
    net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
    config.addCache(cacheConfiguration);

    return net.sf.ehcache.CacheManager.newInstance(config);
  }

  @Bean
  public CacheManager cacheManager() {
    return new EhCacheCacheManager(ehCacheManager());
  }

}*/
