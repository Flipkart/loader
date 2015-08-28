package configuration;

import resource.MockServerResource;
import server.MockServerService;
import server.impl.MockServerServiceImpl;
import templates.Template;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

@Singleton
public class MockServer extends Service<MockServerConfiguration> {

	private Injector injector;
	
	public static void main(String[] args) throws Exception {
		new MockServer().run(args);
	}

	@Override
	public void initialize(Bootstrap<MockServerConfiguration> arg0) {
		
	}

	@Override
	public void run(final MockServerConfiguration configuration, Environment env)
			throws Exception {
		Template.loadAllTemplates(configuration.getTemplateFileBasePath());
		injector = Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(MockServerService.class).to(MockServerServiceImpl.class);
				bind(MockServerConfiguration.class).toInstance(configuration);
			}
		});
		
		env.addResource(injector.getInstance(MockServerResource.class));
	}
}
