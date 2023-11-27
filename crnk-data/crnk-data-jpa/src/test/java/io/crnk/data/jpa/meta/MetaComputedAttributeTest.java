package io.crnk.data.jpa.meta;

import io.crnk.data.jpa.internal.query.MetaComputedAttribute;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetaComputedAttributeTest {


	@Test
	public void getValueNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		MetaComputedAttribute attr = new MetaComputedAttribute();
		attr.getValue(null);
		});
	}

	@Test
	public void setValueNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
		MetaComputedAttribute attr = new MetaComputedAttribute();
		attr.setValue(null, null);
		});
	}
}
