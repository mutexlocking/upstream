package com.daangn.clone.item.domain;

import com.daangn.clone.BasicEntity;
import com.daangn.clone.enums.Status;
import com.daangn.clone.member.domain.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "wish")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wish extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "wish_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "item_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    /** [얀관관계 편의 메서드]*/

    //Wish와 Member
    public void setMember(Member member){
        this.member = member;
        member.getWishList().add(this);
    }

    //Wish와 Item
    public void setItem(Item item){
        this.item = item;
        item.getWishList().add(this);
    }

    /** [생성자]*/
    public Wish(Status status, Member member, Item item) {
        this.status = status;
        if(member!=null){
            setMember(member);
        }
        if(item!=null){
            setItem(item);
        }

    }
}
