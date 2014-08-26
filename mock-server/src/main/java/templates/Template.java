package templates;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
	
	public static void createTemplate(String content, String fileName, String templateFileBasePath, 
			boolean async, String urlRegexPattern, String urlEndpoint, String requestMethod, String params, Map<String,String> staticHeaders, Long waitTimeInSec, Long fireCallbackAfter) throws IOException {
		
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
			
			urlRegexTemplateAssoc.put(urlRegexPattern, template);
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
			urlRegexTemplateAssoc.put(template.getUrlRegexPattern(), template);
		}
	}
	
	public static Template getTemplateForUrl(String url) {
		for(String regex:urlRegexTemplateAssoc.keySet()) {
			if (Pattern.matches(regex, url))
				return urlRegexTemplateAssoc.get(regex);
		}
		return null;
	}

	public static Template getTemplate(String templateName2) {
		for(Template t:urlRegexTemplateAssoc.values()) {
			if(t.getTemplateName().equalsIgnoreCase(templateName2))
				return t;
		}
		return null;
	}
	
}
