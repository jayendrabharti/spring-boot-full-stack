package com.jayendrabharti.springbootfullstack;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Frontend {

    private final Environment environment;

    public Frontend(Environment environment) {
        this.environment = environment;
    }

    @RequestMapping(value = {
        "/",
        "/{path:^(?!api$|swagger-ui$|v3$)[^\\.]+$}",
        "/**/{path:^(?!api$|swagger-ui$|v3$)[^\\.]+$}"
    })
    public String forward() {

        if (environment.matchesProfiles("dev")) {
            return "redirect:http://localhost:5173/";
        }

        return "forward:/index.html";
    }
}
