package io.crnk.meta.model;

import io.crnk.meta.AbstractMetaTest;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.test.mock.models.Project;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.types.ProjectData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class MetaDataObjectTest extends AbstractMetaTest {


	@Test
	public void checkResolvePathWithNullNotAllowed() {
		Assertions.assertThrows( IllegalArgumentException.class, () -> {		
			MetaResource meta = resourceProvider.getMeta(Task.class);
			meta.resolvePath(null);
		});
	}

	@Test
	public void checkResolveEmptyPath() {
		MetaResource meta = resourceProvider.getMeta(Task.class);
		Assertions.assertEquals(MetaAttributePath.EMPTY_PATH, meta.resolvePath(new ArrayList<String>()));
	}


	@Test
	public void checkResolveSubtypeAttribute() {
		MetaResource meta = resourceProvider.getMeta(Task.class);
		Assertions.assertNotNull(meta.findAttribute("subTypeValue", true));
	}

	@Test
	public void checkCannotResolveSubtypeAttributeWithoutIncludingSubtypes() {
		Assertions.assertThrows(IllegalStateException.class, () -> {		
			MetaResource meta = resourceProvider.getMeta(Task.class);
			meta.findAttribute("subTypeValue", false);
		});
	}

	@Test
	public void checkResolveInvalidAttribute() {
		Assertions.assertThrows(IllegalStateException.class, () -> {		
			MetaResource meta = resourceProvider.getMeta(Task.class);
			Assertions.assertNotNull(meta.findAttribute("doesNotExist", true));
		});
	}

	@Test
	public void checkResolveMapPath() {
		MetaResource meta = resourceProvider.getMeta(Project.class);
		MetaAttributePath path = meta.resolvePath(Arrays.asList("data", "customData", "test"));
		Assertions.assertEquals(2, path.length());
		Assertions.assertEquals("data", path.getElement(0).getName());
		MetaMapAttribute mapAttr = (MetaMapAttribute) path.getElement(1);
		Assertions.assertEquals("test", mapAttr.getKey());
		Assertions.assertEquals("customData", mapAttr.getName());
	}


	@Test
	public void checkNestedObject() {
		MetaJsonObject meta = resourceProvider.getMeta(ProjectData.class);
		Assertions.assertEquals("ProjectData", meta.getName());
		Assertions.assertEquals("resources.types.projectdata", meta.getId());
		Assertions.assertNotNull(meta.getAttribute("data").getType());
	}

	@Test
	public void checkResolveInvalidPath() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> {		
			MetaResource meta = resourceProvider.getMeta(Project.class);
			meta.resolvePath(Arrays.asList("name", "doesNotExist"));
		});
	}
}
