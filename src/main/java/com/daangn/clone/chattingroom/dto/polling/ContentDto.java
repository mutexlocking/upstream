package com.daangn.clone.chattingroom.dto.polling;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentDto {
    private Long chattingContentId;
    private Long chattingRoomId;
    private Long targetMemberId;
    private String content;
    private LocalDateTime createdAt;
}
