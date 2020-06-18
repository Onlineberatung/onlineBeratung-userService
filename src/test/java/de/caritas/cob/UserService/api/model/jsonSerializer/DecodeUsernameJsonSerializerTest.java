package de.caritas.cob.UserService.api.model.jsonSerializer;

import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_DECODED;
import static de.caritas.cob.UserService.testHelper.TestConstants.USERNAME_ENCODED;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.fasterxml.jackson.core.JsonGenerator;
import de.caritas.cob.UserService.api.helper.UserHelper;

@RunWith(MockitoJUnitRunner.class)
public class DecodeUsernameJsonSerializerTest {

  @InjectMocks
  private DecodeUsernameJsonSerializer serializer;
  @Mock
  private JsonGenerator jsonGenerator;
  @Mock
  private UserHelper userHelper;

  @Test
  public void serialize_Schould_DecodeEncodedUsername() throws IOException {

    when(userHelper.decodeUsername(USERNAME_ENCODED)).thenReturn(USERNAME_DECODED);

    serializer.serialize(USERNAME_ENCODED, jsonGenerator, null);

    verify(jsonGenerator, times(1)).writeObject(USERNAME_DECODED);
  }

  @Test
  public void serialize_SchouldNot_DecodeDecodedUsername() throws IOException {

    when(userHelper.decodeUsername(USERNAME_DECODED)).thenReturn(USERNAME_DECODED);

    serializer.serialize(USERNAME_DECODED, jsonGenerator, null);

    verify(jsonGenerator, times(1)).writeObject(USERNAME_DECODED);
  }
}
