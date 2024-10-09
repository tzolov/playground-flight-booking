package ai.spring.demo.ai.playground;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

@SpringBootApplication
@Theme(value = "customer-support-agent")
public class Application implements AppShellConfigurator {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class).run(args);
	}
}
