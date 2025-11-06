package com.ApplyZap.Tracker;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.*;

@SpringBootApplication
public class TrackerApplication {
    static {
        Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .ignoreIfMissing()
                .load();

        System.out.println("✅ Dotenv loaded entries:");
        dotenv.entries().forEach(e -> System.out.println(e.getKey() + " = " + e.getValue()));

        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }
	public static void main(String[] args) {
		SpringApplication.run(TrackerApplication.class, args);
        System.out.println("From getenv: " + System.getenv("SUPABASE_PROJECT_URL"));
        System.out.println("From getProperty: " + System.getProperty("SUPABASE_PROJECT_URL"));
	}

}
