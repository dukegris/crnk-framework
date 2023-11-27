package io.crnk.core.queryspec.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

public class LegacyQuerySpecUrlMapperDeserializerTest extends DefaultQuerySpecUrlMapperDeserializerTestBase {

	@BeforeEach
	public void setup() {
		super.setup();
		urlMapper.setEnforceDotPathSeparator(false);
	}

	@Test
	//@Disabled // not support on old filter
	@Disabled
	public void testFilterOnRelatedWithJson() {
		super.testFilterOnRelatedWithJson();
	}
}
