package com.flipkart.perf.server.auth.dummy;

import com.flipkart.perf.server.auth.User;
import com.google.common.base.Optional;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.basic.BasicCredentials;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.server.Authentication;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 9/11/13
 * Time: 9:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleAuthenticator implements Authenticator<BasicCredentials, User> {
    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        System.out.println("Hello");
        if ("secret".equals(credentials.getPassword())) {
            return Optional.of(new User(credentials.getUsername()));
        }
        return Optional.absent();
    }
}