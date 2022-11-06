package com.daangn.clone.chattingcontent;

import com.daangn.clone.common.BasicEntity;
import com.daangn.clone.chattingroom.ChattingRoom;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "chatting_content")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChattingContent extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "chatting_content_id")
    private Long id;

    private String content;

    @JoinColumn(name = "chatting_room_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ChattingRoom chattingRoom;

    @Column(name = "chatting_room_id")
    private Long chattingRoomId;


    @Column(name = "target_member_id")
    private Long targetMemberId;

    @Column(name = "ack_content_id")
    private Long ackContentId;

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
