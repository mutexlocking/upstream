package com.daangn.clone.chattingroom.dto.polling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class NewContentRequest {

    @NotNull(message = "새 메세지가 왔는지의 여부를 알기 위해선, 어느 채팅방의 새 메세지 여부를 확인할지에 대한 chattingRoomId 값이 필수 입니다.")
    private Long chattingRoomId;

    @NotNull(message = "클라이언트가 가지고 있는 마지막 ChattingContentId를 필수로 알아야 합니다.")
    private Long lastChattingContentId;
}
