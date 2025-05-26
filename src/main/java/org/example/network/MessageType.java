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
    C2S_SUBMIT_ANSWER,      // Client gửi câu trả lời
    C2S_LEAVE_GAME,
    C2S_LOGIN_REQUEST,      // Payload: Object[]{username, password}
    C2S_REGISTER_REQUEST,   // Payload: Object[]{username, password}

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
    S2C_LOGIN_SUCCESS,      // Payload: PlayerModel
    S2C_LOGIN_FAILURE,      // Payload: String errorMessage
    S2C_REGISTER_SUCCESS,   // Payload: PlayerModel (hoặc chỉ là thông báo thành công)
    S2C_REGISTER_FAILURE,   // Payload: String errorMessage
    S2C_HELP_RESULT_5050,   // Payload: new Object[]{questionId, optionIndexToRemove1, optionIndexToRemove2} (1-4)
    S2C_HELP_RESULT_CALL,   // Payload: new Object[]{questionId, suggestedOptionIndex, confidence} (ví dụ: "Chuyên gia gợi ý: C (70%)")
    S2C_HELP_RESULT_AUDIENCE,// Payload: new Object[]{questionId, Map<Integer, Double> pollResults} (ví dụ: {1:0.6, 2:0.1, 3:0.2, 4:0.1})
    S2C_OPPONENT_USED_HELP, // Payload: new Object[]{opponentUsername, String helpTypeDescription} (ví dụ: "đã dùng 50/50")
    S2C_HELP_UNAVAILABLE,


    // Thông điệp trong game
    S2C_QUESTION,
    S2C_ANSWER_RESULT,
    S2C_GAME_OVER,
    S2C_UPDATE_GAME_SCORE,
    S2C_GAME_OVER_SCORE,
    S2C_GAME_QUESTION, // Server thông báo kết thúc game
    S2C_OPPONENT_ANSWERED,  // (Tùy chọn) Thông báo đối thủ đã trả lời
    S2C_TIME_UP,


    // Client to Server - Yêu cầu sử dụng trợ giúp
    C2S_USE_HELP_5050,      // Payload: new Object[]{roomId, questionId} (questionId để xác nhận)
    C2S_USE_HELP_CALL,      // Payload: new Object[]{roomId, questionId}
    C2S_USE_HELP_AUDIENCE,  // Payload: new Object[]{roomId, questionId}


}