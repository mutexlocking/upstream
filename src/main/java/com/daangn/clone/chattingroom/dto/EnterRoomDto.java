package com.daangn.clone.chattingroom.dto;

import com.daangn.clone.common.enums.InRoomYn;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnterRoomDto {

    private Long memberId;               //1. 방을 들어온 or 나간 Member의 Id
    private Long memberChattingRoomId;  //2. 어떤 방에 대해 입.퇴실을 하였는지 , 그 ChattingRoom과 연관된 MemberChattingRoom의 Id
    private InRoomYn inRoomYn;         //3. 그 ChattingRoom에 대해 입장하였는지 or 퇴장하였는지 (update 결과)
}
