package io.crnk.security;

import java.util.List;

import io.crnk.security.SecurityConfig.Builder;
import io.crnk.security.model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SecurityConfigTest {

	@Test
	public void test() {
		// tag::docs[]
		Builder builder = SecurityConfig.builder();
		builder.permitAll(ResourcePermission.GET);
		builder.permitAll(Task.class, ResourcePermission.DELETE);
		builder.permitAll("projects", ResourcePermission.PATCH);
		builder.permitRole("someRole", ResourcePermission.GET);
		builder.permitRole("someRole", Task.class, ResourcePermission.DELETE);
		builder.permitRole("someRole", "projects", ResourcePermission.PATCH);
		SecurityConfig config = builder.build();
		// end::docs[]

		List<SecurityRule> rules = config.getRules();
		Assertions.assertEquals(6, rules.size());
		Assertions.assertEquals(ResourcePermission.GET, rules.get(0).getPermission());
		Assertions.assertEquals(ResourcePermission.DELETE, rules.get(1).getPermission());
		Assertions.assertEquals(ResourcePermission.PATCH, rules.get(2).getPermission());
		Assertions.assertEquals(ResourcePermission.GET, rules.get(3).getPermission());
		Assertions.assertEquals(ResourcePermission.DELETE, rules.get(4).getPermission());
		Assertions.assertEquals(ResourcePermission.PATCH, rules.get(5).getPermission());

		Assertions.assertEquals("ANY", rules.get(0).getRole());
		Assertions.assertEquals("ANY", rules.get(1).getRole());
		Assertions.assertEquals("ANY", rules.get(2).getRole());
		Assertions.assertEquals("someRole", rules.get(3).getRole());
		Assertions.assertEquals("someRole", rules.get(4).getRole());
		Assertions.assertEquals("someRole", rules.get(5).getRole());

		Assertions.assertNull(rules.get(0).getResourceClass());
		Assertions.assertEquals(Task.class, rules.get(1).getResourceClass());
		Assertions.assertEquals("projects", rules.get(2).getResourceType());
		Assertions.assertNull(rules.get(3).getResourceClass());
		Assertions.assertEquals(Task.class, rules.get(4).getResourceClass());
		Assertions.assertEquals("projects", rules.get(5).getResourceType());
	}

	@Test
	public void testChained() {
		// tag::docs[]
		SecurityConfig config = SecurityConfig.builder()
				.permitAll(ResourcePermission.GET)
				.permitAll(Task.class, ResourcePermission.DELETE)
				.permitAll("projects", ResourcePermission.PATCH)
				.permitRole("someRole", ResourcePermission.GET)
				.permitRole("someRole", Task.class, ResourcePermission.DELETE)
				.permitRole("someRole", "projects", ResourcePermission.PATCH)
				.build();
		// end::docs[]

		List<SecurityRule> rules = config.getRules();
		Assertions.assertEquals(6, rules.size());
		Assertions.assertEquals(ResourcePermission.GET, rules.get(0).getPermission());
		Assertions.assertEquals(ResourcePermission.DELETE, rules.get(1).getPermission());
		Assertions.assertEquals(ResourcePermission.PATCH, rules.get(2).getPermission());
		Assertions.assertEquals(ResourcePermission.GET, rules.get(3).getPermission());
		Assertions.assertEquals(ResourcePermission.DELETE, rules.get(4).getPermission());
		Assertions.assertEquals(ResourcePermission.PATCH, rules.get(5).getPermission());

		Assertions.assertEquals("ANY", rules.get(0).getRole());
		Assertions.assertEquals("ANY", rules.get(1).getRole());
		Assertions.assertEquals("ANY", rules.get(2).getRole());
		Assertions.assertEquals("someRole", rules.get(3).getRole());
		Assertions.assertEquals("someRole", rules.get(4).getRole());
		Assertions.assertEquals("someRole", rules.get(5).getRole());

		Assertions.assertNull(rules.get(0).getResourceClass());
		Assertions.assertEquals(Task.class, rules.get(1).getResourceClass());
		Assertions.assertEquals("projects", rules.get(2).getResourceType());
		Assertions.assertNull(rules.get(3).getResourceClass());
		Assertions.assertEquals(Task.class, rules.get(4).getResourceClass());
		Assertions.assertEquals("projects", rules.get(5).getResourceType());
	}

	@Test
	public void testMultiple() {
		SecurityConfig config = SecurityConfig.builder()
				.permitAll(ResourcePermission.GET, ResourcePermission.PATCH)
				.permitAll(Task.class, ResourcePermission.DELETE, ResourcePermission.GET)
				.permitAll("projects", ResourcePermission.PATCH, ResourcePermission.GET)
				.permitRole("someRole", ResourcePermission.GET, ResourcePermission.PATCH)
				.permitRole("someRole", Task.class, ResourcePermission.DELETE, ResourcePermission.PATCH)
				.permitRole("someRole", "projects", ResourcePermission.POST, ResourcePermission.PATCH)
				.build();

		List<SecurityRule> rules = config.getRules();
		Assertions.assertEquals(12, rules.size());
		Assertions.assertEquals(ResourcePermission.GET, rules.get(0).getPermission());
		Assertions.assertEquals(ResourcePermission.PATCH, rules.get(1).getPermission());
		Assertions.assertEquals(ResourcePermission.DELETE, rules.get(2).getPermission());
		Assertions.assertEquals(ResourcePermission.GET, rules.get(3).getPermission());
		Assertions.assertEquals(ResourcePermission.PATCH, rules.get(4).getPermission());
		Assertions.assertEquals(ResourcePermission.GET, rules.get(5).getPermission());
		Assertions.assertEquals(ResourcePermission.GET, rules.get(6).getPermission());
		Assertions.assertEquals(ResourcePermission.PATCH, rules.get(7).getPermission());
		Assertions.assertEquals(ResourcePermission.DELETE, rules.get(8).getPermission());
		Assertions.assertEquals(ResourcePermission.PATCH, rules.get(9).getPermission());
		Assertions.assertEquals(ResourcePermission.POST, rules.get(10).getPermission());
		Assertions.assertEquals(ResourcePermission.PATCH, rules.get(11).getPermission());
	}
}
