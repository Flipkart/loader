package io;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.google.common.io.CharStreams;

public class Reader {
	
	public static String readRequestBody(HttpServletRequest request) throws IOException {
		return CharStreams.toString(request.getReader());
	}
	
}
