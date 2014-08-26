package configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.yammer.dropwizard.config.Configuration;

@Singleton
@Getter
@Setter
@NoArgsConstructor
public class MockServerConfiguration extends Configuration {
	
	@Valid
	@NotNull
	@JsonProperty
	private String templateFileBasePath;
	
	@Inject
	public MockServerConfiguration(String templateFileBasePath)  {
		this.templateFileBasePath = templateFileBasePath;
	}
	
}
