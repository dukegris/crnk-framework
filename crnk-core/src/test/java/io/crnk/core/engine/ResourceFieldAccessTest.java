package io.crnk.core.engine;

import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceFieldAccessTest {


	@Test
	public void testEqualsContract() {
		EqualsVerifier.forClass(ResourceFieldAccess.class).usingGetClass().verify();
	}

	@Test
	public void and() {
		ResourceFieldAccess all = new ResourceFieldAccess(true, true, true, true, true);
		ResourceFieldAccess none = new ResourceFieldAccess(false, false, false, false, false);
		ResourceFieldAccess a1 = new ResourceFieldAccess(false, true, false, false, false);
		ResourceFieldAccess a2 = new ResourceFieldAccess(false, false, true, false, false);
		ResourceFieldAccess a3 = new ResourceFieldAccess(false, false, false, true, false);
		ResourceFieldAccess a4 = new ResourceFieldAccess(false, false, false, false, true);
		ResourceFieldAccess a5 = new ResourceFieldAccess(true, false, false, false, false);

		Assertions.assertEquals(a1, a1.and(all));
		Assertions.assertEquals(a1, all.and(a1));
		Assertions.assertEquals(none, none.and(a1));

		Assertions.assertEquals(a2, a2.and(all));
		Assertions.assertEquals(a2, all.and(a2));
		Assertions.assertEquals(none, none.and(a2));

		Assertions.assertEquals(a3, a3.and(all));
		Assertions.assertEquals(a3, all.and(a3));
		Assertions.assertEquals(none, none.and(a3));

		Assertions.assertEquals(a4, a4.and(all));
		Assertions.assertEquals(a4, all.and(a4));
		Assertions.assertEquals(none, none.and(a4));

		Assertions.assertEquals(none, none.and(all));
		Assertions.assertEquals(none, none.and(none));
		Assertions.assertEquals(all, all.and(all));

		Assertions.assertEquals(a5, a5.and(all));
		Assertions.assertEquals(a5, all.and(a5));
		Assertions.assertEquals(none, none.and(a5));
	}
}
