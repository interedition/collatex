package eu.interedition.collatex2.spring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.implementation.tokenization.DefaultTokenNormalizer;
import eu.interedition.collatex2.interfaces.ITokenNormalizer;

@Controller
@RequestMapping("/api/**")
public class ApiController {
  private ITokenNormalizer defaultNormalizer = new DefaultTokenNormalizer();

  @RequestMapping("collate")
  public ModelMap collate(@RequestBody final ApiInput input) throws Exception {
    List<ApiWitness> witnesses = postProcess(input).getWitnesses();
    return new ModelMap("alignment", new CollateXEngine().align(witnesses.toArray(new ApiWitness[witnesses.size()])));
  }

  private ApiInput postProcess(ApiInput input) throws ApiException {
    Set<String> sigle = new HashSet<String>();
    for (ApiWitness witness : input.getWitnesses()) {
      String sigil = witness.getSigil();
      if (sigil == null) {
        throw new ApiException("Witness without id/sigil given");
      }
      if (sigle.contains(sigil)) {
        throw new ApiException("Duplicate id/sigil: " + sigil);
      }
      sigle.add(sigil);

      int tokenPosition = 0;
      for (ApiToken token : witness.getApiTokens()) {
        if (token.getContent() == null || token.getContent().trim().length() == 0) {
          throw new ApiException("Empty token in " + sigil);
        }
        token.setSigil(sigil);
        token.setPosition(++tokenPosition);
        if (token.getNormalized() == null || token.getNormalized().trim().length() == 0) {
          token.setNormalized(defaultNormalizer.apply(token).getNormalized());
        }
      }
    }
    return input;
  }
  
  @ExceptionHandler(ApiException.class)
  public ModelAndView apiError(HttpServletResponse response, ApiException exception) {
    return new ModelAndView(new MappingJacksonJsonView(), new ModelMap("error", exception.getMessage()));
  }
}
