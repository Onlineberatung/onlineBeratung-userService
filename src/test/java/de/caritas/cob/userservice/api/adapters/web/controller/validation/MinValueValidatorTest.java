package de.caritas.cob.userservice.api.adapters.web.controller.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.caritas.cob.userservice.api.exception.httpresponses.BadRequestException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

@RunWith(MockitoJUnitRunner.class)
public class MinValueValidatorTest {

  private final MinValueValidator minValueValidator = new MinValueValidator();

  @Mock private MethodParameter methodParameter;

  @Mock private NativeWebRequest nativeWebRequest;

  @Mock private MinValue minValue;

  @Mock private Parameter parameter;

  public MinValueValidatorTest()
      throws IllegalAccessException, InvocationTargetException, InstantiationException {}

  @Test
  public void supportsParameter_Should_ReturnFalse_When_methodParameterHasNoAnnotation() {
    boolean isSupported = minValueValidator.supportsParameter(methodParameter);

    assertThat(isSupported, is(false));
  }

  @Test
  public void supportsParameter_Should_ReturnFalse_When_methodParameterIsNotAInteger() {
    when(methodParameter.getParameterAnnotation(eq(MinValue.class))).thenReturn(minValue);

    boolean isSupported = minValueValidator.supportsParameter(methodParameter);

    assertThat(isSupported, is(false));
  }

  @Test
  public void supportsParameter_Should_ReturnTrue_When_methodParameterIsAInteger() {
    when(methodParameter.getParameterAnnotation(eq(MinValue.class))).thenReturn(minValue);
    doReturn(Integer.class).when(methodParameter).getNestedParameterType();

    boolean isSupported = minValueValidator.supportsParameter(methodParameter);

    assertThat(isSupported, is(true));
  }

  @Test
  public void resolveArgument_Should_ReturnOriginalValue_When_ParameterIsGreaterThanMinValue() {
    when(parameter.getName()).thenReturn("minValue");
    when(methodParameter.getParameter()).thenReturn(parameter);
    when(methodParameter.getParameterAnnotation(any())).thenReturn(minValue);
    when(nativeWebRequest.getParameter(any())).thenReturn("10");

    Integer result =
        minValueValidator.resolveArgument(methodParameter, null, nativeWebRequest, null);

    assertThat(result, is(10));
  }

  @Test(expected = BadRequestException.class)
  public void resolveArgument_Should_ThrowBadRequestException_When_ParameterIsNull() {
    when(parameter.getName()).thenReturn("minValue");
    when(methodParameter.getParameter()).thenReturn(parameter);
    when(methodParameter.getParameterAnnotation(any())).thenReturn(minValue);
    when(nativeWebRequest.getParameter(any())).thenReturn(null);

    minValueValidator.resolveArgument(methodParameter, null, nativeWebRequest, null);
  }

  @Test(expected = BadRequestException.class)
  public void resolveArgument_Should_ThrowBadRequestException_When_ParameterIsLowerThanMinValue() {
    when(parameter.getName()).thenReturn("minValue");
    when(methodParameter.getParameter()).thenReturn(parameter);
    when(methodParameter.getParameterAnnotation(any())).thenReturn(minValue);
    when(minValue.value()).thenReturn(11);
    when(nativeWebRequest.getParameter(any())).thenReturn("10");

    minValueValidator.resolveArgument(methodParameter, null, nativeWebRequest, null);
  }
}
