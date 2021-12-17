package hdfs.lab5.azarolol;

public class SaveResultMessage {
    private final String path;
    private final Long time;

    public SaveResultMessage(String path, Long time) {
        this.path = path;
        this.time = time;
    }

    public Long getTime() {
        return time;
    }

    public String getPath() {
        return path;
    }
}
