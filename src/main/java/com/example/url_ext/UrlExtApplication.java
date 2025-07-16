package com.example.url_ext;

import com.example.url_ext.service.urlService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class
})
public class UrlExtApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(UrlExtApplication.class, args);
		urlService u = new urlService();
		u.getUrl();
	}

}
