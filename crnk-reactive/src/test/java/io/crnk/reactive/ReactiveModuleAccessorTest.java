package io.crnk.reactive;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ReactiveModuleAccessorTest {

	@Test
	public void check() {
		ReactiveModule module = new ReactiveModule();
		Assertions.assertEquals("reactive", module.getModuleName());

		Assertions.assertEquals(Schedulers.elastic(), module.getWorkerScheduler());

		Scheduler scheduler = Mockito.mock(Scheduler.class);
		module.setWorkerScheduler(scheduler);
		Assertions.assertSame(scheduler, module.getWorkerScheduler());
	}
}
