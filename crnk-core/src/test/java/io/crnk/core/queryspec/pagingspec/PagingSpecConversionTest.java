package io.crnk.core.queryspec.pagingspec;

import io.crnk.core.exception.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PagingSpecConversionTest {


	@Test
	public void checkWithOffsetAndLimit() {
		OffsetLimitPagingSpec offsetSpec = new OffsetLimitPagingSpec(20L, 10L);
		NumberSizePagingSpec numberSpec = offsetSpec.convert(NumberSizePagingSpec.class);
		Assertions.assertEquals(3, numberSpec.getNumber());
		Assertions.assertEquals(10, numberSpec.getSize().intValue());
		OffsetLimitPagingSpec convertedOffsetSpec = numberSpec.convert(OffsetLimitPagingSpec.class);
		Assertions.assertEquals(offsetSpec, convertedOffsetSpec);
	}

	@Test
	public void checkEmpty() {
		OffsetLimitPagingSpec offsetSpec = new OffsetLimitPagingSpec(0L, null);
		NumberSizePagingSpec numberSpec = offsetSpec.convert(NumberSizePagingSpec.class);
		Assertions.assertEquals(1, numberSpec.getNumber());
		Assertions.assertNull(numberSpec.getSize());
		OffsetLimitPagingSpec convertedOffsetSpec = numberSpec.convert(OffsetLimitPagingSpec.class);
		Assertions.assertEquals(offsetSpec, convertedOffsetSpec);
	}

	@Test
	public void checkWithLimit() {
		OffsetLimitPagingSpec offsetSpec = new OffsetLimitPagingSpec(0L, 10L);
		NumberSizePagingSpec numberSpec = offsetSpec.convert(NumberSizePagingSpec.class);
		Assertions.assertEquals(1, numberSpec.getNumber());
		Assertions.assertEquals(10, numberSpec.getSize().intValue());
		OffsetLimitPagingSpec convertedOffsetSpec = numberSpec.convert(OffsetLimitPagingSpec.class);
		Assertions.assertEquals(offsetSpec, convertedOffsetSpec);
	}

	@Test
	public void checkOffsetNotMultiple() {
		OffsetLimitPagingSpec offsetSpec = new OffsetLimitPagingSpec(5L, 10L);
		try {
			offsetSpec.convert(NumberSizePagingSpec.class);
		} catch (BadRequestException e) {
			Assertions.assertEquals("offset=5 must be multiple of limit=10 to support page number/size conversion", e.getMessage());
		}
	}
}
