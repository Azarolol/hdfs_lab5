package hdfs.lab5.azarolol;

public class GetResultMessage {
    private final String path;

    public GetResultMessage(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
