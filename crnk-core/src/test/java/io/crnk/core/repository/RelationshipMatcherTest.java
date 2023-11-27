package io.crnk.core.repository;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RelationshipMatcherTest {

	private ResourceField field;

	@BeforeEach
	public void setup() {
		ResourceInformation resource = Mockito.mock(ResourceInformation.class);
		Mockito.when(resource.getResourceClass()).thenReturn((Class) String.class);
		Mockito.when(resource.getResourceType()).thenReturn("resource");

		field = Mockito.mock(ResourceField.class);
		Mockito.when(field.getResourceFieldType()).thenReturn(ResourceFieldType.RELATIONSHIP);
		Mockito.when(field.getUnderlyingName()).thenReturn("fieldName");
		Mockito.when(field.getOppositeName()).thenReturn("oppositeFieldName");
		Mockito.when(field.getOppositeResourceType()).thenReturn("oppositeResourceType");
		Mockito.when(field.getElementType()).thenReturn((Class) Integer.class);
		Mockito.when(field.getElementType()).thenReturn((Class) Integer.class);
		Mockito.when(field.getResourceInformation()).thenReturn(resource);
	}

	@Test
	public void checkEmpty() {
		Assertions.assertFalse(new RelationshipMatcher().matches(field));
	}

	@Test
	public void checkMatchTargetResourceType() {
		Assertions.assertTrue(new RelationshipMatcher().rule().target(Integer.class).add().matches(field));
	}

	@Test
	public void checkNotMatchTargetResourceType() {
		Assertions.assertFalse(new RelationshipMatcher().rule().target(Long.class).add().matches(field));
	}

	@Test
	public void checkMatchTargetClass() {
		Assertions.assertTrue(new RelationshipMatcher().rule().target("oppositeResourceType").add().matches(field));
	}

	@Test
	public void checkNotMatchTargetClass() {
		Assertions.assertFalse(new RelationshipMatcher().rule().target("notAMatch").add().matches(field));
	}


	@Test
	public void checkMatchSourceResourceType() {
		Assertions.assertTrue(new RelationshipMatcher().rule().source(String.class).add().matches(field));
	}

	@Test
	public void checkNotMatchSourceResourceType() {
		Assertions.assertFalse(new RelationshipMatcher().rule().source(Long.class).add().matches(field));
	}

	@Test
	public void checkMatchSourceClass() {
		Assertions.assertTrue(new RelationshipMatcher().rule().source("resource").add().matches(field));
	}

	@Test
	public void checkNotMatchSourceClass() {
		Assertions.assertFalse(new RelationshipMatcher().rule().source("notAMatch").add().matches(field));
	}

	@Test
	public void checkMatchField() {
		Assertions.assertTrue(new RelationshipMatcher().rule().field("fieldName").add().matches(field));
	}

	@Test
	public void checkNotMatchField() {
		Assertions.assertFalse(new RelationshipMatcher().rule().field("notAMatch").add().matches(field));
	}

	@Test
	public void checkMatchOppositeField() {
		Assertions.assertTrue(new RelationshipMatcher().rule().oppositeField("oppositeFieldName").add().matches(field));
	}

	@Test
	public void checkNotMatchOppositeField() {
		Assertions.assertFalse(new RelationshipMatcher().rule().oppositeField("notAMatch").add().matches(field));
	}
}
