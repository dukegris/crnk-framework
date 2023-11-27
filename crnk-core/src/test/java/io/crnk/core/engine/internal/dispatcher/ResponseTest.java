package io.crnk.core.engine.internal.dispatcher;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.utils.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResponseTest {

	@Test
	public void testHashCodeEquals() {
		Document r1 = new Document();
		Document r2 = new Document();
		r2.setData(Nullable.of(new Resource()));
		Response c1 = new Response(r1, 201);
		Response c1copy = new Response(r1, 201);
		Response c2 = new Response(r2, 202);
		Response c3 = new Response(r1, 202);

		Assertions.assertEquals(c1.hashCode(), c1copy.hashCode());
		Assertions.assertTrue(c1.equals(c1));
		Assertions.assertTrue(c1.equals(c1copy));
		Assertions.assertFalse(c1.equals(c2));
		Assertions.assertFalse(c1.equals(c3));
		Assertions.assertFalse(c2.equals(c3));
		Assertions.assertFalse(c2.equals("otherType"));
	}


	@Test
	public void testGetterSetter() {
		Document document = new Document();
		Response response = new Response(document, 201);

		response.setDocument(document);
		Assertions.assertSame(document, response.getDocument());

		response.setHttpStatus(23);
		Assertions.assertSame(23, response.getHttpStatus());

	}
}
