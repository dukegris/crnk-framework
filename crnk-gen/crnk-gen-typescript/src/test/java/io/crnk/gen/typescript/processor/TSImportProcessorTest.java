package io.crnk.gen.typescript.processor;

import io.crnk.gen.typescript.model.TSArrayType;
import io.crnk.gen.typescript.model.TSClassType;
import io.crnk.gen.typescript.model.TSField;
import io.crnk.gen.typescript.model.TSImport;
import io.crnk.gen.typescript.model.TSInterfaceType;
import io.crnk.gen.typescript.model.TSModule;
import io.crnk.gen.typescript.model.TSParameterizedType;
import io.crnk.gen.typescript.model.TSSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TSImportProcessorTest {

	private List<TSSource> sources;

	private TSSource interfaceSource;

	private TSClassType classType;

	private TSSource classSource;

	private TSInterfaceType interfaceType;

	private TSImportProcessor processor;

	@BeforeEach
	public void setup() {
		processor = new TSImportProcessor();

		interfaceType = new TSInterfaceType();
		interfaceType.setName("SomeInterface");
		interfaceSource = new TSSource();
		interfaceSource.addElement(interfaceType);
		interfaceSource.setNpmPackage("@crnk/test");
		interfaceSource.setDirectory("someDir");
		interfaceSource.setName("some-interface");

		classType = new TSClassType();
		classType.setName("SomeClass");
		classType.addImplementedInterface(interfaceType);
		classSource = new TSSource();
		classSource.setNpmPackage("@crnk/test");
		classSource.setDirectory("someDir");
		classSource.setName("some-class");
		classSource.addElement(classType);

		sources = new ArrayList<>();
		sources.add(classSource);
		sources.add(interfaceSource);
	}

	@Test
	public void sameDirectoryImport() {
		List<TSSource> updatedSources = processor.process(sources);
		Assertions.assertEquals(sources.size(), updatedSources.size());

		Assertions.assertEquals(1, classSource.getImports().size());
		TSImport tsImport = classSource.getImports().get(0);
		Assertions.assertEquals("./some-interface", tsImport.getPath());
	}

	@Test
	public void childDirectoryImport() {
		interfaceSource.setDirectory("someDir/child-dir");

		processor.process(sources);
		TSImport tsImport = classSource.getImports().get(0);
		Assertions.assertEquals("./child-dir/some-interface", tsImport.getPath());
	}

	@Test
	public void parentDirectoryImport() {
		interfaceSource.setDirectory(null);

		processor.process(sources);
		TSImport tsImport = classSource.getImports().get(0);
		Assertions.assertEquals("../some-interface", tsImport.getPath());
	}

	@Test
	public void siblingDirectoryImport() {
		interfaceSource.setDirectory("other-dir");

		processor.process(sources);
		TSImport tsImport = classSource.getImports().get(0);
		Assertions.assertEquals("../other-dir/some-interface", tsImport.getPath());
	}


	@Test
	public void checkArrayElementTypeImported() {
		TSField field = new TSField();
		field.setName("someField");
		field.setType(new TSArrayType(interfaceType));
		classType.addDeclaredMember(field);

		processor.process(sources);
		TSImport tsImport = classSource.getImports().get(0);
		Assertions.assertEquals("./some-interface", tsImport.getPath());
	}

	@Test
	public void checkParameterizedTypeImported() {
		TSInterfaceType parameterType = new TSInterfaceType();
		parameterType.setName("ParamInterface");
		TSSource paramSource = new TSSource();
		paramSource.addElement(parameterType);
		paramSource.setNpmPackage("@crnk/test");
		paramSource.setDirectory("someDir");
		paramSource.setName("some-param");
		sources.add(paramSource);

		TSField field = new TSField();
		field.setName("someField");
		field.setType(new TSParameterizedType(interfaceType, parameterType));
		classType.addDeclaredMember(field);

		processor.process(sources);
		Assertions.assertEquals(2, classSource.getImports().size());
		Assertions.assertEquals("./some-interface", classSource.getImports().get(0).getPath());
		Assertions.assertEquals("./some-param", classSource.getImports().get(1).getPath());
	}

	@Test
	public void checkModuleImport() {
		TSInterfaceType moduleInterface = new TSInterfaceType();
		moduleInterface.setName("SomeInterface");

		TSModule module = new TSModule();
		module.setName("SomeModule");
		module.addElement(moduleInterface);

		TSSource moduleSource = new TSSource();
		moduleSource.addElement(module);
		moduleSource.setNpmPackage("@crnk/test");
		moduleSource.setDirectory("someDir");
		moduleSource.setName("some-module");
		sources.add(moduleSource);

		TSField field = new TSField();
		field.setName("someField");
		field.setType(moduleInterface);
		classType.setImplementedInterfaces(new ArrayList<>());
		classType.addDeclaredMember(field);

		processor.process(sources);
		Assertions.assertEquals(1, classSource.getImports().size());
		TSImport intImport = classSource.getImports().get(0);
		Assertions.assertEquals("./some-module", intImport.getPath());
		Assertions.assertEquals(1, intImport.getTypeNames().size());
		Assertions.assertEquals("SomeModule", intImport.getTypeNames().iterator().next());
	}
}
