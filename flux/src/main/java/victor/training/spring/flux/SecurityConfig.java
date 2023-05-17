package victor.training.spring.flux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.client.RestTemplate;

@Slf4j
@EnableWebFluxSecurity
public class SecurityConfig  {
    // The NEW Spring Security config style
    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {
        return http.csrf().disable()
                .authorizeExchange()
                .anyExchange().permitAll()
                .and().build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Equivalent in OLD style
//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//    http.csrf().disable().authorizeRequests().anyRequest().permitAll();
//  }
}
