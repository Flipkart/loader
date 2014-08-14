package configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.dropwizard.config.Configuration;

@Singleton
@Getter
@Setter
public class MockServerConfiguration extends Configuration {
	
	@Valid
	@NotNull
	@JsonProperty
	private Map<String,String> env = new ConcurrentHashMap<String, String>();
	
	@Valid
	@NotNull
	@JsonProperty
	private String templateFileBasePath;
	
	@Inject
	public MockServerConfiguration(String templateFileBasePath)  {
		this.templateFileBasePath = templateFileBasePath;
	}
	
}
