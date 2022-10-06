package com.daangn.clone.chattingroom.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChattingListDto {

    private Integer sizeOfChatting;
    private List<ChattingDto> chattingDtoList;
}
