package com.daangn.clone.chattingroom.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChattingDto {

    private Long myMemberId;
    private Long targetMemberId;
    private Long itemId;
    private Long memberChattingId;
    private Long chattingRoomId;

}
