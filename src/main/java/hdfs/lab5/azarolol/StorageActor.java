package hdfs.lab5.azarolol;

import akka.actor.AbstractActor;

import java.util.HashMap;
import java.util.Map;

public class StorageActor extends AbstractActor {

    private final Map<String, Long> storage = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(
                        GetResultMessage.class,
                        message -> sender().tell(
                                storage.get(message.getPath()),
                                self())
                )
                .match(
                        SaveResultMessage.class,
                        message -> storage.put(message.getPath(), message.getTime())
                )
                .build();
    }
}
