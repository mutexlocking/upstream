package com.daangn.clone.memberchatting.repository;

import com.daangn.clone.common.enums.Role;
import com.daangn.clone.member.Member;
import com.daangn.clone.memberchatting.MemberChatting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberChattingRepository extends JpaRepository<MemberChatting, Long> {

    @Query("select mc from MemberChatting  mc where mc.id = :memberChattingId")
    MemberChatting findOne(@Param("memberChattingId") Long memberChatingId);

    @Query("select mc from MemberChatting mc join fetch mc.chattingRoom where mc.member.id=:memberId and mc.role=:role")
    List<MemberChatting> findAllWithChattingRoom(@Param("memberId") Long memberId, @Param("role")Role role);

    @Query("select mc from MemberChatting mc where mc.chattingRoom.id=:chattingRoomId and mc.memberId<>:memberId")
    List<MemberChatting> findAllAboutTarget(@Param("chattingRoomId") Long chattingRoomId, @Param("memberId") Long memberId);
}