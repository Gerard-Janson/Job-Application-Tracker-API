package com.gerard.jobapplicationtracker.security;

import com.gerard.jobapplicationtracker.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    CustomUserDetailsService customUserDetailsService;
    @Autowired
    private AuthEntryPoint authEntryPoint;

    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

   @Bean
   CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("http://localhost:3000");
        corsConfiguration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        corsConfiguration.setAllowedHeaders(List.of("Content-Type","Authorization"));
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", corsConfiguration);
        return source;
   }

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(s -> s.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(  a ->
               a.requestMatchers( "/api/v1/tracker/auth/**").permitAll()
               .anyRequest().authenticated());

        http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
   }











}
