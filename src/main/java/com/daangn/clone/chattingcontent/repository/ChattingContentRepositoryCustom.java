package com.daangn.clone.chattingcontent.repository;

import com.daangn.clone.chattingcontent.ChattingContent;
import com.querydsl.core.types.OrderSpecifier;

import java.util.List;

public interface ChattingContentRepositoryCustom {

    List<ChattingContent> findNotReadMessage(Long chattingRoomId, Long targetMemberId, Long lastReadContentId,
                                             OrderSpecifier orderSpecifier, int offset, int limit);
}
