package net.samitkumar.spring_boot_observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@SpringBootApplication
public class SpringBootObservabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootObservabilityApplication.class, args);
	}

    @Bean
    RouterFunction<ServerResponse> routerFunction() {
        return RouterFunctions.route()
                .GET("/metadata", request -> {
                    var header = request.headers();
                    var cookies = request.cookies();
                    return Mono.fromCallable(() -> Map.of("headers", header.asHttpHeaders().toSingleValueMap(), "cookies", cookies.toSingleValueMap()))
                            .flatMap(ServerResponse.ok()::bodyValue);
                })
                .GET("/ping", request -> ServerResponse.ok().bodyValue("PONG!"))
                .build();
    }

}
