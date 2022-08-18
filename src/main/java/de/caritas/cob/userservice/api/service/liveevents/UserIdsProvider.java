package de.caritas.cob.userservice.api.service.liveevents;

import java.util.List;

interface UserIdsProvider {

  List<String> collectUserIds(String rcGroupId);
}
