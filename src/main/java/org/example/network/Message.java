package org.example.network;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L; // Để đảm bảo tương thích khi serialize

    private MessageType type;
    private Object payload; // Dữ liệu kèm theo (có thể là String, PlayerModel, RoomInfo, List<RoomInfo>, etc.)

    public Message(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", payload=" + (payload != null ? payload.toString() : "null") +
                '}';
    }
}