package de.caritas.cob.userservice.api.config.apiclient;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import de.caritas.cob.userservice.api.exception.httpresponses.InternalServerErrorException;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class TenantServiceHelper {

  private static final String FILTER_NAME = "filter";

  private TenantServiceHelper() { // hide public constructor
  }

  public static boolean noValidFilterParams(String queryName, Object queryValue) {
    return isEmpty(queryName) || !queryName.equals(FILTER_NAME) || isNull(queryValue);
  }

  public static MultiValueMap<String, String> obtainQueryParameters(Object queryValue) {
    MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();

    try {
      Arrays.asList(
              Introspector.getBeanInfo(queryValue.getClass(), Object.class)
                  .getPropertyDescriptors())
          .stream()
          .filter(descriptor -> nonNull(descriptor.getReadMethod()))
          .forEach(descriptor -> setMethodKeyValuePairs(queryValue, paramMap, descriptor));
      return paramMap;

    } catch (IntrospectionException exception) {
      throw new InternalServerErrorException(
          String.format("Could not obtain method properties of %s", queryValue.toString()),
          exception);
    }
  }

  private static void setMethodKeyValuePairs(
      Object queryValue, MultiValueMap<String, String> map, PropertyDescriptor descriptor) {
    try {
      Object value = descriptor.getReadMethod().invoke(queryValue);
      if (nonNull(value)) {
        map.add(descriptor.getName(), value.toString());
      }
    } catch (Exception exception) {
      throw new InternalServerErrorException(
          String.format("Could not obtain method key value pairs of %s", queryValue.toString()),
          exception);
    }
  }
}
