package com.daangn.clone.memberchatting;

import com.daangn.clone.common.BasicEntity;
import com.daangn.clone.chattingroom.ChattingRoom;
import com.daangn.clone.common.enums.InRoomYn;
import com.daangn.clone.common.enums.Role;
import com.daangn.clone.member.Member;
import lombok.*;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "member_chatting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberChatting extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "member_chatting_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private InRoomYn inRoomYn;

    @JoinColumn(name = "member_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "member_id")
    private Long memberId;

    @JoinColumn(name = "chatting_room_id", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ChattingRoom chattingRoom;

    @Column(name = "chatting_room_id")
    private Long chattingRoomId;

    /** [연관관계 편의 메서드]*/

    //MemberChatting과 Member
    public void setMember(Member member){
        this.member = member;
        member.getMemberChattingList().add(this);
    }

    //MemberChatting과 ChattingRoom
    public void setChattingRoom(ChattingRoom chattingRoom){
        this.chattingRoom = chattingRoom;
        chattingRoom.getMemberChattingList().add(this);
    }

    /** [생성자]*/
    public MemberChatting(Role role, InRoomYn inRoomYn, Member member, ChattingRoom chattingRoom) {
        this.role = role;
        this.inRoomYn = inRoomYn;

        if(member != null){
            setMember(member);
        }

        if(chattingRoom!=null){
            setChattingRoom(chattingRoom);
        }

    }

    /** [필드 변경 메서드]*/
    public void updateInRoomYn(InRoomYn yn){
        this.inRoomYn = yn;
    }
}
