package de.caritas.cob.userservice.api.port.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.caritas.cob.userservice.api.config.JpaAuditingConfiguration;
import de.caritas.cob.userservice.api.model.Admin;
import de.caritas.cob.userservice.api.model.AdminAgency;
import java.util.Objects;
import java.util.UUID;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
@Import(JpaAuditingConfiguration.class)
class AdminAgencyRepositoryIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  private Admin admin;

  private AdminAgency adminAgency;

  @Autowired private AdminRepository adminRepository;

  @Autowired private AdminAgencyRepository adminAgencyRepository;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  private AuditingHandler auditingHandler;

  @AfterEach
  public void reset() {
    if (Objects.nonNull(admin)) {
      adminRepository.delete(admin);
    }
  }

  @Test
  void saveShouldWriteAuditingData() {
    givenPersistedAdminWIthAgency();

    assertNotNull(adminAgency.getCreateDate());
    assertNotNull(adminAgency.getUpdateDate());
    assertEquals(adminAgency.getCreateDate(), adminAgency.getUpdateDate());
  }

  @Test
  void saveShouldUpdateAuditingData() {
    givenPersistedAdminWIthAgency();

    auditingHandler.markModified(adminAgency);
    adminAgency = adminAgencyRepository.save(adminAgency);

    assertTrue(adminAgency.getCreateDate().isBefore(adminAgency.getUpdateDate()));
  }

  private void givenPersistedAdminWIthAgency() {
    admin = easyRandom.nextObject(Admin.class);
    admin.setId(UUID.randomUUID().toString());
    admin.setRcUserId(null);
    admin.setCreateDate(null);
    admin.setUpdateDate(null);
    admin = adminRepository.save(admin);

    adminAgency = new AdminAgency();
    adminAgency.setAgencyId(easyRandom.nextLong());
    adminAgency.setAdmin(admin);
    adminAgencyRepository.save(adminAgency);
  }
}
