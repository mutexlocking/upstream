package com.daangn.clone.item.domain;

import com.daangn.clone.BasicEntity;
import com.daangn.clone.chatting.domain.ChattingRoom;
import com.daangn.clone.enums.DelYn;
import com.daangn.clone.member.domain.Member;
import com.daangn.clone.member.domain.Town;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "item_id")
    private Long id;

    private String title;
    private String content;
    private Long price; // int값을 초과하는 가격이 입력될 수도 있으니깐

    @Column(name = "visit_count")
    private int visitCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn")
    private DelYn delYn;

    @JoinColumn(name = "seller_member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member sellerMember;

    @JoinColumn(name = "category_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @JoinColumn(name = "town_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Town town;

    @OneToMany(mappedBy = "item")
    private List<ItemImage> itemImageList = new ArrayList<>();

    @OneToMany(mappedBy = "item")
    private List<Wish> wishList = new ArrayList<>();

    @OneToMany(mappedBy = "item")
    private List<ChattingRoom> chattingRoomList = new ArrayList<>();

    /** 어쩔 수 없이 객체가 아닌 Id로 직접 저장*/
    @Column(name = "buyer_member_id")
    private Long buyer_member_id;

    /** [연관관계 편의 메서드]*/

    //Item과 sellerMember
    public void setMember(Member sellerMember){
        this.sellerMember = sellerMember;
        sellerMember.getItemList().add(this);

    }

    //Item과 Category
    public void  setCategory(Category category){
        this.category = category;
        category.getItemList().add(this);
    }

    //Item과 Town
    public void setTown(Town town){
        this.town = town;
        town.getItemList().add(this);
    }

    //Item과 buyerMember간 특수항 경우
    public void seyBuyerMemberId(Long buyer_member_id){
        this.buyer_member_id = buyer_member_id;
    }

    /** [생성자]*/
    public Item(String title, String content, Long price, int visitCount, DelYn delYn, Member sellerMember, Category category, Town town) {
        this.title = title;
        this.content = content;
        this.price = price;
        this.visitCount = visitCount;
        this.delYn = delYn;

        if(sellerMember!=null){
            setSellerMember(sellerMember);
        }
        if(category!=null){
            setCategory(category);
        }
        if(town!=null){
            setTown(town);
        }



    }
}
