package io.crnk.home;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.SimpleModule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

public class HomeModuleExtensionTest {

	@Test
	public void test() {
		HomeModuleExtension extension = new HomeModuleExtension();
		extension.addPath("/test/directory/");
		extension.addPath("/test/something");
		SimpleModule extensionModule = new SimpleModule("extension");
		extensionModule.addExtension(extension);

		HomeModule module = Mockito.spy(HomeModule.create(HomeFormat.JSON_HOME));
		CrnkBoot boot = new CrnkBoot();
		boot.addModule(module);
		boot.addModule(extensionModule);
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.boot();

		List<String> list = module.list("/", new QueryContext());
		Assertions.assertTrue(list.contains("test/"), list.toString());

		list = module.list("/test/", new QueryContext());
		Assertions.assertTrue(list.contains("directory/"), list.toString());
		Assertions.assertTrue(list.contains("something"), list.toString());
	}
}
