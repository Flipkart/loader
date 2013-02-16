package com.open.perf.util;

import org.apache.log4j.Logger;

import com.json.JSONArray;

public class HelperUtil {
private static Logger logger    =   Logger.getLogger(HelperUtil.class);
    
    public static String getEqualChars(String str, char ch) {
        String ret = "";
        for(int i=0;i<str.length();i++)
            ret    +=  ch;
        return ret;
    }

    public static String getExceptionString(Exception exception) {
        String exceptionString  =   exception.getClass().getCanonicalName()+": "+exception.getLocalizedMessage()+"\n";
        for(StackTraceElement element : exception.getStackTrace())
            exceptionString     +=  element.toString()+"\n";
        
        Throwable srcException  =   exception.getCause();
        if(srcException!=null) {
            exceptionString         +=   srcException.getClass().getCanonicalName()+": "+srcException.getLocalizedMessage()+"\n";
            for(StackTraceElement element : srcException.getStackTrace())
                exceptionString     +=  element.toString()+"\n";
        }
        return exceptionString.trim();
    }

    public static void delay(int delay) {
    	try {
    		Thread.sleep(delay);
    	}
    	catch(InterruptedException ie) {
    		logger.error(ie);
    	}
    }

    public static boolean isStringAndNotEmpty(Object value) {
        if(value.getClass().getName().contains("String")) {
            if(value.toString().length() > 0)
                return true;
        }
        return false;
    }

    public static boolean isIntegerAndNotEmpty(Object value) {
        if(value.getClass().getName().contains("Integer")) {
            try {
                Integer.parseInt(value.toString());
                return true;
            }
            catch(NumberFormatException nfe){
            }
        }
        return false;
    }

    public static boolean isFloatAndNotEmpty(Object value) {
        if(value.getClass().getName().contains("Float")) {
            try {
                Float.parseFloat(value.toString());
                return true;
            }
            catch(NumberFormatException nfe){
            }
        }
        return false;
    }

    public static boolean isJSONAndNotEmpty(Object value) {
        if(value.getClass().getName().contains("JSONArray")) {
            return ((JSONArray)value).length() > 0;
        }
        return false;
    }

    public static boolean isBooleanAndNotEmpty(Object value) {
        if(value.getClass().getName().contains("Boolean")) {
            return value.toString().equals("true") || value.toString().equals("false");
        }
        return false;
    }

}
