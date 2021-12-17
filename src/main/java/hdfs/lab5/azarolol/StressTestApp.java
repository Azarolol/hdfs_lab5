package hdfs.lab5.azarolol;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.Query;
import akka.japi.Pair;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.io.IOException;
import java.util.concurrent.CompletionStage;

public class StressTestApp {
    private static final String WELCOME_MESSAGE = "Start!";
    private static final String ACTOR_SYSTEM_NAME = "Routes";
    private static final String HOST_NAME = "localhost";
    private static final int PORT = 8080;
    private static final String SERVER_STARTED_MESSAGE = "Server online at http://localhost:8080/" +
            "\nPress RETURN to stop...";
    private static final String QUERY_PATH = "testUrl";
    private static final String QUERY_COUNT = "count";
    private static final int PARALLELISM_NUMBER = 20;

    public static void main(String[] args) throws IOException {
        System.out.println(WELCOME_MESSAGE);
        ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME);
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = createFlow(http, system, materializer);
        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(HOST_NAME, PORT),
                materializer
        );
        System.out.println(SERVER_STARTED_MESSAGE);
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }

    private static Flow<HttpRequest,HttpResponse,NotUsed> createFlow(Http http, ActorSystem system, ActorMaterializer materializer) {
        return Flow.of(HttpRequest.class).map(
                request -> {
                    Query query = request.getUri().query();
                    String path = query.get(QUERY_PATH).get();
                    int count = Integer.parseInt(query.get(QUERY_COUNT).get());
                    return new Pair<>(path, count);
                })
                .mapAsync(PARALLELISM_NUMBER, request -> {
                    Patterns.ask(
                            system,
                            new GetResultMessage(request.first()),
                            
                    )
                })
        )
    }
}
