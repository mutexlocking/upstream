package com.daangn.clone.websocket;

import lombok.*;
import org.springframework.web.socket.WebSocketSession;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatInfo {
    private WebSocketSession socketSession;
    private Long sellerId;
    private Long buyerId;
}
