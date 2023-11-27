package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.error.ExceptionMapper;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class ExceptionMapperTypeTest {

	@Test
	public void shouldFulfillHashCodeEqualsContract() {
		EqualsVerifier.forClass(ExceptionMapperType.class).verify();
	}

	@Test
	public void checkToString() {
		ExceptionMapper mapper = Mockito.mock(ExceptionMapper.class);
		Mockito.when(mapper.toString()).thenReturn("customMapper");
		ExceptionMapperType type = new ExceptionMapperType(IllegalStateException.class, mapper);
		Assertions.assertEquals("ExceptionMapperType[exceptionClass=java.lang.IllegalStateException, exceptionMapper=customMapper]", type.toString());
	}
}