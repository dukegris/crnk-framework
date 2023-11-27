package io.crnk.core.engine.registry;


import java.util.Collection;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.VersionRange;
import io.crnk.core.module.TestResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HierarchicalResourceRegistryPartTest {

	@Test
	public void testDuplicatePartThrowsException() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			HierarchicalResourceRegistryPart part = new HierarchicalResourceRegistryPart();
			part.putPart("", new DefaultResourceRegistryPart());
			part.putPart("", new DefaultResourceRegistryPart());
		});
	}

	@Test
	public void checkHasEntryForNonExistentEntry() {
		HierarchicalResourceRegistryPart part = new HierarchicalResourceRegistryPart();
		Assertions.assertFalse(part.hasEntry("doesNotExists"));
		Assertions.assertFalse(part.hasEntry(String.class));
		Assertions.assertNull(part.getEntry("doesNotExists")); // TODO consider exception
		Assertions.assertNull(part.getEntryByPath("doesNotExists")); // TODO consider exception
		Assertions.assertNull(part.getEntry(String.class)); // TODO consider exception
	}


	@Test
	public void testRootPart() {
		HierarchicalResourceRegistryPart part = new HierarchicalResourceRegistryPart();
		part.putPart("", new DefaultResourceRegistryPart());

		ResourceInformation information = Mockito.mock(ResourceInformation.class);
		Mockito.when(information.getResourceType()).thenReturn("test");
		Mockito.when(information.getImplementationClass()).thenReturn((Class) TestResource.class);
		Mockito.when(information.getImplementationType()).thenReturn(TestResource.class);
		Mockito.when(information.getVersionRange()).thenReturn(VersionRange.UNBOUNDED);
		Mockito.when(information.getResourcePath()).thenReturn("path");
		RegistryEntry entry = Mockito.mock(RegistryEntry.class);
		Mockito.when(entry.getResourceInformation()).thenReturn(information);
		RegistryEntry savedEntry = part.addEntry(entry);
		Assertions.assertSame(savedEntry, entry);

		Collection<RegistryEntry> resources = part.getEntries();
		Assertions.assertEquals(1, resources.size());
		Assertions.assertSame(entry, part.getEntry("test"));
		Assertions.assertSame(entry, part.getEntry(TestResource.class));
		Assertions.assertTrue(part.hasEntry("test"));
		Assertions.assertTrue(part.hasEntry(TestResource.class));
	}

	@Test
	public void testChildPart() {
		ResourceRegistryPartListener listener = Mockito.mock(ResourceRegistryPartListener.class);

		HierarchicalResourceRegistryPart part = new HierarchicalResourceRegistryPart();
		part.putPart("child", new DefaultResourceRegistryPart());
		part.addListener(listener);

		ResourceInformation information = Mockito.mock(ResourceInformation.class);
		Mockito.when(information.getResourceType()).thenReturn("child/test");
		Mockito.when(information.getImplementationType()).thenReturn(TestResource.class);
		Mockito.when(information.getVersionRange()).thenReturn(VersionRange.UNBOUNDED);
		RegistryEntry entry = Mockito.mock(RegistryEntry.class);
		Mockito.when(entry.getResourceInformation()).thenReturn(information);
		RegistryEntry savedEntry = part.addEntry(entry);
		Assertions.assertSame(savedEntry, entry);
		Mockito.verify(listener, Mockito.times(1)).onChanged(Mockito.any(ResourceRegistryPartEvent.class));

		Collection<RegistryEntry> resources = part.getEntries();
		Assertions.assertEquals(1, resources.size());
		Assertions.assertSame(entry, part.getEntry("child/test"));
		Assertions.assertSame(entry, part.getEntry(TestResource.class));
		Assertions.assertTrue(part.hasEntry("child/test"));
		Assertions.assertTrue(part.hasEntry(TestResource.class));
	}

	@Test
	public void testMissingChildPart() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
			HierarchicalResourceRegistryPart part = new HierarchicalResourceRegistryPart();
			part.putPart("otherChild", new DefaultResourceRegistryPart());

			ResourceInformation information = Mockito.mock(ResourceInformation.class);
			Mockito.when(information.getResourceType()).thenReturn("child/test");
			Mockito.when(information.getResourceClass()).thenReturn((Class) TestResource.class);
			Mockito.when(information.getVersionRange()).thenReturn(VersionRange.UNBOUNDED);
			RegistryEntry entry = Mockito.mock(RegistryEntry.class);
			Mockito.when(entry.getResourceInformation()).thenReturn(information);
			part.addEntry(entry);
		});
	}
}
