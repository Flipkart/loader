package com.flipkart.perf.server.exception;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 2/1/14
 * Time: 3:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class InvalidJobStateException extends Throwable {
    public InvalidJobStateException(String message) {
        super(message);
    }
}
