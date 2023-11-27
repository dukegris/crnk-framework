package io.crnk.core.queryspec;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.exception.BadRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilterOperatorTest {


	@Test
	public void andMatchNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		FilterOperator.AND.matches(null, null);
		});
	}

	@Test
	public void notMatchNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		FilterOperator.NOT.matches(null, null);
		});
	}

	@Test
	public void orMatchNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		FilterOperator.OR.matches(null, null);
		});
	}

	@Test
	public void testLEOperator() {
		Assertions.assertTrue(FilterOperator.LE.matches("a", "b"));
	}

	@Test
	public void testSerialization() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		String json = objectMapper.writerFor(FilterOperator.class).writeValueAsString(FilterOperator.EQ);
		Assertions.assertEquals("\"EQ\"", json);

		FilterOperator operator = objectMapper.readerFor(FilterOperator.class).readValue(json);
		Assertions.assertEquals(FilterOperator.EQ, operator);
	}

	@Test
	public void testLikeOperator() {
		Assertions.assertTrue(FilterOperator.LIKE.matches("test", "te%"));
		Assertions.assertTrue(FilterOperator.LIKE.matches("test", "Te%"));
		Assertions.assertTrue(FilterOperator.LIKE.matches("test", "tE%"));
		Assertions.assertFalse(FilterOperator.LIKE.matches("test", "aE%"));
		Assertions.assertTrue(FilterOperator.LIKE.matches("test", "t%t"));
		Assertions.assertTrue(FilterOperator.LIKE.matches("test.", "t%."));
		Assertions.assertFalse(FilterOperator.LIKE.matches(".", "t"));
		Assertions.assertTrue(FilterOperator.LIKE.matches(".", "."));

		Assertions.assertFalse(FilterOperator.LIKE.matches(".", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches(".", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches("[", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches("\\", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches("^", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches("$", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches("|", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches("?", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches(")", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches("(", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches("+", "t"));
		Assertions.assertFalse(FilterOperator.LIKE.matches("*", "t"));

		Assertions.assertTrue(FilterOperator.LIKE.matches(".", "."));
		Assertions.assertTrue(FilterOperator.LIKE.matches("[", "["));
		Assertions.assertTrue(FilterOperator.LIKE.matches("\\", "\\"));
		Assertions.assertTrue(FilterOperator.LIKE.matches("^", "^"));
		Assertions.assertTrue(FilterOperator.LIKE.matches("$", "$"));
		Assertions.assertTrue(FilterOperator.LIKE.matches("|", "|"));
		Assertions.assertTrue(FilterOperator.LIKE.matches("?", "?"));
		Assertions.assertTrue(FilterOperator.LIKE.matches(")", ")"));
		Assertions.assertTrue(FilterOperator.LIKE.matches("(", "("));
		Assertions.assertTrue(FilterOperator.LIKE.matches("+", "+"));
		Assertions.assertTrue(FilterOperator.LIKE.matches("*", "*"));
		Assertions.assertFalse(FilterOperator.LIKE.matches(null, "*"));
	}

	@Test
	public void testLikeOperatorUsesStringType() {
		Assertions.assertEquals(String.class, FilterOperator.LIKE.getFilterType(null, Integer.class));
	}

	@Test
	public void testLikeWithoutPattern() {
		Assertions.assertThrows(BadRequestException.class, () -> {
		FilterOperator.LIKE.matches("test", null);
		});
	}


	@Test
	public void testDefaultOperatorsUsesSameType() {
		Assertions.assertEquals(Integer.class, FilterOperator.EQ.getFilterType(null, Integer.class));
		Assertions.assertEquals(Integer.class, FilterOperator.GT.getFilterType(null, Integer.class));
		Assertions.assertEquals(Integer.class, FilterOperator.GE.getFilterType(null, Integer.class));
		Assertions.assertEquals(Boolean.class, FilterOperator.LT.getFilterType(null, Boolean.class));
		Assertions.assertEquals(Long.class, FilterOperator.LE.getFilterType(null, Long.class));
	}

	@Test
	public void testEquals() {
		Assertions.assertEquals(FilterOperator.AND, FilterOperator.AND);
		Assertions.assertNotEquals(FilterOperator.AND, "notAnOperator");
		Assertions.assertNotEquals(FilterOperator.AND, null);
		Assertions.assertEquals(FilterOperator.OR, FilterOperator.OR);
		Assertions.assertEquals(FilterOperator.OR, new FilterOperator("OR") {

			@Override
			public boolean matches(Object value1, Object value2) {
				return false;
			}
		});
		Assertions.assertNotEquals(FilterOperator.AND, FilterOperator.OR);
	}
}
