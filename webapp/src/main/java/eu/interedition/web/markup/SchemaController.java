package eu.interedition.web.markup;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Controller
@RequestMapping("/schema")
public class SchemaController {

    @RequestMapping(method = RequestMethod.POST)
    public void upload(MultipartFile schema) {
        if (!schema.isEmpty()) {

        }
    }
    @RequestMapping
    public String index() {
        return "schema";
    }
}
