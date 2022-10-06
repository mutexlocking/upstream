package com.daangn.clone.item.repository;

import com.daangn.clone.category.Category;
import com.daangn.clone.common.enums.DelYn;
import com.daangn.clone.common.enums.SaleSituation;
import com.daangn.clone.item.Item;
import com.daangn.clone.member.Member;
import com.daangn.clone.member.repository.MemberRepository;
import com.daangn.clone.town.Town;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class ItemRepositoryTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MemberRepository memberRepository;
    @Test
    public void test(){

        //given


        //when
        Member member1 = memberRepository.findById(34L).get();

        //then
        System.out.println(member1.getTown().getName());
        System.out.println(member1.getTown().getId());


    }

}