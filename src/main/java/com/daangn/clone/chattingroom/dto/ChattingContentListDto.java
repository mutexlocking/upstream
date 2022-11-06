package com.daangn.clone.chattingroom.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChattingContentListDto {

    private Integer sizeOfChttingContent;
    private List<ChattingContentDto> chattingContentDtoList;

}
