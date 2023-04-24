package de.caritas.cob.userservice.api.port.out;

import static org.junit.jupiter.api.Assertions.*;

import de.caritas.cob.userservice.api.config.JpaAuditingConfiguration;
import de.caritas.cob.userservice.api.model.Admin;
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
class AdminRepositoryIT {

  private static final EasyRandom easyRandom = new EasyRandom();

  private Admin admin;

  @Autowired private AdminRepository adminRepository;

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
    givenPersistedAdmin();

    assertNotNull(admin.getCreateDate());
    assertNotNull(admin.getUpdateDate());
    assertEquals(admin.getCreateDate(), admin.getUpdateDate());
  }

  @Test
  void saveShouldUpdateAuditingData() {
    givenPersistedAdmin();

    auditingHandler.markModified(admin);
    admin = adminRepository.save(admin);
    assertTrue(admin.getCreateDate().isBefore(admin.getUpdateDate()));
  }

  private void givenPersistedAdmin() {
    admin = easyRandom.nextObject(Admin.class);
    admin.setId(UUID.randomUUID().toString());
    admin.setRcUserId(null);
    admin.setCreateDate(null);
    admin.setUpdateDate(null);

    admin = adminRepository.save(admin);
  }
}
