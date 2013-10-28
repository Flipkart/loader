package com.flipkart.perf.common.util;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * nitinka
 * JsonHelper allows you to extract nested key values from given json source.
 */
public class JsonHelper {
    private static String INDEX_PATTERN = "(.+)\\[([0-9]+)\\]";
    private static Pattern pattern;
    private static final ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        pattern = Pattern.compile(INDEX_PATTERN);
    }

    public static String getValueAsString(String jsonSource, String key) throws IOException {
        JsonNode valueNode = get(jsonSource, key);
        if(valueNode != null) {
            return valueNode.getTextValue();
        }
        return null;
    }

    public static Integer getValueAsInt(String jsonSource, String key) throws IOException {
        JsonNode valueNode = get(jsonSource, key);
        if(valueNode != null) {
            return valueNode.getIntValue();
        }
        return null;
    }

    public static Long getValueAsLong(String jsonSource, String key) throws IOException {
        JsonNode valueNode = get(jsonSource, key);
        if(valueNode != null) {
            return valueNode.getLongValue();
        }
        return null;
    }

    public static Boolean getValueAsBoolean(String jsonSource, String key) throws IOException {
        JsonNode valueNode = get(jsonSource, key);
        if(valueNode != null) {
            return valueNode.getBooleanValue();
        }
        return null;
    }

    public static Double getValueAsDouble(String jsonSource, String key) throws IOException {
        JsonNode valueNode = get(jsonSource, key);
        if(valueNode != null) {
            return valueNode.getDoubleValue();
        }
        return null;
    }


    /**
     * Single Json parser which allow user to extract value of a key from given json source.
     * Real value of this function is when you need to retrieve nested or index values.
     *  - You could use key as "Key.SubKey.SubsubKey.SubSubSubKey[3]" . This is going to return third level key value with index 3
     * @param jsonSource
     * @param key
     * @return JsonObject. User Could manipulate or retrieve further value from this Object
     * @throws IOException
     */
    public static JsonNode get(String jsonSource, String key) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(jsonSource);
        String[] keyTokens = key.split("\\.");
        String keyToken = keyTokens[0];
        Matcher m = pattern.matcher(keyToken);
        JsonNode value = null;
        if(m.matches()){
            // its not a plain key , its a indexed key.
            keyToken = m.group(1);
            int index = Integer.parseInt(m.group(2));
            ArrayNode arrayNode = (ArrayNode)jsonNode.get(keyToken);
            value = arrayNode.get(index);
        }
        else {
            value = jsonNode.get(keyToken);
        }

        if(keyTokens.length > 1)
            return get(value.toString(), key.replaceFirst(keyToken + ".", ""));
        return value;
    }
}
