package io.crnk.meta.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Iterator;

public class MetaAttributePathTest {

	private MetaAttributePath path;

	private MetaAttribute attr1;

	private MetaAttribute attr2;

	@BeforeEach
	public void setup() {
		attr1 = Mockito.mock(MetaAttribute.class);
		attr2 = Mockito.mock(MetaAttribute.class);
		Mockito.when(attr1.getName()).thenReturn("a");
		Mockito.when(attr2.getName()).thenReturn("b");

		path = new MetaAttributePath(Arrays.asList(attr1, attr2));
	}


	@Test
	public void invalidConstructorArgumentsThrowsException() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {		
		new MetaAttributePath((MetaAttribute[]) null);
		});
	}


	@Test
	public void concat() {
		MetaAttribute attr3 = Mockito.mock(MetaAttribute.class);
		Mockito.when(attr3.getName()).thenReturn("c");
		Assertions.assertEquals("a.b.c", path.concat(attr3).toString());
	}


	@Test
	public void toStringForEmptyPath() {
		Assertions.assertEquals("", MetaAttributePath.EMPTY_PATH.toString());
	}

	@Test
	public void toStringForSingleAttributePath() {
		MetaAttribute attr3 = Mockito.mock(MetaAttribute.class);
		Mockito.when(attr3.getName()).thenReturn("c");
		path = new MetaAttributePath(Arrays.asList(attr3));
		Assertions.assertEquals("c", path.toString());
	}


	@Test
	public void testHashCode() {
		MetaAttribute attr3 = Mockito.mock(MetaAttribute.class);
		Mockito.when(attr3.getName()).thenReturn("c");
		MetaAttributePath path2 = new MetaAttributePath(Arrays.asList(attr3));
		MetaAttributePath path3 = new MetaAttributePath(Arrays.asList(attr3));

		Assertions.assertNotEquals(path2.hashCode(), path.hashCode());
		Assertions.assertEquals(path2.hashCode(), path3.hashCode());
	}


	@Test
	public void length() {
		Assertions.assertEquals(2, path.length());
	}

	@Test
	public void getLast() {
		Assertions.assertEquals(attr2, path.getLast());
	}


	@Test
	public void iterator() {
		Iterator<MetaAttribute> iterator = path.iterator();
		Assertions.assertTrue(iterator.hasNext());
		Assertions.assertEquals("a", iterator.next().getName());
		Assertions.assertEquals("b", iterator.next().getName());
		Assertions.assertFalse(iterator.hasNext());
	}


	@Test
	public void getLastForEmptyPath() {
		Assertions.assertNull(MetaAttributePath.EMPTY_PATH.getLast());
	}


	@Test
	public void getElement() {
		Assertions.assertEquals(attr1, path.getElement(0));
		Assertions.assertEquals(attr2, path.getElement(1));
	}


	@Test
	public void subPath() {
		MetaAttributePath subPath = path.subPath(1);
		Assertions.assertEquals(1, subPath.length());
		Assertions.assertEquals(attr2, subPath.getElement(0));
	}

	@Test
	public void subRangePath() {
		MetaAttributePath subPath = path.subPath(1, 2);
		Assertions.assertEquals(1, subPath.length());
		Assertions.assertEquals(attr2, subPath.getElement(0));
	}


	@Test
	public void render() {
		Assertions.assertEquals("a.b", path.toString());
	}

	@Test
	public void equals() {
		Assertions.assertTrue(path.equals(path));
		Assertions.assertFalse(path.equals(new Object()));
	}
}
