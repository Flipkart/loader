package io;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

public class Reader {
	
	public static String readRequestBody(HttpServletRequest request) throws IOException {
		BufferedReader reader = request.getReader();
		
		String l;
		StringBuffer buffer = new StringBuffer();
		while((l=reader.readLine())!=null) {
			buffer.append(l);
		}
		return buffer.toString();
	}
	
}
