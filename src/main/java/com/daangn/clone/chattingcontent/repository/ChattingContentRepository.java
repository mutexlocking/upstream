package com.daangn.clone.chattingcontent.repository;


import com.daangn.clone.chattingcontent.ChattingContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChattingContentRepository extends JpaRepository<ChattingContent, Long> , ChattingContentRepositoryCustom{

    @Query("select cc from ChattingContent cc where cc.id = :chattingContentId")
    ChattingContent findOne(@Param("chattingContentId") Long chattingContentId);

    @Query("select cc from ChattingContent cc where cc.chattingRoom.id=:chattingRoomId and cc.createdAt > :lastCreatedAt ORDER BY cc.createdAt DESC ")
    List<ChattingContent> findNotReadContent(@Param("chattingRoomId") Long chattingRoomId,
                                             @Param("lastCreatedAt")LocalDateTime lastCreatedAt);

    @Query("select cc from ChattingContent cc where cc.chattingRoom.id=:chattingRoomId ORDER BY cc.createdAt DESC")
    List<ChattingContent> findNewContent(@Param("chattingRoomId") Long chattingRoomId);

    Boolean existsByIdAfter(Long id);

    @Query("select count(cc) from ChattingContent cc where cc.chattingRoom.id=:chattingRoomId " +
            "and cc.targetMemberId=:targetMemberId and cc.id > :lastChattingContentId")
    Integer findNewMessageCount(@Param("chattingRoomId") Long chattingRoomId, @Param("targetMemberId") Long targetMemberId,
                                @Param("lastChattingContentId") Long lastChattingContentId);






}


