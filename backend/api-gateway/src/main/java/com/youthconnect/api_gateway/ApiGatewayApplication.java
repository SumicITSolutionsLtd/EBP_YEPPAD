package com.youthconnect.api_gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ApiGatewayApplication.class, args);
		logApplicationStartup(context.getEnvironment());
	}

	/**
	 * ✅ CRITICAL FIX FOR EUREKA CONNECTION ERRORS
	 *
	 * The Eureka Server often sends responses as 'application/octet-stream' (binary)
	 * even though it is JSON data. The default RestTemplate throws an UnknownContentTypeException.
	 *
	 * This bean configures a custom converter to treat 'octet-stream' as JSON.
	 */
	@Bean
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

		// Explicitly tell Jackson to handle application/octet-stream
		List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
		mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
		converter.setSupportedMediaTypes(mediaTypes);

		// Add to the front of the list to ensure it takes precedence
		restTemplate.getMessageConverters().add(0, converter);

		return restTemplate;
	}

	private static void logApplicationStartup(Environment env) {
		String protocol = "http";
		if (env.getProperty("server.ssl.key-store") != null) {
			protocol = "https";
		}
		String serverPort = env.getProperty("server.port", "8080");
		String contextPath = env.getProperty("server.servlet.context-path", "/");
		if (contextPath.equals("/")) contextPath = "";

		String hostAddress = "localhost";
		try {
			hostAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			log.warn("The host name could not be determined, using `localhost` as fallback");
		}

		log.info("""
            
            ----------------------------------------------------------
            🚀 API Gateway Started Successfully!
            ----------------------------------------------------------
            📌 Application: \t{}
            🌐 Local URL:   \t{}://localhost:{}{}
            🌍 External IP: \t{}://{}:{}{}
            ----------------------------------------------------------
            """,
				env.getProperty("spring.application.name"),
				protocol, serverPort, contextPath,
				protocol, hostAddress, serverPort, contextPath
		);
	}
}