package de.caritas.cob.userservice.api.service;

import static de.caritas.cob.userservice.api.testHelper.TestConstants.RC_CREDENTIALS_SYSTEM_A;
import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatCredentialsProvider;
import de.caritas.cob.userservice.api.adapters.rocketchat.RocketChatService;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceDTO;
import de.caritas.cob.userservice.api.adapters.rocketchat.dto.login.PresenceDTO.PresenceStatus;
import de.caritas.cob.userservice.api.exception.rocketchat.RocketChatUserNotInitializedException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ActiveProfiles("testing")
class RocketChatServiceIT {

  @Autowired private RocketChatService underTest;

  @Autowired private ObjectMapper objectMapper;

  @MockBean
  @Qualifier("rocketChatRestTemplate")
  private RestTemplate restTemplate;

  @MockBean private RocketChatCredentialsProvider rcCredentialsProvider;

  @Autowired private MongoClient mockedMongoClient;

  @Mock private MongoDatabase mongoDatabase;

  @Mock private MongoCollection<Document> mongoCollection;

  @Mock private MongoCursor<Document> mongoCursor;

  @Mock private FindIterable<Document> findIterable;

  private String chatUserId;
  private PresenceDTO presenceDto;

  @AfterEach
  void reset() {
    chatUserId = null;
    presenceDto = null;
  }

  @Test
  void isLoggedInShouldReturnPositiveStatusWhenOnline()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.ONLINE);

    var isLoggedIn = underTest.isLoggedIn(chatUserId).orElseThrow();

    assertTrue(isLoggedIn);
  }

  @Test
  void isLoggedInShouldReturnPositiveStatusWhenBusy() throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.BUSY);

    var isLoggedIn = underTest.isLoggedIn(chatUserId).orElseThrow();

    assertTrue(isLoggedIn);
  }

  @Test
  void isLoggedInShouldReturnPositiveStatusWhenAway() throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.AWAY);

    var isLoggedIn = underTest.isLoggedIn(chatUserId).orElseThrow();

    assertTrue(isLoggedIn);
  }

  @Test
  void isLoggedInShouldReturnNegativeStatusWhenOffline()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.OFFLINE);

    var isLoggedIn = underTest.isLoggedIn(chatUserId).orElseThrow();

    assertFalse(isLoggedIn);
  }

  @Test
  void isLoggedInShouldReturnEmptyOnClientError() throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAnErroneousPresenceResponse();

    var isLoggedIn = underTest.isLoggedIn(chatUserId);

    assertTrue(isLoggedIn.isEmpty());
  }

  @Test
  void isLoggedInShouldReturnEmptyOnRemoteServerError()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAnInvalidPresenceResponse();

    var isLoggedIn = underTest.isLoggedIn(chatUserId);

    assertTrue(isLoggedIn.isEmpty());
  }

  @Test
  void isAvailableShouldReturnPositiveStatusWhenOnline()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.ONLINE);

    var isAvailable = underTest.isAvailable(chatUserId).orElseThrow();

    assertTrue(isAvailable);
  }

  @Test
  void isAvailableShouldReturnNegativeStatusWhenBusy()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.BUSY);

    var isAvailable = underTest.isAvailable(chatUserId).orElseThrow();

    assertFalse(isAvailable);
  }

  @Test
  void isAvailableShouldReturnNegativeStatusWhenAway()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.AWAY);

    var isAvailable = underTest.isAvailable(chatUserId).orElseThrow();

    assertFalse(isAvailable);
  }

  @Test
  void isAvailableShouldReturnNegativeStatusWhenOffline()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAValidPresenceResponse(PresenceStatus.OFFLINE);

    var isAvailable = underTest.isAvailable(chatUserId).orElseThrow();

    assertFalse(isAvailable);
  }

  @Test
  void isAvailableShouldReturnEmptyOnClientError() throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAnErroneousPresenceResponse();

    var isAvailable = underTest.isAvailable(chatUserId);

    assertTrue(isAvailable.isEmpty());
  }

  @Test
  void isAvailableShouldReturnEmptyOnRemoteServerError()
      throws RocketChatUserNotInitializedException {
    givenAValidRocketChatSystemUser();
    givenAValidChatUserId();
    givenAnInvalidPresenceResponse();

    var isAvailable = underTest.isAvailable(chatUserId);

    assertTrue(isAvailable.isEmpty());
  }

  @Test
  void getChatUsersShouldReturnEmptyMongoResponse() {
    givenEmptyMongoResponse();

    var users = underTest.getChatUsers(RandomStringUtils.randomAlphanumeric(17));

    assertEquals(0, users.size());
  }

  @Test
  void getChatUsersShouldReturnMongoResponseWithMinimalUsers() throws JsonProcessingException {
    var doc1 = givenSubscription("a", "b", null);
    var doc2 = givenSubscription("d", "e", null);
    var doc3 = givenSubscription("f", "g", null);
    givenMongoResponseWith(doc1, doc2, doc3);

    var users = underTest.getChatUsers(RandomStringUtils.randomAlphanumeric(17));

    assertEquals(3, users.size());
    var user1 = users.get(0);
    assertEquals("a", user1.get_id());
    assertEquals("b", user1.getUsername());
    assertNull(user1.getName());
    var user2 = users.get(1);
    assertEquals("d", user2.get_id());
    assertEquals("e", user2.getUsername());
    assertNull(user2.getName());
    var user3 = users.get(2);
    assertEquals("f", user3.get_id());
    assertEquals("g", user3.getUsername());
    assertNull(user3.getName());
  }

  @Test
  void getChatUsersShouldReturnMongoResponseWithFullUsers() throws JsonProcessingException {
    var doc1 = givenSubscription("a", "b", "c");
    var doc2 = givenSubscription("d", "e", "f");
    givenMongoResponseWith(doc1, doc2);

    var users = underTest.getChatUsers(RandomStringUtils.randomAlphanumeric(17));

    assertEquals(2, users.size());
    var user1 = users.get(0);
    assertEquals("a", user1.get_id());
    assertEquals("b", user1.getUsername());
    assertEquals("c", user1.getName());
    var user2 = users.get(1);
    assertEquals("d", user2.get_id());
    assertEquals("e", user2.getUsername());
    assertEquals("f", user2.getName());
  }

  private void givenEmptyMongoResponse() {
    givenMongoResponseWith(null);
  }

  private void givenMongoResponseWith(Document doc, Document... docs) {
    if (nonNull(doc)) {
      when(mongoCursor.next()).thenReturn(doc, docs);
    }
    var booleanList = new LinkedList<Boolean>();
    var numExtraDocs = docs.length;
    while (numExtraDocs-- > 0) {
      booleanList.add(true);
    }
    booleanList.add(false);
    if (nonNull(doc)) {
      when(mongoCursor.hasNext()).thenReturn(true, booleanList.toArray(new Boolean[0]));
    } else {
      when(mongoCursor.hasNext()).thenReturn(false);
    }
    when(findIterable.iterator()).thenReturn(mongoCursor);
    when(mongoCollection.find(any(Bson.class))).thenReturn(findIterable);
    when(mockedMongoClient.getDatabase("rocketchat")).thenReturn(mongoDatabase);
    when(mongoDatabase.getCollection("rocketchat_subscription")).thenReturn(mongoCollection);
  }

  private Document givenSubscription(String chatUserId, String username, String name)
      throws JsonProcessingException {
    var doc = new LinkedHashMap<String, Object>();
    doc.put("_id", RandomStringUtils.randomAlphanumeric(17));
    doc.put("rid", RandomStringUtils.randomAlphanumeric(17));
    doc.put("name", RandomStringUtils.randomAlphanumeric(17));

    var user = new LinkedHashMap<>();
    user.put("_id", chatUserId);
    user.put("username", username);
    if (nonNull(name)) {
      user.put("name", name);
    }

    doc.put("u", user);

    var json = objectMapper.writeValueAsString(doc);

    return Document.parse(json);
  }

  private void givenAValidPresenceResponse(PresenceStatus present) {
    presenceDto = new PresenceDTO();
    presenceDto.setPresence(present);
    presenceDto.setSuccess(true);

    whenPresenceIsRequested().thenReturn(ResponseEntity.ok(presenceDto));
  }

  private void givenAnInvalidPresenceResponse() {
    whenPresenceIsRequested().thenReturn(ResponseEntity.ok(null));
  }

  private void givenAnErroneousPresenceResponse() {
    var errorException = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "error", null, null);
    whenPresenceIsRequested().thenThrow(errorException);
  }

  private void givenAValidChatUserId() {
    chatUserId = RandomStringUtils.randomAlphanumeric(17);
  }

  private OngoingStubbing<ResponseEntity<PresenceDTO>> whenPresenceIsRequested() {
    return when(
        restTemplate.exchange(
            eq("https://testing.com/api/v1/users.getPresence?userId=" + chatUserId),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            ArgumentMatchers.<Class<PresenceDTO>>any()));
  }

  private void givenAValidRocketChatSystemUser() throws RocketChatUserNotInitializedException {
    when(rcCredentialsProvider.getSystemUserSneaky()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
    when(rcCredentialsProvider.getSystemUser()).thenReturn(RC_CREDENTIALS_SYSTEM_A);
  }
}
