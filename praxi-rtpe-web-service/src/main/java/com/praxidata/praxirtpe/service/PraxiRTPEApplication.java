/**
 * 
 */
package com.praxidata.praxirtpe.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author SURENDRANATH
 *
 */
@Configuration
@ComponentScan(basePackages = "com.praxidata.praxirtpe")
@Import({ WebConfig.class })
@EnableAutoConfiguration
public class PraxiRTPEApplication extends SpringBootServletInitializer {
	private static Class applicationClass = PraxiRTPEApplication.class;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(PraxiRTPEApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(applicationClass);
	}
}
