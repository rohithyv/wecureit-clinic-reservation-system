package dev.wecureit.cdex.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
@Configuration public class SecurityConfig {
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService users(PasswordEncoder encoder,@Value("${demo.payer-password}") String payer,@Value("${demo.provider-password}") String provider){
  return new InMemoryUserDetailsManager(
   User.withUsername("payer").password(encoder.encode(payer)).roles("PAYER").build(),
   User.withUsername("provider").password(encoder.encode(provider)).roles("PROVIDER").build());
 }
 @Bean SecurityFilterChain security(HttpSecurity http)throws Exception{
  http.csrf(csrf->csrf.disable()) // Stateless demo-only API using HTTP Basic; replace with OAuth2 resource server in production.
   .authorizeHttpRequests(auth->auth.requestMatchers("/actuator/health").permitAll()
    .requestMatchers(org.springframework.http.HttpMethod.POST,"/api/exchanges").hasRole("PAYER")
    .requestMatchers("/api/**","/fhir/**").hasAnyRole("PAYER","PROVIDER")
    .anyRequest().denyAll())
   .httpBasic(Customizer.withDefaults())
   .sessionManagement(s->s.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS));
  return http.build();
 }
}
