package com.daangn.clone.chattingroom.dto;

import lombok.*;

import java.time.LocalDateTime;


@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChattingContentDto {
    private Long chattingRoomId;
    private Long targetMemberId;
    private String content;
    private LocalDateTime createdAt;
}
