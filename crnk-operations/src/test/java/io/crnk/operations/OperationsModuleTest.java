package io.crnk.operations;

import io.crnk.operations.server.OperationFilter;
import io.crnk.operations.server.OperationsModule;
import io.crnk.operations.server.order.OperationOrderStrategy;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class OperationsModuleTest extends AbstractOperationsTest {

	private OperationsModule module = OperationsModule.create();

	@Test
	public void testName() {
		Assertions.assertEquals("operations", module.getModuleName());
	}

	@Test
	public void hasProtectedConstructor() {
		ClassTestUtils.assertProtectedConstructor(OperationsModule.class);
	}

	@Test
	public void testRemoveFilter() {
		OperationFilter filter = Mockito.mock(OperationFilter.class);

		module.addFilter(filter);
		Assertions.assertEquals(1, module.getFilters().size());
		module.removeFilter(filter);
		Assertions.assertEquals(0, module.getFilters().size());
	}

	@Test
	public void testSetOrderStrategy() {
		OperationOrderStrategy strategy = Mockito.mock(OperationOrderStrategy.class);
		module.setOrderStrategy(strategy);
		Assertions.assertSame(strategy, module.getOrderStrategy());
	}

}
