package server;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import templates.Template;

public interface MockServerService {

	String getMockResponse(Long id,HttpServletRequest request,boolean shouldPersist, Template template) throws IOException;
	
	void hitCallbacks() throws IOException, InterruptedException, ExecutionException;
}
