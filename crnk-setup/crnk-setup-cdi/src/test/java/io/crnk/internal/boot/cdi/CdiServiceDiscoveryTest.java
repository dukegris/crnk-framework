package io.crnk.internal.boot.cdi;

import com.github.cschabl.cdiunit.junit5.CdiUnitExtension;
import io.crnk.cdi.internal.CdiServiceDiscovery;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.module.discovery.DefaultServiceDiscoveryFactory;
import io.crnk.core.module.discovery.ServiceDiscovery;
import io.crnk.core.repository.Repository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.internal.boot.cdi.model.CdiTestExceptionMapper;
import io.crnk.internal.boot.cdi.model.ProjectRepository;
import io.crnk.internal.boot.cdi.model.TaskRepository;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import java.util.List;
import java.util.Optional;

@ExtendWith(CdiUnitExtension.class)
@ApplicationScoped
public class CdiServiceDiscoveryTest {

	@Test
	public void testSetter() {
		CdiServiceDiscovery discovery = new CdiServiceDiscovery();

		BeanManager beanManager = discovery.getBeanManager();
		Assertions.assertNotNull(discovery.getBeanManager());

		BeanManager mock = Mockito.mock(BeanManager.class);
		discovery.setBeanManager(mock);
		Assertions.assertSame(mock, discovery.getBeanManager());
		Assertions.assertNotSame(mock, beanManager);
	}

	@Test
	public void testFactory() {
		DefaultServiceDiscoveryFactory factory = new DefaultServiceDiscoveryFactory();
		ServiceDiscovery instance = factory.getInstance();
		Assertions.assertNotNull(instance);
		Assertions.assertEquals(CdiServiceDiscovery.class, instance.getClass());

		List<?> repositories = instance.getInstancesByType(Repository.class);
		Assertions.assertEquals(1, repositories.size());
		Assertions.assertTrue(repositories.get(0) instanceof ProjectRepository);

		repositories = instance.getInstancesByAnnotation(JsonApiExposed.class);
		Assertions.assertEquals(1, repositories.size());
		Assertions.assertTrue(repositories.get(0) instanceof TaskRepository);
	}

	@Test
	public void testExceptionMapper() {
		CrnkBoot boot = new CrnkBoot();
		boot.boot();

		Optional<ExceptionMapper> mapper = boot.getExceptionMapperRegistry().findMapperFor(IllegalStateException.class);
		Assertions.assertTrue(mapper.isPresent());
		Assertions.assertTrue(mapper.get() instanceof CdiTestExceptionMapper);
	}
}
