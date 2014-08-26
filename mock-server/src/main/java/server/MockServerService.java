package server;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import templates.Template;

public interface MockServerService {

	String getMockResponse(Long id,String requestBody,boolean shouldPersist, Template template) throws IOException;
	
	void hitCallbacks() throws IOException, InterruptedException, ExecutionException;
}
