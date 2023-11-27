package io.crnk.spring.app;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.crnk.spring.setup.boot.core.CrnkBootConfigurer;
import io.crnk.spring.setup.boot.data.facet.FacetModuleConfigurer;
import io.crnk.spring.setup.boot.jpa.JpaModuleConfigurer;
import io.crnk.spring.setup.boot.meta.MetaModuleConfigurer;
import io.crnk.test.mock.TestModule;

@Configuration
public class ModuleConfig {

	@Bean
	public TestModule testModule() {
		return new TestModule();
	}

	@Bean
	public MetaModuleConfigurer metaModuleConfigurer() {
		return Mockito.mock(MetaModuleConfigurer.class);
	}

	@Bean
	public JpaModuleConfigurer jpaModujleConfigurer() {
		return Mockito.mock(JpaModuleConfigurer.class);
	}

	@Bean
	public FacetModuleConfigurer facetModuleConfigurer() {
		return Mockito.mock(FacetModuleConfigurer.class);
	}

	@Bean
	public CrnkBootConfigurer bootConfigurer() {
		return boot -> boot.putServerInfo("vendor", "crnk");
	}
}
