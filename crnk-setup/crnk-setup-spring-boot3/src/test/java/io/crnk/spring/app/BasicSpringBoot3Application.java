package io.crnk.spring.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import io.crnk.spring.setup.boot.data.facet.CrnkFacetAutoConfiguration;
import io.crnk.spring.setup.boot.format.PlainJsonFormatAutoConfiguration;
import io.crnk.spring.setup.boot.home.CrnkHomeAutoConfiguration;
import io.crnk.spring.setup.boot.jpa.CrnkJpaAutoConfiguration;
import io.crnk.spring.setup.boot.meta.CrnkMetaAutoConfiguration;
import io.crnk.spring.setup.boot.mvc.CrnkSpringMvcAutoConfiguration;
import io.crnk.spring.setup.boot.operations.CrnkOperationsProperties;
import io.crnk.spring.setup.boot.security.CrnkSecurityAutoConfiguration;
import io.crnk.spring.setup.boot.security.CrnkSpringSecurityAutoConfiguration;
import io.crnk.spring.setup.boot.ui.CrnkUIAutoConfiguration;
import io.crnk.spring.setup.boot.validation.CrnkValidationAutoConfiguration;

@Configuration
@RestController
@EnableAutoConfiguration
@Import({CrnkCoreAutoConfiguration.class, CrnkJpaAutoConfiguration.class, ModuleConfig.class,
CrnkUIAutoConfiguration.class, CrnkHomeAutoConfiguration.class, CrnkOperationsProperties.class,
CrnkValidationAutoConfiguration.class, CrnkMetaAutoConfiguration.class, CrnkFacetAutoConfiguration.class,
CrnkSpringMvcAutoConfiguration.class, PlainJsonFormatAutoConfiguration.class})
//CrnkSpringSecurityAutoConfiguration.class, CrnkSecurityAutoConfiguration.class})
public class BasicSpringBoot3Application {

	public static void main(String[] args) {
		SpringApplication.run(BasicSpringBoot3Application.class, args);
	}

	@RequestMapping("/api/custom")
	public String customMethod() {
		return "hello";
	}
}
