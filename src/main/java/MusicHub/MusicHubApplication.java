package MusicHub;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class MusicHubApplication {

	public static void main(String[] args) {
		File envFile = new File(".env");

		if (envFile.exists() && envFile.isFile()) {
			Dotenv dotenv = Dotenv.configure().load();

			System.setProperty("SPRING_APPLICATION_NAME", dotenv.get("SPRING_APPLICATION_NAME", ""));
			System.setProperty("SPRING_DATA_MONGODB_URI", dotenv.get("SPRING_DATA_MONGODB_URI", ""));
			System.setProperty("SPRING_DATA_MONGODB_USERNAME", dotenv.get("SPRING_DATA_MONGODB_USERNAME", ""));
			System.setProperty("SPRING_DATA_MONGODB_PASSWORD", dotenv.get("SPRING_DATA_MONGODB_PASSWORD", ""));
			System.setProperty("SPRING_JPA_HIBERNATE_DDL_AUTO", dotenv.get("SPRING_JPA_HIBERNATE_DDL_AUTO", ""));
			System.setProperty("SPRING_JPA_SHOW_SQL", dotenv.get("SPRING_JPA_SHOW_SQL", ""));
			System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT", ""));
			System.setProperty("SERVER_CONTEXT_PATH", dotenv.get("SERVER_CONTEXT_PATH", ""));
			System.setProperty("JWT_SIGNER_KEY", dotenv.get("JWT_SIGNER_KEY", ""));
			System.setProperty("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID", dotenv.get("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID", ""));
			System.setProperty("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET", dotenv.get("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET", ""));
			System.setProperty("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_REDIRECT_URI", dotenv.get("SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_REDIRECT_URI", ""));
			System.setProperty("SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_AUTHORIZATION_URI", dotenv.get("SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_AUTHORIZATION_URI", ""));
			System.setProperty("SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_TOKEN_URI", dotenv.get("SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_TOKEN_URI", ""));
			System.setProperty("SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_USER_INFO_URI", dotenv.get("SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_GOOGLE_USER_INFO_URI", ""));
		}

		SpringApplication.run(MusicHubApplication.class, args);
	}

}
