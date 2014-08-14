package templates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
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
public class Template {
	
	@JsonIgnore
	private String content;
	private String templateName;
	private String urlEndpoint;
	private boolean async;
	private String requestMethod;
	private Map<String,String> staticHeaders;
	private String params;
	private String urlRegexPattern;
	
	@JsonIgnore
	private static Map<String,Template> templates = new HashMap<String, Template>();
	@JsonIgnore
	private static Map<String,Template> urlRegexTemplateAssoc = new HashMap<String, Template>();
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
	
	public String loadTemplate(String filePath) throws IOException {
		File file = new File(filePath);
		BufferedReader reader = null;
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(new FileInputStream(file));
			reader = new BufferedReader(inputStreamReader);
			String l;
			StringBuffer templateBuffer = new StringBuffer();
			while((l = reader.readLine()) != null) {
				templateBuffer.append(l);
			}
			if(templateBuffer.length() > 0)
				content = templateBuffer.toString();
			templateName = file.getName();
			templates.put(file.getName(), this);
			return content;
		}
		finally {
			if(null != inputStreamReader)
				inputStreamReader.close();
			if(null != reader)
				reader.close();
		}
	}
	
	public static void createTemplate(String content, String fileName, String templateFileBasePath, 
			boolean async, String urlRegexPattern, String urlEndpoint, String requestMethod, String params, Map<String,String> staticHeaders) throws IOException {
		
		File file = new File(templateFileBasePath+"/"+fileName);
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
			
			File templateAssocs = new File(templateFileBasePath+"/assoc");
			List<Template> templates = mapper.readValue(templateAssocs, new TypeReference<List<Template>>() {});
			templates.add(template);
			mapper.writeValue(templateAssocs, templates);
			
			Template.templates.put(fileName, template);
			urlRegexTemplateAssoc.put(urlRegexPattern, template);
		}
		finally {
			if(writer != null)
				writer.close();
			if(outputStreamWriter != null)
				outputStreamWriter.close();
		}
	}
		
	public static void loadAllTemplates(String templateFileBasePath) throws IOException {
		File baseFolder = new File(templateFileBasePath);
		File[] files = baseFolder.listFiles();
		
		for(File file:files) {
			if(!file.isFile()) continue;
			Template template = new Template();
			template.loadTemplate(file.getPath());
			templates.put(file.getName(),template);
		}
		
		File templateAssocs = new File(templateFileBasePath+"/assoc");	
		List<Template> templates = mapper.readValue(templateAssocs, new TypeReference<List<Template>>() {});
		for(Template template:templates) {
			template.loadTemplate(templateFileBasePath+template.getTemplateName());
			urlRegexTemplateAssoc.put(template.getUrlRegexPattern(), template);
		}
	}
	
	public static Template getTemplate(String templateName) {
		// fileName same as templateName
		Template template = templates.get(templateName);
		if(null != template)
			return template;
		else return null;
	}
	
	public static Template getTemplateForUrl(String url) {
		for(String regex:urlRegexTemplateAssoc.keySet()) {
			if (Pattern.matches(regex, url))
				return urlRegexTemplateAssoc.get(regex);
		}
		return null;
	}
	
}
