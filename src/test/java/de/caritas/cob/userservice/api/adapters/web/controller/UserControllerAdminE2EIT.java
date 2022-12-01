package de.caritas.cob.userservice.api.adapters.web.controller;

import org.jeasy.random.EasyRandom;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("testing")
@AutoConfigureTestDatabase
@TestPropertySource(properties = "feature.topics.enabled=true")
public class UserControllerAdminE2EIT {

  private static final EasyRandom easyRandom = new EasyRandom();
}
