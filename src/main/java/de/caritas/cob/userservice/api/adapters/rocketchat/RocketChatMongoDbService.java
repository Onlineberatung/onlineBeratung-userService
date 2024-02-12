package de.caritas.cob.userservice.api.adapters.rocketchat;

import com.google.common.collect.Lists;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.caritas.cob.userservice.api.adapters.rocketchat.model.RocketchatSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RocketChatMongoDbService implements InitializingBean {

  @Value("${spring.data.mongodb.rocketchat.uri}")
  String mongoUrl;

  @Value("${spring.data.mongodb.rocketchat.database}")
  String databaseName;

  @NonNull MongoClient mongoClient;

  public List<RocketchatSession> findInactiveSessions() {
    MongoDatabase database = mongoClient.getDatabase(databaseName);
    MongoCollection<Document> collection = database.getCollection("rocketchat_sessions");
    Document sessionInactivityCriteria = sessionInactivityCriteria();
    List<RocketchatSession> rocketchatSessions = Lists.newArrayList();
    collection
        .find(sessionInactivityCriteria)
        .forEach(document -> rocketchatSessions.add(new RocketchatSession(document)));
    return rocketchatSessions;
  }

  private Document sessionInactivityCriteria() {
    Document closedAt = new Document("closedAt", null);
    closedAt.put("lastActivityAt", new Document("$lt", "2021-01-01T00:00:00.000Z"));
    return closedAt;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    initializeMongoClient();
  }

  private void initializeMongoClient() {
    if (mongoClient == null) {
      mongoClient = MongoClients.create(mongoUrl);
    }
  }

  public void patchClosedAt(RocketchatSession rocketchatSession) {
    MongoDatabase database = mongoClient.getDatabase(databaseName);
    MongoCollection<Document> collection = database.getCollection("rocketchat_sessions");
    Document originalSession =
        collection.find(new Document("sessionId", rocketchatSession.getSessionId())).first();

    Document patchedDocument = patchDocumentWithClosedAt(rocketchatSession, originalSession);
    collection.replaceOne(originalSession, patchedDocument);
  }

  private Document patchDocumentWithClosedAt(
      RocketchatSession rocketchatSession, Document document) {
    var patchedDocument = cloneDocument(document);
    patchedDocument.put("closedAt", rocketchatSession.getClosedAt());
    return patchedDocument;
  }

  private Document cloneDocument(Document document) {
    return new Document(
        document.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }
}
