package de.caritas.cob.userservice.api.adapters.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.swagger.web.ApiResourceController;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

@Controller
@ApiIgnore
@RequestMapping(value = "${springfox.docuPath}" + "/swagger-resources")
public class CustomSwaggerUIApiResourceController extends ApiResourceController {

  public CustomSwaggerUIApiResourceController(SwaggerResourcesProvider swaggerResources) {
    super(swaggerResources);
  }
}
