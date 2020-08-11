package de.caritas.cob.userservice.api.helper;

import java.util.Date;
import org.springframework.stereotype.Service;

@Service
public class Now {

  public Date getDate() {
    return new Date();
  }

}
