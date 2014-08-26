package redis;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Getter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
@Getter
public class JedisFunctions {

	private JedisWrapper<String, String> jedisWrapper;
	
	private static ObjectMapper mapper = new ObjectMapper();

	private Map<Long,String> requestIds = new ConcurrentHashMap<Long, String>();
	
	public String getTemplateNameForId(Long id) {
		return requestIds.get(id);
	}
	
	@Inject
	public JedisFunctions(JedisWrapper<String, String> jedisWrapper) {
		this.jedisWrapper = jedisWrapper;
	}
	
	public void register(Long id, String templateName, String requestMethod, String params, 
			Map<String,String> requestHeaders, String url, String content) throws JsonProcessingException {
		requestIds.put(id,templateName);
		registerUrl(id, templateName, url);
		registerRequest(id, templateName, content);
		registerParams(id, templateName, params);
		registerRequestMethod(id, templateName, requestMethod);
		registerRequestHeaders(id, templateName, requestHeaders);
	}
	
	public void registerRequestMethod(Long id,String templateName,String requestMethod) {
		jedisWrapper.set(".request_method."+id, templateName, requestMethod);
	}
	
	public String getRequestMethod(Long id,String templateName) {
		return jedisWrapper.get(".request_method."+id, templateName);
	}
	
	public void registerParams(Long id,String templateName,String params) {
		jedisWrapper.set(".params."+id, templateName, params);
	}
	
	public String getParams(Long id,String templateName) {
		return jedisWrapper.get(".params."+id, templateName);
	}
	
	public synchronized void registerRequestHeaders(Long id,String templateName,Map<String,String> requestHeaders) throws JsonProcessingException {
		jedisWrapper.set(".headers."+id, templateName, mapper.writeValueAsString(requestHeaders));
	}
	
	public synchronized Map<String,String> getRequestHeaders(Long id,String templateName) throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(jedisWrapper.get(".headers."+id, templateName), new TypeReference<Map<String,String>>(){});
	}
	
	public void registerUrl(Long id,String templateName,String url) {
		jedisWrapper.set(".callback_url."+id,templateName, url);
	}
	
	public String getUrl(Long id,String templateName) {
		return jedisWrapper.get(".callback_url."+id,templateName);
	}
	
	public void registerRequest(Long id, String templateName, String content) {
		jedisWrapper.set(".request."+id, templateName, content);
	}
	
	public String getRequest(Long id, String templateName) {
		return jedisWrapper.get(".request."+id, templateName);
	}
	
}
