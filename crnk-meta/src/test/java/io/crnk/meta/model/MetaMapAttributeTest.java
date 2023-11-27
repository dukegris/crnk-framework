package io.crnk.meta.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class MetaMapAttributeTest {

	private MetaMapAttribute impl;

	private MetaAttribute mapAttr;

	private MetaMapType mapType;

	private MetaDataObject parent;

	@BeforeEach
	public void setup() {
		String keyString = "13";
		mapAttr = Mockito.mock(MetaAttribute.class);
		mapType = Mockito.mock(MetaMapType.class);
		impl = new MetaMapAttribute(mapType, mapAttr, keyString);
		parent = Mockito.mock(MetaDataObject.class);
		impl.setParent(parent);
	}

	@Test
	public void testGetters() {
		Assertions.assertEquals(mapAttr, impl.getMapAttribute());
		Assertions.assertEquals(parent, impl.getParent());
		Assertions.assertEquals(mapType, impl.getType());
	}

	@Test
	public void getKey() {
		MetaType keyType = Mockito.mock(MetaType.class);
		Mockito.when(keyType.getImplementationClass()).thenReturn((Class) Integer.class);
		Mockito.when(mapType.getKeyType()).thenReturn(keyType);

		Assertions.assertEquals(Integer.valueOf(13), impl.getKey());
	}

	@Test
	public void checkForwardIsLazy() {
		impl.isLazy();
		Mockito.verify(mapAttr, Mockito.times(1)).isLazy();
	}

	@Test
	public void checkForwardIsDerived() {
		impl.isDerived();
		Mockito.verify(mapAttr, Mockito.times(1)).isDerived();
	}


	@Test
	public void checkGetAnnotationsNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.getAnnotations();
		});
	}


	@Test
	public void checkGetAnnotationNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.getAnnotation(null);
		});
	}


	@Test
	public void checkSetOppositeAttributeNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.setOppositeAttribute(null);
		});
	}


	@Test
	public void checkForwardIsAssociation() {
		impl.isAssociation();
		Mockito.verify(mapAttr, Mockito.times(1)).isAssociation();
	}

	@Test
	public void getIdNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.getId();
		});
	}

	@Test
	public void getVersionNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.isVersion();
		});
	}

	@Test
	public void isIdNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.isId();
		});
	}

	@Test
	public void getOppositeAttributeNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.getOppositeAttribute();
		});
	}

	@Test
	public void getValueNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.getValue(null);
		});
	}

	@Test
	public void addValueNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.addValue(null, null);
		});
	}

	@Test
	public void removeValueNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.removeValue(null, null);
		});
	}

	@Test
	public void setValueNotSupported() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {		
			impl.setValue(null, null);
		});
	}
}
