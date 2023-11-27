package io.crnk.meta;

import java.util.Date;
import java.util.UUID;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.test.mock.models.PrimitiveAttributeResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class MetaLookupTest extends AbstractMetaTest {

	@Test
	public void testOptionalAttribute() {
		MetaResource meta = resourceProvider.getMeta(PrimitiveAttributeResource.class);
		MetaAttribute attribute = meta.getAttribute("optionalValue");
		Assertions.assertEquals("resources.primitiveAttribute.optionalValue", attribute.getId());
		Assertions.assertEquals(String.class, attribute.getType().getImplementationClass());
	}

	@Test
	public void testPrimitiveFloat() {
		MetaType meta = resourceProvider.getMeta(Float.class);
		Assertions.assertEquals("base.float", meta.getId());
		Assertions.assertEquals(Float.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(float.class);
		Assertions.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveDouble() {
		MetaType meta = resourceProvider.getMeta(Double.class);
		Assertions.assertEquals("base.double", meta.getId());
		Assertions.assertEquals(Double.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(double.class);
		Assertions.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveByte() {
		MetaType meta = resourceProvider.getMeta(Byte.class);
		Assertions.assertEquals("base.byte", meta.getId());
		Assertions.assertEquals(Byte.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(byte.class);
		Assertions.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveInteger() {
		MetaType meta = resourceProvider.getMeta(Integer.class);
		Assertions.assertEquals("base.integer", meta.getId());
		Assertions.assertEquals(Integer.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(int.class);
		Assertions.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveShort() {
		MetaType meta = resourceProvider.getMeta(Short.class);
		Assertions.assertEquals("base.short", meta.getId());
		Assertions.assertEquals(Short.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(short.class);
		Assertions.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveLong() {
		MetaType meta = resourceProvider.getMeta(Long.class);
		Assertions.assertEquals("base.long", meta.getId());
		Assertions.assertEquals(Long.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(long.class);
		Assertions.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveBoolean() {
		MetaType meta = resourceProvider.getMeta(Boolean.class);
		Assertions.assertEquals("base.boolean", meta.getId());
		Assertions.assertEquals(Boolean.class, meta.getImplementationClass());

		MetaType primitiveMeta = resourceProvider.getMeta(boolean.class);
		Assertions.assertSame(meta, primitiveMeta);
	}

	@Test
	public void testPrimitiveString() {
		MetaType meta = resourceProvider.getMeta(String.class);
		Assertions.assertEquals("base.string", meta.getId());
		Assertions.assertEquals(String.class, meta.getImplementationClass());
	}

	@Test
	public void testStringArray() {
		MetaType meta = resourceProvider.getMeta(String[].class);
		Assertions.assertEquals("base.string$array", meta.getId());
		Assertions.assertEquals(String[].class, meta.getImplementationClass());
	}

	@Test
	public void testPrimitiveDate() {
		MetaType meta = resourceProvider.getMeta(Date.class);
		Assertions.assertEquals("base.date", meta.getId());
		Assertions.assertEquals(Date.class, meta.getImplementationClass());
	}

	@Test
	@Disabled // add with Java 8
	public void testOffsetDateTime() throws ClassNotFoundException {
		if (ClassUtils.existsClass("java.time.OffsetDateTime")) {
			Class<?> offsetDateTimeClass = Class.forName("java.time.OffsetDateTime");
			MetaType meta = resourceProvider.getMeta(offsetDateTimeClass);
			Assertions.assertEquals("base.offsetDateTime", meta.getId());
			Assertions.assertEquals(offsetDateTimeClass, meta.getImplementationClass());
		}
	}

	@Test
	public void testPrimitiveUUID() {
		MetaType meta = resourceProvider.getMeta(UUID.class);
		Assertions.assertEquals("base.uuid", meta.getId());
		Assertions.assertEquals(UUID.class, meta.getImplementationClass());
	}

	@Test
	public void testPrimitiveObject() {
		MetaType meta = resourceProvider.getMeta(Object.class);
		Assertions.assertEquals("base.object", meta.getId());
		Assertions.assertEquals(Object.class, meta.getImplementationClass());
	}
}
