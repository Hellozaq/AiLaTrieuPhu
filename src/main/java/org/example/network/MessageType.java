package org.example.network;

public enum MessageType {
    // Client to Server
    C2S_CONNECT_REQUEST, // Yêu cầu kết nối ban đầu, gửi kèm PlayerModel
    C2S_GET_ROOM_LIST,
    C2S_CREATE_ROOM,     // Payload: BetAmount (có thể thêm tên phòng nếu muốn)
    C2S_JOIN_ROOM,       // Payload: RoomID
    C2S_LEAVE_ROOM,
    C2S_LOBBY_CHAT,      // Payload: ChatMessage
    C2S_PLAYER_READY,    // Payload: boolean (sẵn sàng / không sẵn sàng)

    // Server to Client
    S2C_CONNECTION_ACKNOWLEDGED, // Xác nhận kết nối thành công, có thể gửi lại thông tin Player đã được server cập nhật
    S2C_ERROR,                 // Payload: ErrorMessage
    S2C_ROOM_LIST_UPDATE,      // Payload: List<RoomInfo> (RoomInfo là một class nhỏ chứa thông tin tóm tắt của phòng)
    S2C_ROOM_JOINED,           // Payload: Room (thông tin chi tiết phòng đã vào, bao gồm cả đối thủ nếu có)
    S2C_ROOM_LEFT,
    S2C_OPPONENT_JOINED_ROOM,  // Payload: PlayerModel (của đối thủ)
    S2C_OPPONENT_LEFT_ROOM,
    S2C_OPPONENT_READY_STATUS, // Payload: PlayerModel (của đối thủ), boolean (trạng thái sẵn sàng)
    S2C_LOBBY_CHAT,            // Payload: Username, ChatMessage
    S2C_GAME_STARTING,         // Payload: RoomID (hoặc thông tin khởi tạo game)
    S2C_UPDATE_PLAYER_INFO,    // Payload: PlayerModel (cập nhật tiền,...)

    // Thông điệp trong game (sẽ định nghĩa sau)
    C2S_SUBMIT_ANSWER,
    S2C_QUESTION,
    S2C_ANSWER_RESULT,
    S2C_GAME_OVER
}