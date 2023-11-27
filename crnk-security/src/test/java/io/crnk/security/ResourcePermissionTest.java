package io.crnk.security;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ResourcePermissionTest {

	@Test
	public void testEquals() {
		EqualsVerifier.forClass(ResourceIdentifier.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();

		Assertions.assertEquals(ResourcePermission.ALL, ResourcePermission.ALL);
		Assertions.assertEquals(ResourcePermission.DELETE, ResourcePermission.DELETE);
		Assertions.assertEquals(ResourcePermission.GET, ResourcePermission.GET);
		Assertions.assertEquals(ResourcePermission.POST, ResourcePermission.POST);
		Assertions.assertNotEquals(ResourcePermission.DELETE, ResourcePermission.ALL);
		Assertions.assertNotEquals(ResourcePermission.DELETE, ResourcePermission.GET);
		Assertions.assertNotEquals(ResourcePermission.DELETE, ResourcePermission.PATCH);
		Assertions.assertNotEquals(ResourcePermission.DELETE, ResourcePermission.POST);
		Assertions.assertNotEquals(ResourcePermission.DELETE, "not a resource permission");
	}

	@Test
	public void testHashCode() {
		Assertions.assertEquals(ResourcePermission.ALL.hashCode(), ResourcePermission.ALL.hashCode());
		Assertions.assertNotEquals(ResourcePermission.DELETE.hashCode(), ResourcePermission.ALL.hashCode());
		Assertions.assertNotEquals(ResourcePermission.GET.hashCode(), ResourcePermission.ALL.hashCode());
		Assertions.assertNotEquals(ResourcePermission.POST.hashCode(), ResourcePermission.PATCH.hashCode());
		Assertions.assertNotEquals(ResourcePermission.POST.hashCode(), ResourcePermission.GET.hashCode());
	}


	@Test
	public void xor() {
		Assertions.assertTrue(ResourcePermission.DELETE.xor(ResourcePermission.DELETE).isEmpty());
		Assertions.assertTrue(ResourcePermission.GET.xor(ResourcePermission.GET).isEmpty());
		Assertions.assertEquals(ResourcePermission.create(false, true, false, true),
				ResourcePermission.GET.xor(ResourcePermission.DELETE));
	}

	@Test
	public void and() {
		Assertions.assertEquals(ResourcePermission.DELETE, ResourcePermission.DELETE.or(ResourcePermission.DELETE));
		Assertions.assertEquals(ResourcePermission.GET, ResourcePermission.GET.or(ResourcePermission.GET));
		Assertions.assertTrue(ResourcePermission.GET.and(ResourcePermission.DELETE).isEmpty());
	}

	@Test
	public void or() {
		Assertions.assertEquals(ResourcePermission.DELETE, ResourcePermission.DELETE.or(ResourcePermission.DELETE));
		Assertions.assertEquals(ResourcePermission.GET, ResourcePermission.GET.or(ResourcePermission.GET));
		Assertions.assertEquals(ResourcePermission.create(false, true, false, true),
				ResourcePermission.GET.or(ResourcePermission.DELETE));
	}

	@Test
	public void fromMethod() {
		Assertions.assertEquals(ResourcePermission.GET, ResourcePermission.fromMethod(HttpMethod.GET));
		Assertions.assertEquals(ResourcePermission.POST, ResourcePermission.fromMethod(HttpMethod.POST));
		Assertions.assertEquals(ResourcePermission.DELETE, ResourcePermission.fromMethod(HttpMethod.DELETE));
		Assertions.assertEquals(ResourcePermission.PATCH, ResourcePermission.fromMethod(HttpMethod.PATCH));
	}

	@Test
	public void string() {
		Assertions.assertEquals("ResourcePermission[post=false, get=true, patch=false, delete=false]",
				ResourcePermission.GET.toString());
	}
}
