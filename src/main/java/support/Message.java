package support;

import java.util.Objects;

public class Message {
    private static Integer idSequence = 0;

    public Integer id;

    public String header;
    public String content;

    public Coord pos;

    public Integer priority;

    public Message(String header, String content, Coord pos, Integer priority) {
        this.id = idSequence++;
        this.header = header;
        this.content = content;
        this.pos = pos;
        this.priority = priority;
    }


    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", header='" + header + '\'' +
                ", pos='" + pos + '\'' +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return priority.equals(message.priority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority);
    }
}
