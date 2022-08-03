//package com.daangn.clone.item.domain;
//
//import com.daangn.clone.enums.Status;
//import com.daangn.clone.member.domain.Member;
//import com.daangn.clone.member.domain.Town;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//
//import static com.daangn.clone.enums.DelYn.FOR_SALE;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Transactional
//@Rollback(value = false)
//class ItemTest {
//
//    @PersistenceContext
//    private EntityManager em;
//
//    @Test
//    public void itemTest(){
//        //given
//        Town town1 = new Town("서울특별시 광진구 화양동");
//        Category category1 = new Category("전자기기");
//
//        Member memberA = new Member("memberA", "memberAA", "memberA", Status.ACTIVE, town1);
//        Member memberB = new Member("memberB", "memberBB", "memberB", Status.ACTIVE, town1);
//        Item iphone11 = new Item("아이폰11 팝니다", "아이폰11 팝니다", 100000L, 0, FOR_SALE, memberA,category1, town1);
//
//        em.persist(town1); em.persist(category1); em.persist(memberA); em.persist(memberB); em.persist(iphone11);
//
//        //wneh
//        //iphone11.setBuyerMember(memberB);
//        iphone11.setBuyerMember(memberB);
////
////        //then
//        assertThat(iphone11.getBuyerMember()).isEqualTo(memberB);
//        assertThat(memberB.getBuyedItemList().get(0)).isEqualTo(iphone11);
//
//    }
//
//
//
//}