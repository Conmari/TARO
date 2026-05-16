package scari.corp.taro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TaroApplication {

	static void main(String[] args) {
		SpringApplication.run(TaroApplication.class, args);
	}

}
