package templates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang.text.StrSubstitutor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.dropwizard.json.JsonSnakeCase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ThreadSafe
@JsonSnakeCase
public class Template implements Comparable<Template> {
	
	private String content;
	private String templateName;
	private String urlEndpoint;
	private boolean async;
	private String requestMethod;
	private Map<String,String> staticHeaders;
	private String params;
	private String urlRegexPattern;
	private Long waitTimeInSec;
	private Long fireCallbackAfter;
	private int priority;
	private String requestBodyRegexPattern;
	
	@JsonIgnore
	private static List<Template> templates = new ArrayList<Template>();
	@JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper();
		
	public String getResponseBodyFromTemplate(Map<String,Object> substituteMap) {
		StrSubstitutor substitutor = new StrSubstitutor(substituteMap);
		return substitutor.replace(content);
	}
	
	public String getUrlFromTemplate(Map<String,Object> substituteMap) {
		StrSubstitutor substitutor = new StrSubstitutor(substituteMap);
		return substitutor.replace(urlEndpoint);
	}
	
	public String getParamsFromTemplate(Map<String,Object> substituteMap) {
		StrSubstitutor substitutor = new StrSubstitutor(substituteMap);
		return substitutor.replace(params);
	}
	
	public static void createTemplate(String content, String fileName, String templateFileBasePath, 
			boolean async, String urlRegexPattern, String urlEndpoint, String requestMethod
			, String params, Map<String,String> staticHeaders, Long waitTimeInSec
			, Long fireCallbackAfter, int priority, String requestBodyRegexPattern) throws IOException {
		
		File file = new File(templateFileBasePath+"/"+fileName);
		if(!file.exists()) {
			file.createNewFile();
		}
		OutputStreamWriter outputStreamWriter = null;
		BufferedWriter writer = null;
		try {
			outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file));
			writer = new BufferedWriter(outputStreamWriter);
			writer.append(content);
			
			Template template = new Template();
			template.setContent(content);
			template.setTemplateName(fileName);
			template.setUrlRegexPattern(urlRegexPattern);
			template.setUrlEndpoint(urlEndpoint);
			template.setAsync(async);
			template.setRequestMethod(requestMethod);
			template.setStaticHeaders(staticHeaders);
			template.setParams(params);
			template.setWaitTimeInSec(waitTimeInSec);
			template.setFireCallbackAfter(fireCallbackAfter);
			template.setPriority(priority);
			template.setRequestBodyRegexPattern(requestBodyRegexPattern);
			
			File templateAssocs = new File(templateFileBasePath+"/assoc");
			if(!templateAssocs.exists()) {
				templateAssocs.createNewFile();
			}
			List<Template> templates = new ArrayList<Template>();
			if(!isFileEmpty(templateAssocs))
				templates = mapper.readValue(templateAssocs, new TypeReference<List<Template>>() {});
			for(Template t:templates)
				if(t.getTemplateName().equalsIgnoreCase(template.getTemplateName())) {
					templates.remove(t);
					break;
				}
			templates.add(template);
			mapper.writeValue(templateAssocs, templates);
			Template.templates.add(template);
			// sort according to priority
			Collections.sort(Template.templates);
		} catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if(writer != null)
				writer.close();
			if(outputStreamWriter != null)
				outputStreamWriter.close();
		}
	}
	
	private static boolean isFileEmpty(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));     
		if (br.readLine() == null) {
		    return true;
		}
		return false;
	}
		
	public static void loadAllTemplates(String templateFileBasePath) throws IOException {		
		File templateAssocs = new File(templateFileBasePath+"/assoc");	
		List<Template> templates = mapper.readValue(templateAssocs, new TypeReference<List<Template>>() {});
		for(Template template:templates) {
			Template.templates.add(template);
		}
		Collections.sort(Template.templates);
	}
	
	public static Template getTemplate(String url, String requestBody, String requestMethod) {
		boolean passed = false;
		for(Template template:templates) {
			if(url != null && template.getUrlRegexPattern() != null) {
				passed =  Pattern.matches(template.getUrlRegexPattern(), url);
				if(!passed) continue;
			}
			if(requestMethod != null && template.getRequestMethod() != null) {
				passed = template.getRequestMethod().equalsIgnoreCase(requestMethod);
				if(!passed) continue;
			}
			if(requestBody != null && template.getRequestBodyRegexPattern() != null) {
				passed = Pattern.matches(template.getRequestBodyRegexPattern(), requestBody);
				if(!passed) continue;
			}
			return template;
		}
		return null;
	}

	public static Template getTemplate(String templateName2) {
		for(Template template:templates) {
			if(template.getTemplateName().equalsIgnoreCase(templateName2))
				return template;
		}
		return null;
	}

	/**
	 * for use  by comparator
	 */
	public int compareTo(Template template) {
		return this.getPriority()-template.getPriority();
	}
	
}
