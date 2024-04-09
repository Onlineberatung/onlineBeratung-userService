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

  public static final String APPLICATION_SETTINGS_CACHE = "applicationSettingsCache";
  public static final String TENANT_CACHE = "tenantCache";
  public static final String TENANT_ADMIN_CACHE = "tenantAdminCache";
  public static final String TOPICS_CACHE = "topicsCache";

  public static final String ROCKET_CHAT_USER_CACHE = "rocketChatUserCache";

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

  @Value("${cache.topic.configuration.maxEntriesLocalHeap}")
  private long topicMaxEntriesLocalHeap;

  @Value("${cache.topic.configuration.eternal}")
  private boolean topicEternal;

  @Value("${cache.topic.configuration.timeToIdleSeconds}")
  private long topicTimeToIdleSeconds;

  @Value("${cache.topic.configuration.timeToLiveSeconds}")
  private long topicTimeToLiveSeconds;

  @Value("${cache.appsettings.configuration.maxEntriesLocalHeap}")
  private long appSettingsMaxEntriesLocalHeap;

  @Value("${cache.appsettings.configuration.eternal}")
  private boolean appSettingsEternal;

  @Value("${cache.appsettings.configuration.timeToIdleSeconds}")
  private long appSettingsTimeToIdleSeconds;

  @Value("${cache.appsettings.configuration.timeToLiveSeconds}")
  private long appSettingsTimeToLiveSeconds;

  @Value("${cache.rocketchat.configuration.maxEntriesLocalHeap}")
  private long rocketchatCacheMaxEntriesLocalHeap;

  @Value("${cache.rocketchat.configuration.eternal}")
  private boolean rocketchatCacheEternal;

  @Value("${cache.rocketchat.configuration.timeToIdleSeconds}")
  private long rocketchatCacheTimeToIdleSeconds;

  @Value("${cache.rocketchat.configuration.timeToLiveSeconds}")
  private long rocketchatCacheTimeToLiveSeconds;

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
    config.addCache(buildTenantAdminCacheConfiguration());
    config.addCache(buildTopicCacheConfiguration());
    config.addCache(buildApplicationSettingsCacheConfiguration());

    config.addCache(buildRocketchatUserCacheConfiguration());
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

  private CacheConfiguration buildTenantAdminCacheConfiguration() {
    var tenantCacheConfiguration = new CacheConfiguration();
    tenantCacheConfiguration.setName(TENANT_ADMIN_CACHE);
    tenantCacheConfiguration.setMaxEntriesLocalHeap(tenantMaxEntriesLocalHeap);
    tenantCacheConfiguration.setEternal(tenantEternal);
    tenantCacheConfiguration.setTimeToIdleSeconds(tenantTimeToIdleSeconds);
    tenantCacheConfiguration.setTimeToLiveSeconds(tenantTimeToLiveSeconds);
    return tenantCacheConfiguration;
  }

  private CacheConfiguration buildTopicCacheConfiguration() {
    var topicCacheConfiguration = new CacheConfiguration();
    topicCacheConfiguration.setName(TOPICS_CACHE);
    topicCacheConfiguration.setMaxEntriesLocalHeap(topicMaxEntriesLocalHeap);
    topicCacheConfiguration.setEternal(topicEternal);
    topicCacheConfiguration.setTimeToIdleSeconds(topicTimeToIdleSeconds);
    topicCacheConfiguration.setTimeToLiveSeconds(topicTimeToLiveSeconds);
    return topicCacheConfiguration;
  }

  private CacheConfiguration buildApplicationSettingsCacheConfiguration() {
    var appSettingsCacheConfiguration = new CacheConfiguration();
    appSettingsCacheConfiguration.setName(APPLICATION_SETTINGS_CACHE);
    appSettingsCacheConfiguration.setMaxEntriesLocalHeap(appSettingsMaxEntriesLocalHeap);
    appSettingsCacheConfiguration.setEternal(appSettingsEternal);
    appSettingsCacheConfiguration.setTimeToIdleSeconds(appSettingsTimeToIdleSeconds);
    appSettingsCacheConfiguration.setTimeToLiveSeconds(appSettingsTimeToLiveSeconds);
    return appSettingsCacheConfiguration;
  }

  private CacheConfiguration buildRocketchatUserCacheConfiguration() {
    var rocketchatCacheConfiguration = new CacheConfiguration();
    rocketchatCacheConfiguration.setName(ROCKET_CHAT_USER_CACHE);
    rocketchatCacheConfiguration.setMaxEntriesLocalHeap(rocketchatCacheMaxEntriesLocalHeap);
    rocketchatCacheConfiguration.setEternal(rocketchatCacheEternal);
    rocketchatCacheConfiguration.setTimeToIdleSeconds(rocketchatCacheTimeToIdleSeconds);
    rocketchatCacheConfiguration.setTimeToLiveSeconds(rocketchatCacheTimeToLiveSeconds);
    return rocketchatCacheConfiguration;
  }
}
