package de.caritas.cob.userservice.api.port.out;

import de.caritas.cob.userservice.api.model.Admin;
import org.springframework.data.repository.CrudRepository;

public interface AdminAgencyRepository extends CrudRepository<Admin, String> {}
