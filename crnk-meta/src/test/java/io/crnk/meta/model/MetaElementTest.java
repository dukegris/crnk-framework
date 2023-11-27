package io.crnk.meta.model;

import io.crnk.meta.AbstractMetaTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetaElementTest extends AbstractMetaTest {

	@Test
	public void checkDataObjectCast() {
		Assertions.assertThrows(IllegalStateException.class, () -> {		
		new MetaKey().asDataObject();
		});
	}

	@Test
	public void checkTypeCast() {
		Assertions.assertThrows(IllegalStateException.class, () -> {		
		new MetaKey().asType();
		});
	}
}
