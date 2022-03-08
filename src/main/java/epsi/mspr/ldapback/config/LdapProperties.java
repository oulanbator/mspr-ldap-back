package epsi.mspr.ldapback.config;

import org.springframework.stereotype.Component;

@Component
public class LdapProperties {
    private String domain = "chatelet.local";
    private String providerUrl = "ldap://192.168.50.100:389";

    public LdapProperties() {
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getProviderUrl() {
        return providerUrl;
    }

    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    } 
}
