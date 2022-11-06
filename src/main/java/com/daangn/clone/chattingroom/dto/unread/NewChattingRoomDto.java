package com.daangn.clone.chattingroom.dto.unread;

import com.daangn.clone.chattingroom.dto.ChattingContentDto;
import com.daangn.clone.chattingroom.dto.ChattingDto;
import com.daangn.clone.chattingroom.dto.ChattingListDto;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewChattingRoomDto {

    /** 새로 생긴 chattingRoomList*/
    private Integer sizeOfChatting;
    private List<ChattingDto> chattingDtoList;


}
