package io.crnk.core.engine.result;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImmediateResultTest {

	private ImmediateResultFactory resultFactory = new ImmediateResultFactory();


	@Test
	public void checkNotAsync() {
		Assertions.assertFalse(resultFactory.isAsync());

	}

	@Test
	public void checkContextAccess() throws ExecutionException, InterruptedException {
		Object context = new Object();
		Assertions.assertFalse(resultFactory.hasThreadContext());
		resultFactory.setThreadContext(context);
		Assertions.assertSame(context, resultFactory.getThreadContext());
		Assertions.assertTrue(resultFactory.hasThreadContext());

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {
			Future<?> future = executorService.submit(new Runnable() {
				@Override
				public void run() {
					Assertions.assertFalse(resultFactory.hasThreadContext());
				}
			});
			future.get();

			Assertions.assertFalse(resultFactory.isAsync());
		} finally {
			executorService.shutdownNow();
		}
	}

	@Test
	public void subscribeNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			Result<Object> result = resultFactory.just(new Object());
			result.subscribe(null, null);
		});
	}

	@Test
	public void onErrorResumeNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
			Result<Object> result = resultFactory.just(new Object());
			result.onErrorResume(null);
		});
	}
}
