package com.daangn.clone.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Slf4j
@Controller
public class SocketController{

    @GetMapping("/chat")
    public String getChat(){
        log.info("@ChatController, chat GET()");
        return "chat";
    }

}
