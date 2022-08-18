package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.ChatAgency;
import org.springframework.data.repository.CrudRepository;

public interface ChatAgencyRepository extends CrudRepository<ChatAgency, Long> {}
