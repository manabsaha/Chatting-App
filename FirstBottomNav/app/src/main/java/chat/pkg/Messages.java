package chat.pkg;

/**
 * Created by Manab on 13-04-2019.
 */

public class Messages {

    private String message;
    private String type;
    private String from;
    private String thumb;
    private long time;
    private Boolean seen;

    public Messages(){

    }

    public Messages(String message, String type, long time, String from, Boolean seen, String thumb) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.from = from;
        this.seen = seen;
        this.thumb = thumb;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

}
