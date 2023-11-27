package io.crnk.gen.gradle;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;

import io.crnk.core.engine.document.Resource;
import io.crnk.gen.gradle.internal.RuntimeClassLoaderFactory;
import io.crnk.gen.typescript.TSGeneratorModule;
import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSImport;
import io.crnk.gen.typescript.model.TSMember;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class RuntimeClassoaderFactoryTest {

	@TempDir
    Path testProjectDir;

	private RuntimeClassLoaderFactory.SharedClassLoader sharedClassLoader;

	private URLClassLoader classLoader;

	private RuntimeClassLoaderFactory factory;


	@BeforeEach
	public void setup() throws IOException {
		// TODO address dependency resolution issues
		GeneratorPlugin.APPLY_DOCLET_BY_DEFAULT = false;

		testProjectDir.resolve("src").resolve("main").resolve("java");

		File outputDir = testProjectDir.getRoot().toFile();

		Project project = ProjectBuilder.builder().withName("crnk-gen-typescript-test").withProjectDir(outputDir).build();
		project.setVersion("0.0.1");

		project.getPluginManager().apply(JavaPlugin.class);
		project.getPluginManager().apply(GeneratorPlugin.class);

		GeneratorExtension config = project.getExtensions().getByType(GeneratorExtension.class);
		config.getRuntime().setConfiguration("test");

		TSGeneratorModule module = new TSGeneratorModule();

		factory = new RuntimeClassLoaderFactory(project, module);
		ClassLoader parentClassLoader = getClass().getClassLoader();
		classLoader = factory.createClassLoader(parentClassLoader, true);
		sharedClassLoader = (RuntimeClassLoaderFactory.SharedClassLoader) classLoader.getParent();

	}

	@Test
	public void checkSharedClassAccessible() throws ClassNotFoundException {
		sharedClassLoader.putSharedClass("test", String.class);

		Assertions.assertSame(String.class, classLoader.loadClass("test"));
		Assertions.assertSame(String.class, sharedClassLoader.loadClass("test"));
	}

	@Test
	public void checkBootstrapClassesAccessible() throws ClassNotFoundException {
		Assertions.assertSame(String.class, classLoader.loadClass(String.class.getName()));
		Assertions.assertSame(Object.class, classLoader.loadClass(Object.class.getName()));
	}


	@Test
	public void checkTypescriptModelExposed() throws ClassNotFoundException {
		Assertions.assertSame(TSMember.class, classLoader.loadClass(TSMember.class.getName()));
		Assertions.assertSame(TSClassType.class, classLoader.loadClass(TSClassType.class.getName()));
		Assertions.assertSame(TSImport.class, classLoader.loadClass(TSImport.class.getName()));
	}

	@Test
	public void defaultLogbackTestProvidedFromParentClassloader() {
		Assertions.assertNotNull(classLoader.getResource("logback-test.xml"));
	}

	@Test
	public void defaultLogbackTestNotProvidedWithInvalidParentClassLoader() {
		Assertions.assertThrows(IllegalStateException.class, () -> {
		ClassLoader bootstrapClassLoader = ClassLoader.getSystemClassLoader().getParent();
		classLoader = factory.createClassLoader(bootstrapClassLoader, true);
			Assertions.assertNull(bootstrapClassLoader.getResource("logback-test.xml"));
			Assertions.assertNull(classLoader.getResource("logback-test.xml"));
		});
	}

	@Test
	public void classLoaderExposesTestConfiguration() {
		Assertions.assertNotEquals(0, classLoader.getURLs().length);
	}

	@Test
	public void classLoaderIsolatesCallerEnvironment() throws ClassNotFoundException {
		Assertions.assertThrows( ClassNotFoundException.class, () -> {
		// methods from context classpath should not be accessible
		classLoader.loadClass(Resource.class.getName());
		});
	}

}
