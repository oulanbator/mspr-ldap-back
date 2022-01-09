package epsi.mspr.ldapback.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import epsi.mspr.ldapback.service.jwt.JwtRequestFilter;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private JwtRequestFilter jwtRequestFilter;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.cors();
    http.csrf().disable()
      .authorizeRequests()
      .antMatchers("/authenticate").permitAll()
      .anyRequest().fullyAuthenticated()
      .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.addFilterBefore(this.jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    // http.logout().logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)));
  }

  @Override
  public void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth
      .ldapAuthentication()
        .userDnPatterns("uid={0},ou=people")
        .groupSearchBase("ou=groups")
        .contextSource()
          .url("ldap://localhost:8389/dc=springframework,dc=org")
          .and()
        .passwordCompare()
          .passwordEncoder(new BCryptPasswordEncoder())
          .passwordAttribute("userPassword");
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
      return super.authenticationManagerBean();
  }

}
