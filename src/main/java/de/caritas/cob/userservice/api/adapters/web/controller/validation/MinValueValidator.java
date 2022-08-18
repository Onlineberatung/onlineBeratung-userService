package de.caritas.cob.userservice.api.adapters.web.controller.validation;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Checks if parameter has value greater than or equal the given value and throws a 400 - Bad
 * Request if not.
 */
public class MinValueValidator implements HandlerMethodArgumentResolver {

  /**
   * Checks if parameter can be resolved.
   *
   * @param methodParameter the parameter annotated with {@link MinValue}
   * @return true if parameter is an {@link Integer} annotated with {@link MinValue}
   */
  @Override
  public boolean supportsParameter(MethodParameter methodParameter) {
    return nonNull(methodParameter.getParameterAnnotation(MinValue.class))
        && Integer.class.equals(methodParameter.getNestedParameterType());
  }

  /**
   * Resolves the argument and throws a {@link BadRequestException} if validation fails.
   *
   * @param methodParameter the parameter to be resolved
   * @param modelAndViewContainer the current container
   * @param nativeWebRequest the current request
   * @param webDataBinderFactory the current data factory
   * @return the {@link Integer} value of the parameter
   */
  @Override
  public Integer resolveArgument(
      MethodParameter methodParameter,
      ModelAndViewContainer modelAndViewContainer,
      NativeWebRequest nativeWebRequest,
      WebDataBinderFactory webDataBinderFactory) {

    MinValue minValue = methodParameter.getParameterAnnotation(MinValue.class);
    String parameter = nativeWebRequest.getParameter(methodParameter.getParameter().getName());
    if (isNull(parameter) || Integer.parseInt(parameter) < requireNonNull(minValue).value()) {
      throw new BadRequestException(minValue.message());
    }

    return Integer.parseInt(parameter);
  }
}
