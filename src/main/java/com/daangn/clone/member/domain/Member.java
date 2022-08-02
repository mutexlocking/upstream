package com.daangn.clone.member.domain;

import com.daangn.clone.BasicEntity;
import com.daangn.clone.enums.Status;
import com.daangn.clone.item.domain.Item;
import com.daangn.clone.item.domain.Wish;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // protected 기본 생성자를 추가해줌 (JPA 규약 상 의무적으로 필요함)
public class Member extends BasicEntity {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;
    private String password;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Status status;

    @JoinColumn(name = "town_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Town town;

    @OneToMany(mappedBy = "sellerMember")
    private List<Item> itemList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Wish> wishList = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<MemberChatting> memberChattingList = new ArrayList<>();


    /** [연관관계 편의 메서드] */

    // Member와 Town
    public void setTown(Town town){
        this.town = town;
        town.getMemberList().add(this);
    }

    /** [생성자]*/
    public Member(String username, String password, String nickname, Status status, Town town) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.status = status;
        if(town != null) {
            setTown(town);
        }
    }


}
