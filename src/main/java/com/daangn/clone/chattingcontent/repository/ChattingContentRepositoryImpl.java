package com.daangn.clone.chattingcontent.repository;

import com.daangn.clone.chattingcontent.ChattingContent;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.EntityManager;
import java.util.List;

import static com.daangn.clone.chattingcontent.QChattingContent.chattingContent;

public class ChattingContentRepositoryImpl implements ChattingContentRepositoryCustom{

    private JPAQueryFactory queryFactory;

    public ChattingContentRepositoryImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<ChattingContent> findNotReadMessage(Long chattingRoomId, Long targetMemberId, Long lastReadContentId,
                                                    OrderSpecifier orderSpecifier, int offset, int limit) {
        return queryFactory
                .selectFrom(chattingContent)
                .where(chattingRoomIdEq(chattingRoomId), targetMemberIdEq(targetMemberId))
                .orderBy(orderSpecifier)
                .offset(offset * limit)
                .limit(limit)
                .fetch();
    }

    private Predicate chattingRoomIdEq(Long chattingRoomId){
        return chattingContent.chattingRoom.id.eq(chattingRoomId);
    }

    private Predicate targetMemberIdEq(Long targetMemberId){
        return chattingContent.targetMemberId.eq(targetMemberId);
    }

    private Predicate lastReadContentIdOver(Long lastReadContentId){
        return chattingContent.id.gt(lastReadContentId);
    }
}
