package com.daangn.clone.init;

import com.daangn.clone.chatting.domain.ChattingContent;
import com.daangn.clone.chatting.domain.ChattingRoom;
import com.daangn.clone.enums.DelYn;
import com.daangn.clone.enums.Role;
import com.daangn.clone.enums.Status;
import com.daangn.clone.item.domain.Category;
import com.daangn.clone.item.domain.Item;
import com.daangn.clone.item.domain.Wish;
import com.daangn.clone.member.domain.Member;
import com.daangn.clone.member.domain.MemberChatting;
import com.daangn.clone.member.domain.Town;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class initDb {

    private final InitService initService;


    /** 샘플데이터로 DB 초기화 -> 스프링 빈 의존관계 주입이 끝난 직후 수행되는 로직 by @PostConstruct*/
    @PostConstruct
    void init(){
        initService.doInit1();
    }

    /** 실질적으로 샘플 데이터를 DB에 넣는 Service 로직 */
    @Service
    @Transactional
    @RequiredArgsConstructor
    static class InitService{

        private final EntityManager em;

        public void doInit1(){

            /** 1. 먼저 값이 들어가 있어야 하는 Town과 Category를 초기화 */

            Town town1 = new Town("서울특별시 광진구 중곡제1동"); Town town2 = new Town("서울특별시 광진구 중곡제2동");
            Town town3 = new Town("서울특별시 광진구 중곡제3동"); Town town4 = new Town("서울특별시 광진구 중곡제4동");
            Town town5 = new Town("서울특별시 광진구 능동");
            Town town6 = new Town("서울특별시 광진구 구의제1동"); Town town7 = new Town("서울특별시 광진구 구의제2동");
            Town town8 = new Town("서울특별시 광진구 구의제3동");
            Town town9 = new Town("서울특별시 광진구 광장동");
            Town town10 = new Town("서울특별시 광진구 자양제1동"); Town town11 = new Town("서울특별시 광진구 자양제2동");
            Town town12 = new Town("서울특별시 광진구 자양제3동"); Town town13 = new Town("서울특별시 광진구 자양제4동");
            Town town14 = new Town("서울특별시 광진구 화양동"); Town town15 = new Town("서울특별시 광진구 군자동");


            Category category1 = new Category("중고차");
            Category category2 = new Category("디지털기기");
            Category category3 = new Category("생활가전");
            Category category4 = new Category("가구/인테리어");
            Category category5 = new Category("유아동");
            Category category6 = new Category("유아도서");
            Category category7 = new Category("생활/가공식품");
            Category category8 = new Category("스포츠/레저");
            Category category9 = new Category("여성잡화");
            Category category10 = new Category("여성의류");
            Category category11 = new Category("남성패션/잡화");
            Category category12 = new Category("게임/취미");
            Category category13 = new Category("뷰티/미용");
            Category category14 = new Category("반려동물용품");
            Category category15 = new Category("도서/티켓/음반");
            Category category16 = new Category("식물");
            Category category17 = new Category("기타 중고물품");
            Category category18 = new Category("삽니다");



            em.persist(town1); em.persist(town2); em.persist(town3); em.persist(town4); em.persist(town5);
            em.persist(town6); em.persist(town7); em.persist(town8); em.persist(town9); em.persist(town10);
            em.persist(town11); em.persist(town12); em.persist(town13); em.persist(town14); em.persist(town15);

            em.persist(category1); em.persist(category2); em.persist(category3); em.persist(category4); em.persist(category5);
            em.persist(category6); em.persist(category7); em.persist(category8); em.persist(category9); em.persist(category10);
            em.persist(category11); em.persist(category12); em.persist(category13); em.persist(category14); em.persist(category15);
            em.persist(category16); em.persist(category17); em.persist(category18);


            /** 2. 이후 샘플 Member와 샘플 Post 그리고 샘플 Wish 등록*/
            Member member1 = new Member("aaa", "aaaaaa", "aaa", Status.ACTIVE, town14);
            Member member2 = new Member("bbb", "bbbbbb", "bbb", Status.ACTIVE, town14);
            Member member3 = new Member("ccc", "cccccc", "ccc", Status.ACTIVE, town14);

            Member member4 = new Member("ddd", "dddddd", "ddd", Status.ACTIVE, town6);
            Member member5 = new Member("eee", "eeeeee", "eee", Status.ACTIVE, town6);
            Member member6 = new Member("fff", "ffffff", "fff", Status.ACTIVE, town6);

            Item item11 = new Item("아이폰6 팝니다", "아이폰6 팝니다", 100000L, 0, DelYn.FOR_SALE, member1, category3, member1.getTown());
            Item item12 = new Item("아이폰7 팝니다", "아이폰7 팝니다", 200000L, 0, DelYn.FOR_SALE, member1, category3, member1.getTown());
            Item item13 = new Item("아이폰8 팝니다", "아이폰8 팝니다", 300000L, 0, DelYn.FOR_SALE, member1, category3, member1.getTown());

            Item item21 = new Item("아이폰11 팝니다", "아이폰11 팝니다", 400000L, 0, DelYn.FOR_SALE, member2, category3, member2.getTown());
            Item item22 = new Item("아이폰12 팝니다", "아이폰12 팝니다", 500000L, 0, DelYn.FOR_SALE, member2, category3, member2.getTown());
            Item item23 = new Item("아이폰13 팝니다", "아이폰13 팝니다", 600000L, 0, DelYn.FOR_SALE, member2, category3, member2.getTown());

            Item item31 = new Item("쇼파 팝니다", "쇼파 팝니다", 700000L, 0, DelYn.FOR_SALE, member3, category5, member3.getTown());
            Item item32 = new Item("침대 팝니다", "침대 팝니다", 800000L, 0, DelYn.FOR_SALE, member3, category5, member3.getTown());
            Item item33 = new Item("이불 팝니다", "이불 팝니다", 900000L, 0, DelYn.FOR_SALE, member3, category5, member3.getTown());

            Item item41 = new Item("아이패드5 팝니다", "아이패드5 팝니다", 100000L, 0, DelYn.FOR_SALE, member4, category3, member4.getTown());
            Item item42 = new Item("아이패드6 팝니다", "아이패드6 팝니다", 200000L, 0, DelYn.FOR_SALE, member4, category3, member4.getTown());
            Item item43 = new Item("아이패드7 팝니다", "아이패드7 팝니다", 300000L, 0, DelYn.FOR_SALE, member4, category3, member4.getTown());

            Item item51 = new Item("아이패드8 팝니다", "아이패드8 팝니다", 400000L, 0, DelYn.FOR_SALE, member5, category3, member5.getTown());
            Item item52 = new Item("아이패드9 팝니다", "아이패드9 팝니다", 500000L, 0, DelYn.FOR_SALE, member5, category3, member5.getTown());
            Item item53 = new Item("아이패드10 팝니다", "아이패드10 팝니다", 600000L, 0, DelYn.FOR_SALE, member5, category3, member5.getTown());

            Item item61 = new Item("밥솥 팝니다", "밥솥 팝니다", 700000L, 0, DelYn.FOR_SALE, member6, category4, member6.getTown());
            Item item62 = new Item("에어컨 팝니다", "에어컨 팝니다", 800000L, 0, DelYn.FOR_SALE, member6, category4, member6.getTown());
            Item item63 = new Item("냉장고 팝니다", "냉장고 팝니다", 900000L, 0, DelYn.FOR_SALE, member6, category4, member6.getTown());

            Wish wish1 = new Wish(Status.ACTIVE, member1, item23);
            Wish wish2 = new Wish(Status.ACTIVE, member3, item23);

            em.persist(member1); em.persist(member2); em.persist(member3);
            em.persist(member4); em.persist(member5); em.persist(member6);

            em.persist(item11); em.persist(item12); em.persist(item13);
            em.persist(item21); em.persist(item22); em.persist(item23);
            em.persist(item31); em.persist(item32); em.persist(item33);

            em.persist(item41); em.persist(item42); em.persist(item43);
            em.persist(item51); em.persist(item52); em.persist(item53);
            em.persist(item61); em.persist(item62); em.persist(item63);

            em.persist(wish1); em.persist(wish2);

            /** 3. item23에 대해서 member1과 member3아 member2에게 채팅을 시도했다고 가정 */

            ChattingRoom chattingRoom12 = new ChattingRoom(Status.ACTIVE, item23);
            ChattingRoom chattingRoom13 = new ChattingRoom(Status.ACTIVE, item23);

            ChattingContent chattingContent12 = new ChattingContent("안녕하세요 ~ member1인데 아이폰 13 구매하고 싶어서요~", chattingRoom12);
            ChattingContent chattingContent13 = new ChattingContent("안녕하세요 ~ member3인데 아이폰13 구매하고 싶어서요~", chattingRoom13);

            MemberChatting memberChatting12 = new MemberChatting(Role.EXPECTED_BUYER, member1, chattingRoom12);
            MemberChatting memberChatting13 = new MemberChatting(Role.EXPECTED_BUYER, member3, chattingRoom13);
            MemberChatting memberChatting21 = new MemberChatting(Role.SELLER, member2, chattingRoom12);
            MemberChatting memberChatting23 = new MemberChatting(Role.SELLER, member2, chattingRoom13);


            em.persist(chattingRoom12); em.persist(chattingRoom13);
            em.persist(chattingContent12); em.persist(chattingContent13);
            em.persist(memberChatting12); em.persist(memberChatting13); em.persist(memberChatting21); em.persist(memberChatting23);


        }
    }


}
