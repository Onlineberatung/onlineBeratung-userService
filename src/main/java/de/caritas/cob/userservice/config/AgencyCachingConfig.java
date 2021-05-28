package de.caritas.cob.userservice.config;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableCaching
public class AgencyCachingConfig {

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
  public net.sf.ehcache.CacheManager agencyEhCacheManager() {
    var cacheConfiguration = new CacheConfiguration();
    cacheConfiguration.setName(AGENCY_CACHE);
    cacheConfiguration.setMaxEntriesLocalHeap(maxEntriesLocalHeap);
    cacheConfiguration.setEternal(eternal);
    cacheConfiguration.setTimeToIdleSeconds(timeToIdleSeconds);
    cacheConfiguration.setTimeToLiveSeconds(timeToLiveSeconds);
    var config = new net.sf.ehcache.config.Configuration();
    config.addCache(cacheConfiguration);

    return net.sf.ehcache.CacheManager.newInstance(config);
  }

  @Bean
  @Primary
  public CacheManager agencyCacheManager() {
    return new EhCacheCacheManager(agencyEhCacheManager());
  }

}
