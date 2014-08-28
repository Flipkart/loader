package resource;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import server.MockServerService;
import templates.Template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.MockServerConfiguration;

@Path("/mock")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class MockServerResource {
		
	private MockServerService mockServerService;
	
	private MockServerConfiguration config;
	private AtomicLong id = new AtomicLong(1l);
	
	@Inject
	public MockServerResource(MockServerService mockServerService, MockServerConfiguration config) {
		this.mockServerService = mockServerService;
		this.config = config;
	}
	
	@POST
	@Path("/proxy/{extension : .*}")
	public Response mockResponsePost(@QueryParam("persist") @DefaultValue("true") boolean shouldPersist, 
			@PathParam(value = "extension") String extension, 
			@QueryParam("callbackUrl") String callbackUrl, 
			@Context HttpServletRequest request, String requestBody)
	{
		return getMockResponse(shouldPersist, extension, callbackUrl, request, requestBody);
	}
	
	@PUT
	@Path("/proxy/{extension : .*}")
	public Response mockResponsePut(@QueryParam("persist") @DefaultValue("true") boolean shouldPersist, 
			@PathParam(value = "extension") String extension, 
			@QueryParam("callbackUrl") String callbackUrl, 
			@Context HttpServletRequest request, String requestBody)
	{
		return getMockResponse(shouldPersist, extension, callbackUrl, request, requestBody);
	}
	
	@GET
	@Path("/proxy/{extension : .*}")
	public Response mockResponse(@QueryParam("persist") @DefaultValue("true") boolean shouldPersist, 
			@PathParam(value = "extension") String extension, 
			@QueryParam("callbackUrl") String callbackUrl, 
			@Context HttpServletRequest request)
	{
		return getMockResponse(shouldPersist, extension, callbackUrl, request, null);
	}
	
	private Response getMockResponse(boolean shouldPersist, String extension, String callbackUrl, HttpServletRequest request, String requestBody) {
		try {
			String method = request.getMethod();
			Template template = Template.getTemplate(extension, requestBody, method);
			long fireCallbackAfter = 0;
			if(template.getFireCallbackAfter() != null)
				fireCallbackAfter = template.getFireCallbackAfter();
			
			String mockResponse = mockServerService.getMockResponse(id.get(), requestBody
					, shouldPersist, template, fireCallbackAfter);
			
			if(!template.isAsync()) {
				if(null != template.getWaitTimeInSec())
					Thread.sleep(template.getWaitTimeInSec()*1000);
				return Response.ok(mockResponse).build();
			}
			else
				return Response.ok("Async request completed.").build();
		} catch(Exception e) {
			e.printStackTrace();
			return Response.status(207).build();
		}
		finally {
			id.addAndGet(1);
		}
	}
	
	@POST
	@Path("/register/{templateName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	public Response registerTemplate(@PathParam(value = "templateName")String templateName, 
			@QueryParam("async") @DefaultValue("true") boolean async,
			@Context HttpServletRequest request, 
			String requestBody) throws IOException {
		
		Map<String,String> headers = new HashMap<String,String>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.put(headerName,request.getHeader(headerName));
		}
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode node = mapper.reader(ObjectNode.class).readValue(requestBody);
		String urlEndpoint = node.get("urlEndpoint").asText();
		String urlRegexPattern = node.get("urlRegexPattern").asText();
		String params = node.get("params").asText();
		Long waitTimeInSec = node.get("waitTimeInSec").asLong();
		Long fireCallbackAfter = null;
		if(node.get("fireCallbackAfter") != null)
			fireCallbackAfter = node.get("fireCallbackAfter").asLong();
		int priority = node.get("priority").asInt();
		String requestBodyRegexPattern = null;
		if(node.get("requestBodyRegexPattern") != null)
			requestBodyRegexPattern = node.get("requestBodyRegexPattern").asText();
		String content = "";
		if(null != node.get("content"))
			content = node.get("content").toString();
		String method = node.get("method").asText();
		
		Template.createTemplate(content, templateName, config.getTemplateFileBasePath(), async, 
				urlRegexPattern, urlEndpoint, method, params, headers, 
				waitTimeInSec, fireCallbackAfter, priority, requestBodyRegexPattern);
		
		return Response.ok("Successfully Registered Template- "+templateName).build();
	}
	
	@GET
	@Path("/template/{templateName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	public Response getTemplate(@PathParam(value = "templateName") String templateName, @Context Request request) {
		Response response = Response.ok(Template.getTemplate(templateName)).build();
		return response;
	}
	
	@POST
	@Path("/callbacks")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	public Response triggerCallbacks() throws IOException, InterruptedException, ExecutionException {
		mockServerService.hitCallbacks();
		return Response.ok("callbacks done.").build();
	}
}
