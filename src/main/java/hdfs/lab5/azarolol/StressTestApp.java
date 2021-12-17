package hdfs.lab5.azarolol;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
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
import akka.stream.javadsl.Sink;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
    private static final long DURATION_TIME = 1000;

    public static void main(String[] args) throws IOException {
        System.out.println(WELCOME_MESSAGE);
        ActorSystem system = ActorSystem.create(ACTOR_SYSTEM_NAME);
        ActorRef storage = system.actorOf(Props.create(StorageActor.class));
        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = createFlow(http, storage, materializer);
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

    private static Flow<HttpRequest,HttpResponse,NotUsed> createFlow(Http http, ActorRef storage, ActorMaterializer materializer) {
        return Flow.of(HttpRequest.class).map(
                request -> {
                    Query query = request.getUri().query();
                    String path = query.get(QUERY_PATH).get();
                    int count = Integer.parseInt(query.get(QUERY_COUNT).get());
                    return new Pair<>(path, count);
                })
                .mapAsync(PARALLELISM_NUMBER, request -> {
                    Patterns.ask(
                            storage,
                            new GetResultMessage(request.first()),
                            Duration.ofMillis(DURATION_TIME))
                            .thenCompose(response -> {
                                if (((Optional<Long>) response).isPresent()) {
                                    return CompletableFuture.completedFuture(((Optional<?>) response).get());
                                } else {
                                    Sink<Pair<String, Integer>, CompletionStage<Long>> testSink = createSink();
                                }
                            }
                    )
                })
        )
    }

    private static Sink<Pair<String, Integer>, CompletionStage<Long>> createSink() {
        return Flow.<Pair<String, Integer>>create()
                .mapConcat(request ->
                        new ArrayList<>(Collections.nCopies(request.second(), request.first())))
                .mapAsync(PARALLELISM_NUMBER, path -> {
                    long currentTime = System.currentTimeMillis();
                    Dsl.get()
                })
    }
}
