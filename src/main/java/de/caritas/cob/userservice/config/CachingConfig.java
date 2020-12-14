package de.caritas.cob.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.sf.ehcache.config.CacheConfiguration;

@Configuration
@EnableCaching
public class CachingConfig {

  @Value("${cache.agencies.configuration.maxEntriesLocalHeap}")
  private long maxEntriesLocalHeap;

  @Value("${cache.agencies.configuration.eternal}")
  private boolean eternal;

  @Value("${cache.agencies.configuration.timeToIdleSeconds}")
  private long timeToIdleSeconds;

  @Value("${cache.agencies.configuration.timeToLiveSeconds}")
  private long timeToLiveSeconds;

  public static final String AGENCY_CACHE = "agencyCache";

  @Bean(destroyMethod = "shutdown")
  public net.sf.ehcache.CacheManager ehCacheManager() {
    CacheConfiguration cacheConfiguration = new CacheConfiguration();
    cacheConfiguration.setName(AGENCY_CACHE);
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

}
