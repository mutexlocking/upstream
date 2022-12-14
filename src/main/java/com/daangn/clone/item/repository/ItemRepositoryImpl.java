package com.daangn.clone.item.repository;

import com.daangn.clone.common.enums.DelYn;
import com.daangn.clone.common.enums.ItemStatus;
import com.daangn.clone.item.Item;
import com.daangn.clone.item.dto.paging.ItemSummaryDto;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.hibernate.procedure.spi.ParameterRegistrationImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import java.util.List;

import static com.daangn.clone.item.QItem.item;
import static com.daangn.clone.member.QMember.member;
import static com.daangn.clone.town.QTown.town;

public class ItemRepositoryImpl implements ItemRepositoryCustom{

    private JPAQueryFactory queryFactory;

    public ItemRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<ItemSummaryDto> searchItemSummaryDtos(Long townId, Long categoryIdCond, ItemStatus situationCond, OrderSpecifier specifier, int offset, int limit) {
        return queryFactory
                .select(Projections.bean(ItemSummaryDto.class,
                        item.id,
                        item.title,
                        item.town.name.as("townName"),
                        item.createdAt,
                        item.price,
                        item.itemImageList.get(0).path.as("itemImageUrl"),
                        item.wishList.size().as("numOfWish"),
                        item.chattingRoomList.size().as("numOfChattingRoomList")))
                .from(item)
                .innerJoin(item.town, town)
                .where(delYnEq(DelYn.N), townIdEq(townId), categoryIdEq(categoryIdCond), itemStatusEq(situationCond))
                .orderBy(specifier)
                .offset(offset*limit)
                .limit(limit)
                .fetch();
    }

    @Override
    public List<Item> searchItems(Long townId, Long categoryIdCond, ItemStatus itemStatusCond,
                                          OrderSpecifier specifier, int offset, int limit) {
        return queryFactory
                .selectFrom(item)
                .innerJoin(item.town, town)
                .where(delYnEq(DelYn.N), townIdEq(townId), categoryIdEq(categoryIdCond), itemStatusEq(itemStatusCond))
                .orderBy(specifier)
                .offset(offset*limit) //??????
                .limit(limit)
                .fetch();
    }

    /** ????????? ???????????? ?????? ??????????????? ???????????? ??? */
    //????????????
    private Predicate delYnEq(DelYn delYn){
        return item.delYn.eq(delYn);
    }

    private Predicate townIdEq(Long townId){ return item.town.id.eq(townId);}

    //????????????
    private Predicate categoryIdEq(Long categoryIdCond) {
        return categoryIdCond!=null ? item.category.id.eq(categoryIdCond) : null;
    }

    //????????????
    private Predicate itemStatusEq(ItemStatus itemStatusCond) {
        return itemStatusCond!=null ? item.itemStatus.eq(itemStatusCond) : null;
    }
}
