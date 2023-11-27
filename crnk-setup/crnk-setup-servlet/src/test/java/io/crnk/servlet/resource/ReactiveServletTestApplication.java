package io.crnk.servlet.resource;

import io.crnk.client.CrnkClient;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.SimpleModule;
import io.crnk.reactive.ReactiveModule;
import io.crnk.servlet.AsyncCrnkServlet;
import io.crnk.servlet.reactive.model.SlowResourceRepository;
import io.crnk.test.mock.ClientTestModule;
import io.crnk.test.mock.reactive.ReactiveTestModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// RCS Spring port
// import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@RestController
@SpringBootApplication
// RCS Spring port
// public class ReactiveServletTestApplication implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {
public class ReactiveServletTestApplication implements ApplicationListener<ServletWebServerInitializedEvent> {

	private int port;

	private CrnkClient client;


	private ReactiveTestModule testModule = new ReactiveTestModule();

	@Autowired
	private CrnkBoot boot;

	@Override
	// RCS Spring port
	// public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
	public void onApplicationEvent(ServletWebServerInitializedEvent event) {
		// RCS Spring port
		// port = event.getEmbeddedServletContainer().getPort();
		port = event.getWebServer().getPort();
		client = new CrnkClient("http://localhost:" + port + "/api");
		client.addModule(new ClientTestModule());
	}

	public static void main(String[] args) {
		SpringApplication.run(ReactiveServletTestApplication.class, args);
	}

	@Bean
	public SlowResourceRepository slowRepository() {
		return new SlowResourceRepository();
	}

	@Bean
	public ReactiveServletTestContainer testContainer() {
		return new ReactiveServletTestContainer(testModule, () -> client, boot);
	}

	// tag::reactive[]
	@Bean
	public AsyncCrnkServlet asyncCrnkServlet(SlowResourceRepository slowResourceRepository) {
		SimpleModule slowModule = new SimpleModule("slow");
		slowModule.addRepository(slowResourceRepository);

		AsyncCrnkServlet servlet = new AsyncCrnkServlet();
		servlet.getBoot().addModule(new ReactiveModule());
		servlet.getBoot().addModule(testModule);
		servlet.getBoot().addModule(slowModule);

		return servlet;
	}

	@Bean
	public ServletRegistrationBean crnkServletRegistration(AsyncCrnkServlet servlet) {
		ServletRegistrationBean bean = new ServletRegistrationBean(servlet, "/api/*");
		bean.setLoadOnStartup(1);
		return bean;
	}

	@Bean
	public CrnkBoot crnkBoot(AsyncCrnkServlet servlet) {
		return servlet.getBoot();
	}
	// end::reactive[]

}
