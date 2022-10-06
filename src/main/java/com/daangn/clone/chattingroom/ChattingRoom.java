package com.daangn.clone.chattingroom;

import com.daangn.clone.common.BasicEntity;
import com.daangn.clone.chattingcontent.ChattingContent;
import com.daangn.clone.common.enums.Status;
import com.daangn.clone.item.Item;
import com.daangn.clone.memberchatting.MemberChatting;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "chatting_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChattingRoom extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "chatting_room_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @JoinColumn(name = "item_id",insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    @Column(name = "item_id")
    private Long itemId;

    @OneToMany(mappedBy = "chattingRoom")
    private List<ChattingContent> chattingContentList = new ArrayList<>();

    @OneToMany(mappedBy = "chattingRoom")
    private List<MemberChatting> memberChattingList = new ArrayList<>();

    /** [연관관계 편의 메서드]*/

    //ChattingRoom과 Item
    public void setItem(Item item){
        this.item = item;
        item.getChattingRoomList().add(this);
    }

    /** [생성자]*/
    public ChattingRoom(Status status, Item item) {
        this.status = status;
        if(item!=null){
            setItem(item);
        }
    }
}
