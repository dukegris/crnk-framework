package io.crnk.client.suite;

import io.crnk.client.internal.ResourceRepositoryStubImpl;
import io.crnk.test.mock.models.nested.PostComment;
import io.crnk.test.mock.models.nested.PostCommentId;
import io.crnk.test.mock.models.nested.PostHeader;
import io.crnk.test.suite.NestedRepositoryAccessTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NestedRepositoryClientTest extends NestedRepositoryAccessTestBase {

    public NestedRepositoryClientTest() {
        ClientTestContainer testContainer = new ClientTestContainer();
        this.testContainer = testContainer;
    }


    @Test
    public void checkOneNestedUrls() {
        ResourceRepositoryStubImpl stub = (ResourceRepositoryStubImpl) oneNestedRepo;
        PostHeader resource = new PostHeader();
        resource.setPostId("a");
        resource.setValue("nested");
        String postUrl = stub.computeUrl(resource, true);
        String patchUrl = stub.computeUrl(resource, false);
        Assertions.assertTrue(postUrl.endsWith("/post/a/postHeader"));
        Assertions.assertTrue(patchUrl.endsWith("/post/a/postHeader"));
    }

    @Test
    public void checkManyNestedUrls() {
        ResourceRepositoryStubImpl stub = (ResourceRepositoryStubImpl) manyNestedRepo;
        PostCommentId id = new PostCommentId("a", "b");
        PostComment resource = new PostComment();
        resource.setId(id);
        resource.setValue("nested");
        String postUrl = stub.computeUrl(resource, true);
        String patchUrl = stub.computeUrl(resource, false);
        Assertions.assertTrue(postUrl.endsWith("/post/a/comments"));
        Assertions.assertTrue(patchUrl.endsWith("/post/a/comments/b"));
    }
}