package io.crnk.gen.typescript.internal;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.gen.typescript.TSGeneratorConfig;
import io.crnk.gen.typescript.TSGeneratorModule;
import io.crnk.gen.typescript.transform.TSMetaTransformationContext;
import io.crnk.gen.typescript.transform.TSMetaTransformationOptions;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.types.ProjectData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;

public class TSGeneratorTest {

	@TempDir
    Path testProjectDir;

    private TSGenerator generator;
    private MetaModule metaModule;
    private TSGeneratorConfig config;

    @BeforeEach
    public void setup() {
        File outputDir = testProjectDir.getRoot().toFile();

        MetaModuleConfig metaConfig = new MetaModuleConfig();
        metaConfig.addMetaProvider(new ResourceMetaProvider());
        metaModule = MetaModule.createServerModule(metaConfig);

        CrnkBoot boot = new CrnkBoot();
        boot.setServiceDiscovery(new EmptyServiceDiscovery());
        boot.addModule(metaModule);
        boot.boot();

        TSGeneratorModule module = new TSGeneratorModule();
        module.initDefaults(testProjectDir.getRoot().toFile());
        config = module.getConfig();

        generator = new TSGenerator(outputDir, metaModule.getLookup(), config);
    }

    @Test
    public void checkMetaExcludedByDefault() {
        Assertions.assertTrue(config.getExcludes().contains("resources.meta"));
    }

    @Test
    public void throwExceptionWhenMetaElementNotMappedToNpmPackage() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
        TSMetaTransformationContext transformationContext = generator.createMetaTransformationContext();
        MetaElement metaElement = Mockito.mock(MetaElement.class);
        metaElement.setId("does.not.exist");
        transformationContext.getNpmPackage(metaElement);
        });
    }

    @Test
    public void throwExceptionWhenMetaElementNotMappedToDirectory() {
		Assertions.assertThrows(UnsupportedOperationException.class, () -> {
        TSMetaTransformationContext transformationContext = generator.createMetaTransformationContext();
        MetaElement metaElement = Mockito.mock(MetaElement.class);
        metaElement.setId("does.not.exist");
        transformationContext.getDirectory(metaElement);
        });
    }

    @Test
    public void throwExceptionWhenTransformingUnknownMetaElement() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
        MetaElement metaElement = Mockito.mock(MetaElement.class);
        metaElement.setId("does.not.exist");

        TSMetaTransformationOptions options = Mockito.mock(TSMetaTransformationOptions.class);
        generator.transform(metaElement, options);
        });
    }

    @Test
    public void testResourcesMappedToRootDirectory() {
        MetaResource element = new MetaResource();
        element.setImplementationType(Task.class);
        element.setId("resources.task");

        TSMetaTransformationContext context = generator.createMetaTransformationContext();
        Assertions.assertEquals("", context.getDirectory(element));
    }

    @Test
    public void testDataObjectsMappedToRootDirectoryByDefault() {
        MetaResource element = new MetaResource();
        element.setImplementationType(ProjectData.class);
        element.setId("sometehing.task");

        TSMetaTransformationContext context = generator.createMetaTransformationContext();
        Assertions.assertEquals("", context.getDirectory(element));
        Assertions.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
    }

    @Test
    public void testDirectoryMapping() {
        MetaResource element = new MetaResource();
        element.setImplementationType(ProjectData.class);
        element.setId("a.b.task");

        config.getNpm().getDirectoryMapping().put("a", "x");

        TSMetaTransformationContext context = generator.createMetaTransformationContext();
        Assertions.assertEquals("/x/b", context.getDirectory(element));
        Assertions.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
    }

    @Test
    public void testNestedDirectoryMapping() {
        MetaResource element = new MetaResource();
        element.setImplementationType(ProjectData.class);
        element.setId("a.b.task");

        config.getNpm().getDirectoryMapping().put("a", "x/y");

        TSMetaTransformationContext context = generator.createMetaTransformationContext();
        Assertions.assertEquals("/x/y/b", context.getDirectory(element));
        Assertions.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
    }

    @Test
    public void testNestedDirectoryMappingIgnoresLeadingSlash() {
        MetaResource element = new MetaResource();
        element.setImplementationType(ProjectData.class);
        element.setId("a.b.task");

        config.getNpm().getDirectoryMapping().put("a", "/x/y");

        TSMetaTransformationContext context = generator.createMetaTransformationContext();
        Assertions.assertEquals("/x/y/b", context.getDirectory(element));
        Assertions.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
    }

    @Test
    public void testNestedDirectoryMappingIgnoresTrailingSlash() {
        MetaResource element = new MetaResource();
        element.setImplementationType(ProjectData.class);
        element.setId("a.b.task");

        config.getNpm().getDirectoryMapping().put("a", "x/y/");

        TSMetaTransformationContext context = generator.createMetaTransformationContext();
        Assertions.assertEquals("/x/y/b", context.getDirectory(element));
        Assertions.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
    }

    @Test
    public void testEmptyDirectoryMapping() {
        MetaResource element = new MetaResource();
        element.setImplementationType(ProjectData.class);
        element.setId("a.b.task");

        config.getNpm().getDirectoryMapping().put("a", "");

        TSMetaTransformationContext context = generator.createMetaTransformationContext();
        Assertions.assertEquals("/b", context.getDirectory(element));
        Assertions.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
    }


    @Test
    public void testRootDirectoryMapping() {
        MetaResource element = new MetaResource();
        element.setImplementationType(ProjectData.class);
        element.setId("a.b.task");

        config.getNpm().getDirectoryMapping().put("a.b", "");

        TSMetaTransformationContext context = generator.createMetaTransformationContext();
        Assertions.assertEquals("/", context.getDirectory(element));
        Assertions.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
    }

    @Test
    public void testRootDirectoryMappingIgnoresSlash() {
        MetaResource element = new MetaResource();
        element.setImplementationType(ProjectData.class);
        element.setId("a.b.task");

        config.getNpm().getDirectoryMapping().put("a.b", "/");

        TSMetaTransformationContext context = generator.createMetaTransformationContext();
        Assertions.assertEquals("/", context.getDirectory(element));
        Assertions.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
    }
}
