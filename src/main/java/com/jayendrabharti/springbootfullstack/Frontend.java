package com.jayendrabharti.springbootfullstack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Frontend {

	@Autowired
	private Environment environment;

	@RequestMapping(value = { "/", "/{path:^(?!api$|swagger-ui$|v3$)[^\\.]+$}",
			"/**/{path:^(?!api$|swagger-ui$|v3$)[^\\.]+$}" })
	public String forward() {

		if (environment.matchesProfiles("dev")) {
			return "redirect:http://localhost:5173/";
		}

		return "forward:/index.html";
	}

}
