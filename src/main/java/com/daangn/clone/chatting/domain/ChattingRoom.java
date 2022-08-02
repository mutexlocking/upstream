package com.daangn.clone.chatting.domain;

import com.daangn.clone.BasicEntity;
import com.daangn.clone.enums.Status;
import com.daangn.clone.item.domain.Item;
import com.daangn.clone.member.domain.Member;
import com.daangn.clone.member.domain.MemberChatting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "chatting_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChattingRoom extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "chatting_room_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @JoinColumn(name = "item_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

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
