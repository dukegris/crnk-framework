package io.crnk.test.suite;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.nested.PostComment;
import io.crnk.test.mock.models.nested.PostCommentId;
import io.crnk.test.mock.models.nested.NestedRelatedResource;
import io.crnk.test.mock.models.nested.PostHeader;
import io.crnk.test.mock.models.nested.Post;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class NestedRepositoryAccessTestBase {

	protected TestContainer testContainer;

	protected ResourceRepository<Post, String> parentRepo;

	protected ResourceRepository<PostComment, PostCommentId> manyNestedRepo;

	protected ResourceRepository<PostHeader, String> oneNestedRepo;

	protected ResourceRepository<NestedRelatedResource, String> relatedRepo;

	@BeforeEach
	public void setup() {
		testContainer.start();

		parentRepo = testContainer.getRepositoryForType(Post.class);
		manyNestedRepo = testContainer.getRepositoryForType(PostComment.class);
		oneNestedRepo = testContainer.getRepositoryForType(PostHeader.class);
		relatedRepo = testContainer.getRepositoryForType(NestedRelatedResource.class);

		NestedRelatedResource relatedResource = new NestedRelatedResource();
		relatedResource.setId("related");
		relatedRepo.create(relatedResource);

		Post parentResource = new Post();
		parentResource.setId("a");
		parentRepo.create(parentResource);
	}

	@AfterEach
	public void tearDown() {
		testContainer.stop();
	}


	@Test
	public void testMany() {
		PostCommentId id = new PostCommentId("a", "b");

		// perform create
		PostComment resource = new PostComment();
		resource.setId(id);
		resource.setValue("nested");
		resource = manyNestedRepo.create(resource);
		Assertions.assertEquals(id, resource.getId());
		String selfUrl = resource.getLinks().getSelf().getHref();
		Assertions.assertTrue(selfUrl.contains("a/comments/b"), selfUrl);
		Assertions.assertEquals("nested", resource.getValue());

		// perform update
		resource.setValue("updated");
		resource = manyNestedRepo.save(resource);
		Assertions.assertEquals("updated", resource.getValue());
		selfUrl = resource.getLinks().getSelf().getHref();
		Assertions.assertTrue(selfUrl.contains("a/comments/b"));

		// perform find over all nested resources
		ResourceList<PostComment> list = manyNestedRepo.findAll(new QuerySpec(PostComment.class));
		Assertions.assertEquals(1, list.size());
		resource = list.get(0);
		Assertions.assertEquals("updated", resource.getValue());

		// perform delete
		manyNestedRepo.delete(id);
		try {
			manyNestedRepo.findOne(id, new QuerySpec(PostComment.class));
			Assertions.fail("should no longer be available");
		}
		catch (ResourceNotFoundException e) {
			// ok
		}
	}

	@Test
	public void testOne() {
		// perform create
		PostHeader resource = new PostHeader();
		resource.setPostId("a");
		resource.setValue("nested");
		resource = oneNestedRepo.create(resource);
		Assertions.assertEquals("a", resource.getPostId());
		String selfUrl = resource.getLinks().getSelf().getHref();
		Assertions.assertTrue(selfUrl.contains("a/postHeader"), selfUrl);
		Assertions.assertEquals("nested", resource.getValue());

		// perform update
		resource.setValue("updated");
		resource = oneNestedRepo.save(resource);
		Assertions.assertEquals("updated", resource.getValue());
		selfUrl = resource.getLinks().getSelf().getHref();
		Assertions.assertTrue(selfUrl.contains("a/postHeader"), selfUrl);

		// perform find over all nested resources
		ResourceList<PostHeader> list = oneNestedRepo.findAll(new QuerySpec(PostHeader.class));
		Assertions.assertEquals(1, list.size());
		resource = list.get(0);
		Assertions.assertEquals("updated", resource.getValue());

		// perform delete
		oneNestedRepo.delete("a");
		try {
			oneNestedRepo.findOne("a", new QuerySpec(PostHeader.class));
			Assertions.fail("should no longer be available");
		}
		catch (ResourceNotFoundException e) {
			// ok
		}
	}
}
