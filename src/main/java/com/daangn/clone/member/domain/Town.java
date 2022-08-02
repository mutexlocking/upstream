package com.daangn.clone.member.domain;

import com.daangn.clone.BasicEntity;
import com.daangn.clone.item.domain.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "town")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Town extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "town_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "town")
    private List<Member> memberList = new ArrayList<>();

    @OneToMany(mappedBy = "town")
    private List<Item> itemList = new ArrayList<>();

    /** [생성자]*/
    public Town(String name) {
        this.name = name;
    }
}
