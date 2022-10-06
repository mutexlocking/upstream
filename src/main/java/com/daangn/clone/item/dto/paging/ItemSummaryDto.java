package com.daangn.clone.item.dto.paging;

import com.daangn.clone.chattingroom.ChattingRoom;
import com.daangn.clone.common.enums.DelYn;
import com.daangn.clone.item.Item;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSummaryDto {

    private Long itemId;
    private String title;
    private String townName;
    private LocalDateTime createdAt;
    private Long price;
    private String itemImageUrl;
    private int numOfWish;
    private int numOfChattingRoomList;

}
