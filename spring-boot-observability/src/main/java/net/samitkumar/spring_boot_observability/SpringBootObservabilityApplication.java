package net.samitkumar.spring_boot_observability;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
public class SpringBootObservabilityApplication {

	public static void main(String[] args) {
        System.setProperty("reactor.netty.http.server.accessLogEnabled", "true");
		SpringApplication.run(SpringBootObservabilityApplication.class, args);
	}

    @Bean
    JsonPlaceHolderClient jsonPlaceHolderClient(WebClient.Builder webClientBuilder) {

        WebClientAdapter adapter = WebClientAdapter
                .create(webClientBuilder.baseUrl("https://jsonplaceholder.typicode.com/").build());
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(JsonPlaceHolderClient.class);
    }

    @Bean
    RouterFunction<ServerResponse> routerFunction(JsonPlaceHolderHandler jsonPlaceHolderHandler) {
        return RouterFunctions.route()
                .GET("/metadata", request -> {
                    var header = request.headers();
                    var cookies = request.cookies();
                    return Mono.fromCallable(() -> Map.of("headers", header.asHttpHeaders().toSingleValueMap(), "cookies", cookies.toSingleValueMap()))
                            .flatMap(ServerResponse.ok()::bodyValue);
                })
                .GET("/ping", request -> ServerResponse.ok().bodyValue("PONG!"))
                .path("/json-placeholder", builder -> builder
                        .GET("/users", jsonPlaceHolderHandler::getUsers))
                .build();
    }
}

@HttpExchange("/users")
interface JsonPlaceHolderClient {
    @GetExchange
    Mono<List<User>> getUsers();
}
record User(Integer id, String name, String username, String email, String phone, String website) {}

@Component
@RequiredArgsConstructor
class JsonPlaceHolderHandler {
    final JsonPlaceHolderClient jsonPlaceHolderClient;

    public Mono<ServerResponse> getUsers(ServerRequest request) {
        return jsonPlaceHolderClient
                .getUsers()
                .flatMap(this::simulateError)
                .onErrorResume(Mono::error)
                .flatMap(ServerResponse.ok()::bodyValue);
    }

    private Mono<?> simulateError(List<User> users) {

        if (ThreadLocalRandom.current().nextInt(10) < 3) {
            return Mono.error(new RuntimeException("Simulated failure for demo"));
        }
        return Mono.just(users);
    }
}