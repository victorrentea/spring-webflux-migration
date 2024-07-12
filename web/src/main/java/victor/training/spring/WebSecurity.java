//package victor.training.spring;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//
//import static org.springframework.security.config.Customizer.withDefaults;
//
//@EnableMethodSecurity
//@Configuration
//@EnableWebSecurity
//public class WebSecurity {
//  @Bean
//  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//    http
//        .authorizeHttpRequests(authz -> authz
//            .anyRequest().permitAll()
//        )
//        .httpBasic(withDefaults())
//        .formLogin(withDefaults())
//        .csrf(csrf -> csrf.disable());
//    return http.build();
//  }
//}
