package io.crnk.reactive;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ReactiveModuleAccessorTest {

	@Test
	public void check() {
		ReactiveModule module = new ReactiveModule();
		Assert.assertEquals("reactive", module.getModuleName());

		// RCS Deprecated https://github.com/reactor/reactor-core/issues/1893 
		//Assert.assertEquals(Schedulers.elastic(), module.getWorkerScheduler());
		Assert.assertEquals(Schedulers.boundedElastic(), module.getWorkerScheduler());

		Scheduler scheduler = Mockito.mock(Scheduler.class);
		module.setWorkerScheduler(scheduler);
		Assert.assertSame(scheduler, module.getWorkerScheduler());
	}
}
