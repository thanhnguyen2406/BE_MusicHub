package musichub;

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
			System.setProperty("SECURITY_PERMIT_PATHS", dotenv.get("SECURITY_PERMIT_PATHS", ""));

			System.setProperty("API_VERSION", dotenv.get("API_VERSION", ""));
			System.setProperty("API_CONTACT_NAME", dotenv.get("API_CONTACT_NAME", ""));
			System.setProperty("API_CONTACT_EMAIL", dotenv.get("API_CONTACT_EMAIL", ""));
			System.setProperty("API_TITLE", dotenv.get("API_TITLE", ""));
			System.setProperty("API_DESCRIPTION", dotenv.get("API_DESCRIPTION", ""));
			System.setProperty("API_DEV_URL", dotenv.get("API_DEV_URL", ""));
			System.setProperty("API_DEV_DESCRIPTION", dotenv.get("API_DEV_DESCRIPTION", ""));
			System.setProperty("API_DEV_URL", dotenv.get("API_DEV_URL", ""));
			System.setProperty("API_PROD_DESCRIPTION", dotenv.get("API_PROD_DESCRIPTION", ""));

			System.setProperty("CORS_ALLOWED_ORIGINS", dotenv.get("CORS_ALLOWED_ORIGINS", ""));
			System.setProperty("CORS_ALLOWED_METHODS", dotenv.get("CORS_ALLOWED_METHODS", ""));
			System.setProperty("CORS_ALLOWED_HEADERS", dotenv.get("CORS_ALLOWED_HEADERS", ""));
			System.setProperty("CORS_ALLOW_CREDENTIALS", dotenv.get("CORS_ALLOW_CREDENTIALS", ""));

			System.setProperty("SPRING_DATA_MONGODB_URI", dotenv.get("SPRING_DATA_MONGODB_URI", ""));
			System.setProperty("SPRING_DATA_MONGODB_USERNAME", dotenv.get("SPRING_DATA_MONGODB_USERNAME", ""));
			System.setProperty("SPRING_DATA_MONGODB_PASSWORD", dotenv.get("SPRING_DATA_MONGODB_PASSWORD", ""));

			System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT", ""));
			System.setProperty("SERVER_CONTEXT_PATH", dotenv.get("SERVER_CONTEXT_PATH", ""));

			System.setProperty("JWT_ISSUER_URI", dotenv.get("JWT_ISSUER_URI", ""));
			System.setProperty("JWT_JWK_SET_URI", dotenv.get("JWT_JWK_SET_URI", ""));
			System.setProperty("JWT_AUTH_CONVERTER_RESOURCE_ID", dotenv.get("JWT_AUTH_CONVERTER_RESOURCE_ID", ""));
			System.setProperty("JWT_AUTH_CONVERTER_PRINCIPLE_ATTRIBUTE", dotenv.get("JWT_AUTH_CONVERTER_PRINCIPLE_ATTRIBUTE", ""));
			System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION", ""));

			System.setProperty("KEYCLOAK_REALM", dotenv.get("KEYCLOAK_REALM", ""));
			System.setProperty("KEYCLOAK_DOMAIN", dotenv.get("KEYCLOAK_DOMAIN", ""));
			System.setProperty("KEYCLOAK_CLIENT_ID", dotenv.get("KEYCLOAK_CLIENT_ID", ""));
			System.setProperty("KEYCLOAK_CLIENT_SECRET", dotenv.get("KEYCLOAK_CLIENT_SECRET", ""));
			System.setProperty("KEYCLOAK_URLS_AUTH", dotenv.get("KEYCLOAK_URLS_AUTH", ""));

			System.setProperty("SERVER_RSOCKET_PORT", dotenv.get("SERVER_RSOCKET_PORT", ""));
			System.setProperty("SERVER_RSOCKET_MAPPING_PATH", dotenv.get("SERVER_RSOCKET_MAPPING_PATH", ""));
			System.setProperty("SERVER_RSOCKET_TRANSPORT", dotenv.get("SERVER_RSOCKET_TRANSPORT", ""));
		}

		SpringApplication.run(MusicHubApplication.class, args);
	}

}
