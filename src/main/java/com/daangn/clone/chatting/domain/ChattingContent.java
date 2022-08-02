package com.daangn.clone.chatting.domain;

import com.daangn.clone.BasicEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "chatting_content")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChattingContent extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "chatting_content_id")
    private Long id;

    private String content;

    @JoinColumn(name = "chatting_room_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChattingRoom chattingRoom;

    /** [연관관계 편의 메서드]*/

    //ChattingContent와 ChattingRoom
    public void setChattingRoom(ChattingRoom chattingRoom){
        this.chattingRoom = chattingRoom;
        chattingRoom.getChattingContentList().add(this);
    }

    /** [생성자]*/
    public ChattingContent(String content, ChattingRoom chattingRoom) {
        this.content = content;
        if(chattingRoom!=null){
            setChattingRoom(chattingRoom);
        }
    }
}
