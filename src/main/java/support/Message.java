package support;

public class Message {
    private static Integer idSequence = 0;

    public Integer id;

    public String header;
    public String content;
    public Integer priority;

    public Message(String header, String content, Integer priority) {
        this.id = idSequence++;
        this.header = header;
        this.content = content;
        this.priority = priority;
    }


    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", header='" + header + '\'' +
                ", content='" + content + '\'' +
                ", priority=" + priority +
                '}';
    }
}
