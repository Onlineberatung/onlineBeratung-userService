package de.caritas.cob.userservice.api.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.caritas.cob.userservice.api.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
@SpringBootTest(
    properties = {
      "spring.jpa.hibernate.ddl-auto=validate",
      "identity.technical-user.username=tech-user",
      "identity.technical-user.password=tech-pass"
    })
class LiquibaseHibernateIT {
  @Container
  @SuppressWarnings("resource")
  static MariaDBContainer<?> DB =
      new MariaDBContainer<>("mariadb:10.5.10").withDatabaseName("userservice");

  @Autowired UserRepository userRepository;

  @DynamicPropertySource
  static void registerMySQLProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", DB::getJdbcUrl);
    registry.add("spring.datasource.username", DB::getUsername);
    registry.add("spring.datasource.password", DB::getPassword);
  }

  @Test
  void databaseAndEntityMappingsShouldBeInSync() {
    assertTrue(userRepository.count() > -1);
  }
}
