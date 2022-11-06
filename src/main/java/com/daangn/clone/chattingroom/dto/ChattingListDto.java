package com.daangn.clone.chattingroom.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChattingListDto {

    private List<ChattingDto> chattingDtoList;
    private Integer sizeOfChatting;
}
