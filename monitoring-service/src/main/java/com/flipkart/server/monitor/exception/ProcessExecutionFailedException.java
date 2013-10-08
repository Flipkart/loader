package com.flipkart.server.monitor.exception;

/**
 * Created by IntelliJ IDEA.
 * User: nitinka
 * Date: 4/1/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessExecutionFailedException extends Exception {
    public ProcessExecutionFailedException(String error) {
        super(error);
    }
}
