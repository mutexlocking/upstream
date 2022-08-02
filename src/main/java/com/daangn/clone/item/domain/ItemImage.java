package com.daangn.clone.item.domain;

import com.daangn.clone.BasicEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "item_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemImage extends BasicEntity{

    @Id @GeneratedValue
    @Column(name = "item_image_id")
    private Long id;

    private String path;

    @JoinColumn(name = "item_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    /** [연관관계 편의 메서드]*/

    //ItemImage와 Item
    public void setItem(Item item){
        this.item = item;
        item.getItemImageList().add(this);
    }

    /** [생성자]*/
    public ItemImage(String path, Item item) {
        this.path = path;
        if(item!=null){
            setItem(item);
        }
    }
}
