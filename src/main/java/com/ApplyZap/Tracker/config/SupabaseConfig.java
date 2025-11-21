package com.ApplyZap.Tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SupabaseConfig {

    @Value("${supabase.projectUrl}")
    private String projectUrl;

    @Value("${supabase.anon-key}")
    private String anonKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public String getAnonKey() {
        return anonKey;
    }

    public String getAuthUrl() {
        return projectUrl + "/auth/v1/user";
    }
}
