package hdfs.lab5.azarolol;

public class SaveResultMessage {
    private final String path;
    private final float time;

    public SaveResultMessage(String path, float time) {
        this.path = path;
        this.time = time;
    }

    public float getTime() {
        return time;
    }

    public String getPath() {
        return path;
    }
}
