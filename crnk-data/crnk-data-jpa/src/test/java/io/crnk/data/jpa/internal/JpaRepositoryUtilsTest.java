package io.crnk.data.jpa.internal;

import io.crnk.test.mock.ClassTestUtils;
import org.junit.jupiter.api.Test;

public class JpaRepositoryUtilsTest {

	@Test
	public void hasPrivateConstructor() {
		ClassTestUtils.assertPrivateConstructor(JpaRepositoryUtils.class);
	}
}
