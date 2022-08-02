package com.daangn.clone.item.domain;

import com.daangn.clone.BasicEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "category_id")
    private Long id;

    private String name;

    @OneToMany(mappedBy = "category")
    private List<Item> itemList = new ArrayList<>();

    /** [생성자]*/
    public Category(String name) {
        this.name = name;
    }
}
