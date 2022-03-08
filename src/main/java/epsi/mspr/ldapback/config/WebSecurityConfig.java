package epsi.mspr.ldapback.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import epsi.mspr.ldapback.service.jwt.JwtRequestFilter;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private LdapProperties ldapProperties;
  @Autowired
  private JwtRequestFilter jwtRequestFilter;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors();
    http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/authenticate").permitAll()
            .antMatchers("/api/verify-identity").permitAll()
            .anyRequest().fullyAuthenticated()
            .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.addFilterBefore(this.jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    // http.logout().logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)));
  }
  
  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
      auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
  }

  @Bean
  public AuthenticationProvider activeDirectoryLdapAuthenticationProvider() {
    // Build the Ldap auth provider bean
    ActiveDirectoryLdapAuthenticationProvider authenticationProvider =
            new ActiveDirectoryLdapAuthenticationProvider(ldapProperties.getDomain(), ldapProperties.getProviderUrl());
    authenticationProvider.setConvertSubErrorCodesToExceptions(true);
    authenticationProvider.setUseAuthenticationRequestCredentials(true);
    return authenticationProvider;
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
      return super.authenticationManagerBean();
  }

}
