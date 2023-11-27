package io.crnk.ui;


import java.util.Arrays;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.TestModule;
import io.crnk.ui.presentation.element.DataTableElement;
import io.crnk.ui.presentation.element.EditorElement;
import io.crnk.ui.presentation.element.ExplorerElement;
import io.crnk.ui.presentation.element.FormContainerElement;
import io.crnk.ui.presentation.element.FormElement;
import io.crnk.ui.presentation.element.FormElements;
import io.crnk.ui.presentation.element.PlainTextElement;
import io.crnk.ui.presentation.element.QueryElement;
import io.crnk.ui.presentation.element.TableColumnElement;
import io.crnk.ui.presentation.element.TableColumnsElement;
import io.crnk.ui.presentation.repository.EditorRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class EditorRepositoryTest {


	private UIModule uiModule;

	private QueryContext queryContext;

	@BeforeEach
	public void setup() {
		uiModule = UIModule.create(new UIModuleConfig());

		SimpleModule module = new SimpleModule("presentationTest");
		module.addRepository(new InMemoryResourceRepository<>(PresentationTask.class));
		module.addRepository(new InMemoryResourceRepository<>(PresentationProject.class));

		CrnkBoot boot = new CrnkBoot();
		boot.addModule(module);
		boot.addModule(uiModule);
		boot.addModule(new TestModule());
		boot.boot();

		HttpRequestContextProvider httpRequestContextProvider = boot.getModuleRegistry().getHttpRequestContextProvider();
		httpRequestContextProvider.onRequestStarted(new HttpRequestContextBaseAdapter(Mockito.mock(HttpRequestContextBase.class)));
		queryContext = httpRequestContextProvider.getRequestContext().getQueryContext();
		queryContext.setRequestVersion(2);
	}

	@Test
	public void checkFindAll() {
		EditorRepository repository = uiModule.getEditorRepository();
		ResourceList<EditorElement> editors = repository.findAll(new QuerySpec(EditorElement.class));
		Assertions.assertNotNull(editors);
		Assertions.assertTrue(editors.size() > 0);
	}


	@Test
	public void checkFindOne() {
		EditorRepository repository = uiModule.getEditorRepository();
		EditorElement editor = repository.findOne("local-tasks", new QuerySpec(EditorElement.class));
		Assertions.assertNotNull(editor);

		Assertions.assertEquals("tasks", editor.getBaseQuery().getResourceType());

		FormContainerElement form = editor.getForm();

		FormElements elements = form.getElements();
		Assertions.assertNotEquals(0, elements.getElementIds().size());

		FormElement idFormElement = elements.getElements().get("id");
		Assertions.assertEquals("id", idFormElement.getId());
		Assertions.assertEquals("id", idFormElement.getLabel());
		Assertions.assertFalse(idFormElement.isEditable());
		Assertions.assertEquals(PathSpec.of("id"), idFormElement.getAttributePath());
		PlainTextElement idComponent = (PlainTextElement) idFormElement.getComponent();
		Assertions.assertEquals("number", idComponent.getComponentId());
	}


	@Test
	public void checkNestedExplorerForManyRelationship() {
		EditorRepository repository = uiModule.getEditorRepository();
		EditorElement editor = repository.findOne("local-presentationProject", new QuerySpec(EditorElement.class));

		FormElement formElement = editor.getForm().getElements().getElements().get("tasks");
		ExplorerElement explorer = (ExplorerElement) formElement.getComponent();

		QueryElement baseQuery = explorer.getBaseQuery();
		Assertions.assertEquals("presentationTask", baseQuery.getResourceType());

		Assertions.assertEquals(Arrays.asList(PathSpec.of("name")), explorer.getFullTextSearchPaths());
		Assertions.assertNull(explorer.getServicePath()); // local mode => not available
		Assertions.assertEquals("presentationProject/tasks", explorer.getPath());

		Assertions.assertEquals("presentationTask", explorer.getBaseQuery().getResourceType());

		DataTableElement table = explorer.getTable();
		TableColumnsElement columns = table.getColumns();
		Assertions.assertNotEquals(0, columns.getElementIds().size());

		TableColumnElement idColumn = columns.getElements().get("id");
		Assertions.assertEquals("id", idColumn.getId());
		Assertions.assertEquals("id", idColumn.getLabel());
		Assertions.assertFalse(idColumn.isEditable());
		Assertions.assertTrue(idColumn.isSortable());
		Assertions.assertEquals(PathSpec.of("id"), idColumn.getAttributePath());
		Assertions.assertEquals("number", idColumn.getComponent().getComponentId());

		// should not be recursive
		Assertions.assertFalse(table.getColumns().getElements().containsKey("project"));
		Assertions.assertFalse(table.getColumns().getElements().containsKey("description"));

	}


	@Test
	public void checkResourceVersionOutOfRange() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
		queryContext.setRequestVersion(0);

		EditorRepository repository = uiModule.getEditorRepository();
		repository.findOne("local-presentationProject", new QuerySpec(EditorElement.class));
		});
	}

	@Test
	public void checkResourceVersionInRange() {
		queryContext.setRequestVersion(1);

		EditorRepository repository = uiModule.getEditorRepository();
		EditorElement editor = repository.findOne("local-presentationProject", new QuerySpec(EditorElement.class));
		Assertions.assertNotNull(editor);
	}

	@Test
	public void checkFieldVersionOutOfRange() {
		queryContext.setRequestVersion(1);
		EditorRepository repository = uiModule.getEditorRepository();
		EditorElement editor = repository.findOne("local-presentationProject", new QuerySpec(EditorElement.class));

		FormElements elements = editor.getForm().getElements();
		Assertions.assertEquals(Arrays.asList("id", "name", "tasks"), elements.getElementIds());
	}

	@Test
	public void checkFieldVersionInRange() {
		queryContext.setRequestVersion(2);
		EditorRepository repository = uiModule.getEditorRepository();
		EditorElement editor = repository.findOne("local-presentationProject", new QuerySpec(EditorElement.class));

		FormElements elements = editor.getForm().getElements();
		Assertions.assertEquals(Arrays.asList("id", "name", "description", "tasks"), elements.getElementIds());
	}
}
