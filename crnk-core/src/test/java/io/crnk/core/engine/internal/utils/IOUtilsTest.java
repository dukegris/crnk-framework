package io.crnk.core.engine.internal.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;


public class IOUtilsTest {

	@Test
	public void testPrivateConstructor() {
		CoreClassTestUtils.assertPrivateConstructor(IOUtils.class);
	}

	@Test
	public void readFully() throws IOException {
		Random r = new Random();
		for (int i = 0; i < 100; i++) {
			byte[] b = new byte[i * 100];
			r.nextBytes(b);
			Assertions.assertTrue(Arrays.equals(b, IOUtils.readFully(new ByteArrayInputStream(b))));
		}
	}
}
