package io.crnk.internal.boot.cdi;

import com.github.cschabl.cdiunit.junit5.CdiUnitExtension;
import io.crnk.cdi.internal.CdiTransactionRunner;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.module.discovery.DefaultServiceDiscoveryFactory;
import io.crnk.core.module.discovery.ServiceDiscovery;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.TransactionalException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@ExtendWith(CdiUnitExtension.class)
@ApplicationScoped
public class CdiTransactionRunnerTest {


	private TransactionRunner runner;

	@BeforeEach
	public void setup() {
		DefaultServiceDiscoveryFactory factory = new DefaultServiceDiscoveryFactory();
		ServiceDiscovery instance = factory.getInstance();
		List<TransactionRunner> runners = instance.getInstancesByType(TransactionRunner.class);
		Assertions.assertEquals(1, runners.size());
		runner = runners.get(0);
	}

	@Test
	public void test() throws Exception {
		Callable callable = Mockito.mock(Callable.class);
		runner.doInTransaction(callable);

		Mockito.verify(callable, Mockito.times(1)).call();
	}


	@Test
	public void testHasPublicNoArgConstructor() {
		Assertions.assertNotNull(new CdiTransactionRunner());
	}


	@Test
	public void testTransactionalRuntimeExceptionToBeUnwrapped() throws Exception {
		Callable callable = Mockito.mock(Callable.class);
		Mockito.when(callable.call()).thenThrow(new TransactionalException("a", new IllegalStateException("b")));
		try {
			runner.doInTransaction(callable);
			Assertions.fail();
		} catch (IllegalStateException e) {
			Assertions.assertEquals("b", e.getMessage());
		}
		Mockito.verify(callable, Mockito.times(1)).call();
	}


	@Test
	public void testTransactionalExceptionNotToBeUnwrapped() throws Exception {
		Callable callable = Mockito.mock(Callable.class);
		Mockito.when(callable.call()).thenThrow(new TransactionalException("a", new IOException("b")));
		try {
			runner.doInTransaction(callable);
			Assertions.fail();
		} catch (TransactionalException e) {
			Assertions.assertEquals("a", e.getMessage());
		}
		Mockito.verify(callable, Mockito.times(1)).call();
	}

	@Test
	public void testRuntimeExceptionToPassThrough() throws Exception {
		Callable callable = Mockito.mock(Callable.class);
		Mockito.when(callable.call()).thenThrow(new IllegalStateException("b"));
		try {
			runner.doInTransaction(callable);
			Assertions.fail();
		} catch (IllegalStateException e) {
			Assertions.assertEquals("b", e.getMessage());
		}
		Mockito.verify(callable, Mockito.times(1)).call();
	}

	@Test
	public void testExceptionToBeWrapped() throws Exception {
		Callable callable = Mockito.mock(Callable.class);
		Mockito.when(callable.call()).thenThrow(new Exception("b"));
		try {
			runner.doInTransaction(callable);
			Assertions.fail();
		} catch (IllegalStateException e) {
			Assertions.assertEquals("b", e.getCause().getMessage());
		}
		Mockito.verify(callable, Mockito.times(1)).call();
	}
}
