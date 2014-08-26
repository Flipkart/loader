package server.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.RequestBuilder;

import redis.JedisFunctions;
import server.MockServerService;
import templates.Template;

@Singleton
public class MockServerServiceImpl implements MockServerService {

	private JedisFunctions functions;

	private ObjectMapper mapper = new ObjectMapper();

	@Inject
	public MockServerServiceImpl(JedisFunctions functions) {
		this.functions = functions;
	}

	@SuppressWarnings("unchecked")
	public String getMockResponse(Long id,String requestBody,boolean shouldPersist, Template template) throws IOException {

		String response,params,url;
		if(null != requestBody) {
			Map<String,Object> requestMap = (Map<String,Object>)mapper.readValue(requestBody, Map.class);
			Map<String,Object> substituteMap = (Map<String,Object>)requestMap.get("substitutor");

			response = template.getResponseBodyFromTemplate(substituteMap);
			params = template.getParamsFromTemplate(substituteMap);
			url = template.getUrlFromTemplate(substituteMap);
		}
		else {
			response = template.getContent();
			params = template.getParams();
			url = template.getUrlEndpoint();
		}

		// convert mock request to response
		if(shouldPersist) {
			functions.register(id, template.getTemplateName(), template.getRequestMethod(), 
					params, template.getStaticHeaders(), url, response);
		}
		return response;
	}

	// To be run as a background process
	public void hitCallbacks() throws IOException, InterruptedException, ExecutionException {
		List<Long> idsToBeRemoved = new ArrayList<Long>();
		for(Long id:functions.getRequestIds().keySet()) {

			try {
				String templateName = functions.getTemplateNameForId(id);
				String requestBody = functions.getRequest(id, templateName);
				String url = functions.getUrl(id, templateName);
				String requestMethod = functions.getRequestMethod(id, templateName);
				Map<String, String> requestHeaders = functions.getRequestHeaders(id, templateName);
				String params = functions.getParams(id, templateName);
				url = !url.contains("?") && !StringUtils.isEmpty(params)?url+"?"+params:url+params;

				RequestBuilder builder = new RequestBuilder();
				builder.setBody(requestBody);
				for(Entry<String,String> e: requestHeaders.entrySet()) {
					builder.addHeader(e.getKey(), e.getValue());
				}

				@SuppressWarnings("resource")
				AsyncHttpClient client = new AsyncHttpClient();

				if(requestMethod == null || "POST".equalsIgnoreCase(requestMethod)) 
					client.preparePost(url); 
				else if("GET".equalsIgnoreCase(requestMethod)) 
					client.prepareGet(url);
				else if("PUT".equalsIgnoreCase(requestMethod)) 
					client.preparePut(url);
				else if("DELETE".equalsIgnoreCase(requestMethod)) 
					client.prepareDelete(url);
				else continue;

				client.executeRequest(builder.build()).get();
			} catch(Exception e) {
				e.printStackTrace();
			}
			finally {
				idsToBeRemoved.add(id);
			}
		}

		for(Long id:idsToBeRemoved) {
			functions.getRequestIds().remove(id);
		}
	}

}
