package io.crnk.servlet.reactive.suite;

import io.crnk.servlet.resource.ReactiveServletTestApplication;
import io.crnk.servlet.resource.ReactiveServletTestContainer;
import io.crnk.test.suite.InheritanceAccessTestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ReactiveServletTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled // properly setup task subtype repository
public class InheritanceReactiveTest extends InheritanceAccessTestBase {

	@Autowired
	public void setTestContainer(ReactiveServletTestContainer testContainer) {
		this.testContainer = testContainer;
	}
}
