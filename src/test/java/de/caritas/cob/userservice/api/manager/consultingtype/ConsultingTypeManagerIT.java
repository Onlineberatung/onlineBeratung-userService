package de.caritas.cob.userservice.api.manager.consultingtype;

import static de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManagerTest.countConsultingTypeSettings;
import static de.caritas.cob.userservice.api.manager.consultingtype.ConsultingTypeManagerTest.loadConsultingTypeSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import de.caritas.cob.userservice.UserServiceApplication;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserServiceApplication.class)
@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ConsultingTypeManagerIT {

  @Autowired
  private ConsultingTypeManager consultingTypeManager;
  

}
