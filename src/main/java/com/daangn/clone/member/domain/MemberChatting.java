package com.daangn.clone.member.domain;

import com.daangn.clone.BasicEntity;
import com.daangn.clone.chatting.domain.ChattingRoom;
import com.daangn.clone.enums.Role;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "member_chatting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberChatting extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "member_chatting_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Role role;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "chatting_room_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ChattingRoom chattingRoom;

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
    public MemberChatting(Role role, Member member, ChattingRoom chattingRoom) {
        this.role = role;

        if(member != null){
            setMember(member);
        }

        if(chattingRoom!=null){
            setChattingRoom(chattingRoom);
        }

    }
}
