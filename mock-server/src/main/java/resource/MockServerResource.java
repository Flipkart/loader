package resource;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import configuration.MockServerConfiguration;

@Path("/mock")
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class MockServerResource {
		
	private MockServerService mockServerService;
	
	private MockServerConfiguration config;
	private AtomicLong id;
	
	@Inject
	public MockServerResource(MockServerService mockServerService, MockServerConfiguration config) {
		this.mockServerService = mockServerService;
		this.config = config;
	}
	
	@GET
	@Path("/proxy/{extension : .*}")
	public Response mockResponse(@QueryParam("persist") @DefaultValue("true") boolean shouldPersist, 
			@PathParam(value = "extension") String extension, 
			@QueryParam("callbackUrl") String callbackUrl, 
			@Context HttpServletRequest request) throws IOException 
	{
		try {
			Template template = Template.getTemplateForUrl(extension);
			String mockResponse = mockServerService.getMockResponse(id.get(), request, shouldPersist, template);
			if(!template.isAsync())
				return Response.ok(mockResponse).build();
			else
				return Response.ok().build();
		}
		finally {
			id.addAndGet(1);
		}
	}
	
	@POST
	@Path("/register/{templateName}")
	@Consumes(MediaType.TEXT_HTML)
	@Produces(MediaType.APPLICATION_JSON)
	public Response registerAsyncTemplate(@PathParam(value = "templateName")String templateName, 
			@QueryParam("urlEndpoint") String urlEndpoint, 
			@QueryParam("urlRegexPattern") String urlRegexPattern,
			@QueryParam("params") String params,
			@QueryParam("async") @DefaultValue("true") boolean async,
			@Context HttpServletRequest request, 
			String requestBody) throws IOException {
		
		Map<String,String> headers = new HashMap<String,String>();
		Enumeration<String> headerNames = request.getHeaderNames();
		while(headerNames.hasMoreElements()) {
			String headerName = headerNames.nextElement();
			headers.put(headerName,request.getHeader(headerName));
		}
		Template.createTemplate(requestBody, templateName, config.getTemplateFileBasePath(), async, 
				urlRegexPattern, urlEndpoint, request.getMethod(), params, headers);
		return Response.ok().build();
	}
	
	@GET
	@Path("/template/{templateName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_HTML)
	public Response getTemplate(@PathParam(value = "templateName") String templateName, @Context Request request) {
		Response response = Response.ok(Template.getTemplate(templateName)).build();
		return response;
	}
}
