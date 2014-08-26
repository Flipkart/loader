package configuration;

import resource.MockServerResource;
import server.MockServerService;
import server.impl.MockServerServiceImpl;
import templates.Template;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class MockServer extends Service<MockServerConfiguration> {

	public static MockServerConfiguration configuration;
	public static Injector injector;
	
	public static void main(String[] args) throws Exception {
		new MockServer().run(args);
	}

	@Override
	public void initialize(Bootstrap<MockServerConfiguration> arg0) {
		
	}

	@Override
	public void run(MockServerConfiguration configuration, Environment env)
			throws Exception {
		Template.loadAllTemplates("/Users/tushar.mahapatra/templates");
		injector = Guice.createInjector(new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(MockServerService.class).to(MockServerServiceImpl.class);
			}
		});
		
		env.addResource(injector.getInstance(MockServerResource.class));
	}
}
