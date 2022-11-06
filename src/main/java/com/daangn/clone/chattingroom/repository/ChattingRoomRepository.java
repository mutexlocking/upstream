package com.daangn.clone.chattingroom.repository;

import com.daangn.clone.chattingroom.ChattingRoom;
import com.daangn.clone.common.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChattingRoomRepository extends JpaRepository<ChattingRoom, Long> {

    @Query("select distinct cr from ChattingRoom cr join fetch cr.chattingMemberList mc join fetch mc.member where cr.id=:id")
    ChattingRoom findOneWithMember(@Param("id")Long id);

    @Query("select cr.id from ChattingRoom cr where cr.itemId = :itemId")
    List<Long> findId(@Param("itemId")Long itemId);

    boolean existsByIdAndStatus(Long id, Status status);
}
