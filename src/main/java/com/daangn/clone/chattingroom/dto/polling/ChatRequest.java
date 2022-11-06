package com.daangn.clone.chattingroom.dto.polling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotNull(message = "어떤 채팅방에 메세지를 보낼지에 대한 chattingRoomId 값은 필수 입니다.")
    private Long chattingRoomId;
    @NotNull(message = "어떤 상대방에게 메세지를 보낼지에 대한 targetMemberId 값은 필수 입니다.")
    private Long targetMemberId;
    @NotNull(message = "메세지 내용은 필수 입니다.")
    private String content;
}
