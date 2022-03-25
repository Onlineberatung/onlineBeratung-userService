package de.caritas.cob.userservice.api.config;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheManagerConfig {

  public static final String AGENCY_CACHE = "agencyCache";
  public static final String CONSULTING_TYPE_CACHE = "consultingTypeCache";
  public static final String TENANT_CACHE = "tenantCache";

  @Value("${cache.agencies.configuration.maxEntriesLocalHeap}")
  private long agenciesMaxEntriesLocalHeap;

  @Value("${cache.agencies.configuration.eternal}")
  private boolean agenciesEternal;

  @Value("${cache.agencies.configuration.timeToIdleSeconds}")
  private long agenciesTimeToIdleSeconds;

  @Value("${cache.agencies.configuration.timeToLiveSeconds}")
  private long agenciesTimeToLiveSeconds;

  @Value("${cache.consulting.type.configuration.maxEntriesLocalHeap}")
  private long consultingTypeMaxEntriesLocalHeap;

  @Value("${cache.consulting.type.configuration.eternal}")
  private boolean consultingTypeEternal;

  @Value("${cache.consulting.type.configuration.timeToIdleSeconds}")
  private long consultingTypeTimeToIdleSeconds;

  @Value("${cache.consulting.type.configuration.timeToLiveSeconds}")
  private long consultingTypeTimeToLiveSeconds;

  @Value("${cache.tenant.configuration.maxEntriesLocalHeap}")
  private long tenantMaxEntriesLocalHeap;

  @Value("${cache.tenant.configuration.eternal}")
  private boolean tenantEternal;

  @Value("${cache.tenant.configuration.timeToIdleSeconds}")
  private long tenantTimeToIdleSeconds;

  @Value("${cache.tenant.configuration.timeToLiveSeconds}")
  private long tenantTimeToLiveSeconds;

  @Bean
  public CacheManager cacheManager() {
    return new EhCacheCacheManager(ehCacheManager());
  }

  @Bean(destroyMethod = "shutdown")
  public net.sf.ehcache.CacheManager ehCacheManager() {
    var config = new net.sf.ehcache.config.Configuration();
    config.addCache(buildAgencyCacheConfiguration());
    config.addCache(buildConsultingTypeCacheConfiguration());
    config.addCache(buildTenantCacheConfiguration());

    return net.sf.ehcache.CacheManager.newInstance(config);
  }

  private CacheConfiguration buildAgencyCacheConfiguration() {
    var agencyCacheConfiguration = new CacheConfiguration();
    agencyCacheConfiguration.setName(AGENCY_CACHE);
    agencyCacheConfiguration.setMaxEntriesLocalHeap(agenciesMaxEntriesLocalHeap);
    agencyCacheConfiguration.setEternal(agenciesEternal);
    agencyCacheConfiguration.setTimeToIdleSeconds(agenciesTimeToIdleSeconds);
    agencyCacheConfiguration.setTimeToLiveSeconds(agenciesTimeToLiveSeconds);
    return agencyCacheConfiguration;
  }

  private CacheConfiguration buildConsultingTypeCacheConfiguration() {
    var consultingTypeCacheConfiguration = new CacheConfiguration();
    consultingTypeCacheConfiguration.setName(CONSULTING_TYPE_CACHE);
    consultingTypeCacheConfiguration.setMaxEntriesLocalHeap(consultingTypeMaxEntriesLocalHeap);
    consultingTypeCacheConfiguration.setEternal(consultingTypeEternal);
    consultingTypeCacheConfiguration.setTimeToIdleSeconds(consultingTypeTimeToIdleSeconds);
    consultingTypeCacheConfiguration.setTimeToLiveSeconds(consultingTypeTimeToLiveSeconds);
    return consultingTypeCacheConfiguration;
  }

  private CacheConfiguration buildTenantCacheConfiguration() {
    var tenantCacheConfiguration = new CacheConfiguration();
    tenantCacheConfiguration.setName(TENANT_CACHE);
    tenantCacheConfiguration.setMaxEntriesLocalHeap(tenantMaxEntriesLocalHeap);
    tenantCacheConfiguration.setEternal(tenantEternal);
    tenantCacheConfiguration.setTimeToIdleSeconds(tenantTimeToIdleSeconds);
    tenantCacheConfiguration.setTimeToLiveSeconds(tenantTimeToLiveSeconds);
    return tenantCacheConfiguration;
  }

}
