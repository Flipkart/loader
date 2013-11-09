package com.flipkart.perf.server.auth.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocketFactory;

/**
 * Creates connections to an LDAP server given the a set of default credentials, but allows those
 * credentials to be overridden on a per-connection basis.
 */
public class LdapConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(LdapConnectionFactory.class);

    private final String server;
    private final int port;
    private final String userDN;
    private final String password;

    public LdapConnectionFactory(String server, int port, String userDN, String password) {
        this.server = server;
        this.port = port;
        this.userDN = userDN;
        this.password = password;
    }

    public LDAPConnection getLDAPConnection() throws LDAPException {
        return getLDAPConnection(userDN, password);
    }

    public LDAPConnection getLDAPConnection(String userDN, String password) throws LDAPException {
        // Use the following if your LDAP server doesn't have a valid certificate.
    /*
    SSLUtil sslUtil = new SSLUtil(new TrustAllTrustManager());

    LDAPConnection ldapConnection = null;
    try {
      ldapConnection = new LDAPConnection(sslUtil.createSSLSocketFactory());
    } catch (GeneralSecurityException gse) {
      logger.error("Couldn't create SSL socket factory", gse);
    }
    */

        LDAPConnection ldapConnection = new LDAPConnection(SSLSocketFactory.getDefault());

        ldapConnection.connect(server, port);
        ldapConnection.bind(userDN, password);

        return ldapConnection;
    }
}