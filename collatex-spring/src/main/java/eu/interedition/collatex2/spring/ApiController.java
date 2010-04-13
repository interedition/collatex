package eu.interedition.collatex2.spring;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/**")
public class ApiController {

  @RequestMapping("collate")
  public void collate(ModelMap model) {
    model.addAttribute("hello", "world");
  }
}
