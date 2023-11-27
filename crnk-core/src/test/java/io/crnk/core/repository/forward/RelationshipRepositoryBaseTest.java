package io.crnk.core.repository.forward;

import io.crnk.core.engine.internal.utils.CoreClassTestUtils;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.repository.RelationshipRepositoryBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Deprecated
public class RelationshipRepositoryBaseTest {

	@Test
	public void hasDefaultConstructor() {
		CoreClassTestUtils.assertProtectedConstructor(RelationshipRepositoryBase.class);
	}

	@Test
	public void checkAccessors() {
		RelationshipRepositoryBase base = new RelationshipRepositoryBase(Task.class, Project.class);
		Assertions.assertEquals(Task.class, base.getSourceResourceClass());
		Assertions.assertEquals(Project.class, base.getTargetResourceClass());
	}

	@Test
	public void checkConstructors() {
		new RelationshipRepositoryBase("a", "b");
		new RelationshipRepositoryBase("a");
	}
}
