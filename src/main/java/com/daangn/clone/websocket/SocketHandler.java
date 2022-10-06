package com.daangn.clone.websocket;

import com.daangn.clone.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** Client가 보낸 통신을 처리할 Handler - 즉 MVC로 따지면 Controller역할? */
@Component
@RequiredArgsConstructor
@Slf4j
public class SocketHandler extends BinaryWebSocketHandler {

    /** KEY : MemberId , VALUE : 해당 Member와 웹소켓 연결된 session */
    private static Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();


    /** 1. 클라이언트의 소켓 연결 성공시 호출되는 메소드 */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Object payloadObj = message.getPayload();
        String payload = payloadObj.toString();
        JSONObject payloadJson = new JSONObject(payload);

        String role = payloadJson.getString("role");
        long myMemberId = payloadJson.getLong("myMemberId");
        long targetMemberId = payloadJson.getLong("targetMemberId");

        if(role.equals("open")){
            sessionMap.put(myMemberId, session);

        } else if(role.equals("close")){
            sessionMap.remove(myMemberId);

        } else if(role.equals("chat")){
            String content = payloadJson.getString("content");

            //1. 일단 상대방이 웹소켓에 연결되어있는것과 무관하게 DB에 insert
            // 단 상대방이 연결되어 있다면 read_yn을 Y로 하고 ,


            sessionMap.get(targetMemberId).sendMessage(new TextMessage(content));

        }

    }

    /** 3. 클라이언트의 소켓 연결 종료시 호출되는 메소드 */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
    }
}
