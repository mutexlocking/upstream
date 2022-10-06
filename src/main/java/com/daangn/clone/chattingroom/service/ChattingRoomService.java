package com.daangn.clone.chattingroom.service;

import com.daangn.clone.chattingcontent.ChattingContent;
import com.daangn.clone.chattingcontent.repository.ChattingContentRepository;

import com.daangn.clone.chattingroom.ChattingRoom;
import com.daangn.clone.chattingroom.dto.ChattingDto;
import com.daangn.clone.chattingroom.dto.ChattingListDto;
import com.daangn.clone.chattingroom.dto.EnterRoomDto;
import com.daangn.clone.chattingroom.repository.ChattingRoomRepository;
import com.daangn.clone.common.enums.InRoomYn;
import com.daangn.clone.common.enums.Status;
import com.daangn.clone.common.response.ApiException;
import com.daangn.clone.common.response.ApiResponseStatus;
import com.daangn.clone.file.FileServiceUtil;
import com.daangn.clone.item.Item;
import com.daangn.clone.item.repository.ItemRepository;
import com.daangn.clone.member.Member;
import com.daangn.clone.member.repository.MemberRepository;
import com.daangn.clone.memberchatting.MemberChatting;
import com.daangn.clone.memberchatting.repository.MemberChattingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.daangn.clone.common.enums.DelYn.Y;
import static com.daangn.clone.common.enums.Role.*;
import static com.daangn.clone.common.enums.SaleSituation.RESERVED;
import static com.daangn.clone.common.enums.SaleSituation.SOLD_OUT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChattingRoomService {

    private final ItemRepository itemRepository;
    private final ChattingRoomRepository chattingRoomRepository;
    private final ChattingContentRepository chattingContentRepository;
    private final MemberChattingRepository memberChattingRepository;
    private final MemberRepository memberRepository;

    private void checkSetChatting(String username, Long itemId){

        //1. 일단 저 itemId의 Item이 유효한 Item인지 판별 (삭제되지 않았고 && 판매완료된 상품이 아니어야 함)
        Item item = itemRepository.findItem(itemId).orElseThrow(
                () -> {
                    throw new ApiException(ApiResponseStatus.FAIL_CREATE_CHATTING_ROOM, "채팅을 하고자 하는 판매자의 상품이 존재하지 않습니다.");
                }
        );

        if(item.getDelYn()==Y || item.getSalesituation()==SOLD_OUT || item.getSalesituation()==RESERVED){
            throw new ApiException(ApiResponseStatus.FAIL_CREATE_CHATTING_ROOM, "채팅을 하고자 하는 판매자의 상품이 삭제되었거나 or 이미 예약되었거나 or 판매된 상품입니다.");
        }


        //2. 그 뒤 혹시 모를 상황을 대비해 , 채팅을 요청하는 쪽과 - 판매자가 같은 Member인지를 판벌햐는 로직을 수행함 (이걸 검사하는게 맞는지 질문)
        if(item.getSellerMember().getUsername().equals(username)){
            throw new ApiException(ApiResponseStatus.FAIL_CREATE_CHATTING_ROOM, "채팅하고자 하는 판매자와, 구매하려는 사용자가 같을 수는 없습니다.");
        }

    }

    /** 이 비지니스 로직은, 무조건 Buyer 만의 입장에서 -> 상품을 보고 구매자가 판매자에게 채팅을 요청하면 -> 그 요청한 채팅을 설정해주는 서비스
     * (모든 채팅은 어쨌든 , Buyer가 Seller에게 채팅을 요청하므로 써 시작된다 -> so 이 비지니스 로직에서 채팅에 관한 첫 설정을 모두 마쳐야 한다) */
    @Transactional
    public ChattingDto  setChatting(String username, Long itemId){

        //0. 유효성 검사
        checkSetChatting(username, itemId);
        Item item = itemRepository.findOne(itemId);
        Member buyerMember = memberRepository.findByUsername(username);

        //1. 먼저 그 Item에 대해 , 해당 username의 buyerMember가 이미 채팅을 진행했었다면
        // -> ChattingRoom , MemberChatting, ChattingContent 엔티티들이 이미 생성되어 있었을 것.
        // -> 따라서 이를 기준으로
        // (1) 기존에 채팅이 진행중이었다면 별도의 엔티티 생성 없이 바로 ChattingDto를 생성해서 반환하고
        // (2) 처음으로 이 구매자가 채팅 시도를 하는것 이라면 , 위 엔티티들을 생성한 후 , ChattingDto를 생성해서 반환해야 한다.

        //2.기존에 채팅을 시도하여 , 그에 따른 ChattingRoom , MemberChatting 엔티티들이 생성되어 있는지 판단

        /** (1) 그 전에 해당 아이템에 대해 먼저 어떤 채팅도 시도되지 않았을 수 있는 경우를 check 하고 */
        if(CollectionUtils.isEmpty(item.getChattingRoomList())){
            return setChattingFirst(itemId, item, buyerMember);
        }

        //만약 그 전에 해당 아이템에 대해 채팅이 시도되었다면 -> 시도된 채팅중에 , 요청한 사용자도 채팅을 시도하였는지 check
        List<MemberChatting> memberChattingList =
                memberChattingRepository.findAllWithChattingRoom(buyerMember.getId(), EXPECTED_BUYER);
        boolean isPastChat = memberChattingList.stream()
                .map(mc -> mc.getChattingRoom())
                .anyMatch(c -> c.getItem().getId() == itemId);

        /** (2) 어쨌든 이 사용자는 기존에 채팅을 진행하지 않았다면 -> 처음 채팅환경을 setting*/
        if(isPastChat == false){
            return setChattingFirst(itemId, item, buyerMember);
        }

        /** (3) 만약 이 사용자가 그전에 해당 상품에 관해 채팅을 시도한 기록이 있다면 -> 관련 엔티티를 추출하여 DTO로 변환하여 반환 */
        ChattingRoom chattingRoom = memberChattingList.stream()
                .map(mc -> mc.getChattingRoom())
                .filter(c -> c.getItem().getId() == itemId)
                .collect(Collectors.toList())
                .get(0);

        /** (4) 이때 시도한 기록이 있다는건 -> 그 BuyerMember의 MemberChatting도 만들어졌다는 의미 이므로
         * -> ChattingRoom으로 부터 , 그 BuyerMember와 연관된 MemberChatting을 추출한다 */
        MemberChatting memberChatting = memberChattingList.stream()
                .filter(mc -> mc.getChattingRoom().getId() == chattingRoom.getId())
                .collect(Collectors.toList())
                .get(0);

        /** (5) 이후 응답을 반환하기 전 , SELLER의 InRoomYn은 N으로 && BUYER의 InRoomYn은 Y로 업데이트 해줘야 한다*/
        chattingRoom.getMemberChattingList().stream()
                .filter(mc -> mc.getMember().getId()==buyerMember.getId())
                .forEach(mc -> mc.updateInRoomYn(InRoomYn.Y));

        chattingRoom.getMemberChattingList().stream()
                .filter(mc -> mc.getMember().getId()!=buyerMember.getId())
                .forEach(mc -> mc.updateInRoomYn(InRoomYn.N));


        //. 이후 ChattingDto를 생성하여 반환
        return ChattingDto.builder()
                .myMemberId(buyerMember.getId())
                .targetMemberId(item.getSellerMember().getId())
                .itemId(itemId)
                .memberChattingId(memberChatting.getId()) //BUYER의 MemberChatting이란 점 주의!ㅠ
                .chattingRoomId(chattingRoom.getId())
                .build();

    }

    private ChattingDto setChattingFirst(Long itemId, Item item, Member buyerMember){
        /** File작업과 DB작업을 한 메소드 안에서 다루는 경우 , DB작업을 모두 마친 후 File 작업을 수행해야 함  */

        //2_1. ChattingRoom 생성
        ChattingRoom chattingRoom = ChattingRoom.builder()
                                                .status(Status.ACTIVE)
                                                .itemId(item.getId())
                                                .build();
        chattingRoomRepository.save(chattingRoom);

        //2_2. ChattingContent 생성은 하지 않음 -> 왜냐하면 ChattingContent는 사실상 각각의 메세지 단위의 row 이므로 ,
        // 미리 생성한다고 별 의미가 없음

        //2_3. MemberChatting 생성 - 이때 요청한 Member , 즉 Buyer와 / 상품 판매자인 Seller 모두에 대해서 생성해줘야 함
        MemberChatting memberChattingAtBuyer = MemberChatting.builder()
                .role(EXPECTED_BUYER)
                .inRoomYn(InRoomYn.Y) /** 구매자 입장에선 바로 채팅방으로 들어가서 채팅을 보내려는 시도이니 , Y로 해줘야 할 것 같음*/
                .memberId(buyerMember.getId())
                .chattingRoomId(chattingRoom.getId())
                .build();

        MemberChatting memberChattingAtSeller = MemberChatting.builder()
                .role(SELLER)
                .inRoomYn(InRoomYn.N) /** SELLER 입장에서는 들어와야 Y로 바뀌는 것이므로 , default 는 N */
                .memberId(item.getSellerMember().getId())
                .chattingRoomId(chattingRoom.getId())
                .build();

        memberChattingRepository.save(memberChattingAtBuyer);
        memberChattingRepository.save(memberChattingAtSeller);

        return ChattingDto.builder()
                .myMemberId(buyerMember.getId())
                .targetMemberId(item.getSellerMember().getId())
                .itemId(itemId)
                .memberChattingId(memberChattingAtBuyer.getId()) /** 요청한 Member에 대응되는 MemberChattingId를 넘겨야 함 */
                .chattingRoomId(chattingRoom.getId())
                .build();
    }




    /** 채팅 탭에서 , 이 사용자가 { BUYER로써 + SELLER로써 } 과거에 연결되었던 유효한 모든 채팅 정보를 조회하여 반환한다 */
    public ChattingListDto getChattingList(String username){

        //0. 만약 해당 username의 Member가 아직 한번도 채팅을 시도하지 않았다면 (SELLER 로든 BUYER로든) 빈 응답을 반환해야 함
        if(CollectionUtils.isEmpty(memberRepository.findByUsername(username).getMemberChattingList())){
            return ChattingListDto.builder().sizeOfChatting(0).build();
        }

        //1. 해당 Member조회 + 연관된 MemberChattingList 및 ChattingRoom들을 함께 페치조인으로 가져옴
        /** 이제 더이상의 조회 없이 , 이 한번 조회한 데이터를 가지고 같은 순서대로 접근하기 때문에 , 나중에 element들을 대응시킬 때도 순서대로 대응된다.*/
        Member member = memberRepository.findMemberWithChatting(username);

        //2. 이후 그 Member와 관련된 MemberChattingList를 통해 일차적으로 ChattingDto 정보를 추출 (targetMemberId 정보만 제외)
        List<ChattingDto> chattingDtoList = setFirstChattingDtoList(member);

        //3. 이후 이차적으로 각 ChattingRoom에서의 targetMemberId를 추출하여 DTO에 담기
        List<Long> targetMemberIdList = getTargetMemberIdList(member);

        //4. 이후 그 targetMemberId값을 , 각각의 ChattingDto에 넣어주면 끝 (비로소 targetMemberId 정보를 넣어줌)
        setSecondChattingDtoList(chattingDtoList, targetMemberIdList);

        //5. 해당 List<ChattingDto> 정보를 ChattingListDto로 감싸서 반환
        return ChattingListDto.builder()
                .sizeOfChatting(chattingDtoList.size())
                .chattingDtoList(chattingDtoList)
                .build();


    }

    private List<ChattingDto> setFirstChattingDtoList(Member member){
        return member.getMemberChattingList().stream()
                .map(mc -> ChattingDto.builder()
                        .myMemberId(member.getId())
                        .itemId(mc.getChattingRoom().getItem().getId())
                        .memberChattingId(mc.getId())
                        .chattingRoomId(mc.getChattingRoom().getId())
                        .build())
                .collect(Collectors.toList());
    }

    private List<Long> getTargetMemberIdList(Member member){
        return member.getMemberChattingList().stream()
                .map(mc -> mc.getChattingRoom())
                .map(c -> c.getMemberChattingList().get(0).getMember().getId() != member.getId()
                        ? c.getMemberChattingList().get(0).getMember().getId()
                        : c.getMemberChattingList().get(1).getMember().getId())
                .collect(Collectors.toList());
    }

    private void setSecondChattingDtoList(List<ChattingDto> chattingDtoList, List<Long> targetMemberIdList){


        //여기서 for문을 안쓰고 싶었지만 .. 어쩔 수 없었따..
        List<Integer> list = new ArrayList<>();
        for(int i=0; i<targetMemberIdList.size(); i++){
            list.add(i);
        }

        /** ChattingDtoList를 만들 떄와 , TargetMemberIdList를 만들 떄 모두, member.getMemberChattingList() 의 대응되는 ChattingRoom
         *  에서 부터 시작했으므로 -> 결과적으로 대응되는 element의 순서가 같게 됨  (이로써 같은 idx에 대응되도록 setting 하는게 가능!)*/
        list.stream()
                .forEach(i -> chattingDtoList.get(i).setTargetMemberId(targetMemberIdList.get(i)));

    }

    private void checkUpdateInRoomYn(String username, Long chattingRoomId){
        Member member = memberRepository.findByUsername(username);

        //1. 일단 그 Member와 연관된 ChattingRoom이 하나라도 있는가? == 사실상 연관된 MemberChatting이 하나라도 있는가?
        if(CollectionUtils.isEmpty(member.getMemberChattingList())){
            throw new ApiException(ApiResponseStatus.FAIL_ENTER_CHATTING_ROOM, "입장하고자 하는 그 채팅방은 존재하지 않습니다.");
        }

        //2. 그 Member와 연관된 MemberChatting들이 존재하여도,
        // 실제로 그중 Id 값이 chattingRoomId값인 연관된 ChattingRoom 이 존재하는가?
        boolean isExistTheChatiingRoom = member.getMemberChattingList().stream()
                .map(mc -> mc.getChattingRoom())
                .anyMatch(c -> c.getId() == chattingRoomId);

        if(isExistTheChatiingRoom==false){
            throw new ApiException(ApiResponseStatus.FAIL_ENTER_CHATTING_ROOM, "입장하고자 하는 그 채팅방은 존재하지 않습니다.");
        }

    }


    @Transactional
    /** 채팅목록에 존재하는 특정 채팅방에 입장하여 , MemberChatting의 InRoomYn 정보가 업데이트 되는 서비스 */
    public EnterRoomDto EnterOrExitTheChattingRooom(String username, Long chattingRoomId, InRoomYn yn){

        //0. memberChattingId의 유효성 검사 -> 여기서 걸리지 않으면 , 확실히 입장하고자 하는 채팅방이 존재하는 것!
        checkUpdateInRoomYn(username, chattingRoomId);

        //1. 입장한 그 채팅방에 대응되는 MemberChatting 하나를 식별하여 -> InRoomYn 값을 인자로 받은 값으로 update 후 ,
        Member member = memberRepository.findMemberWithChatting(username);
        MemberChatting memberChatting = member.getMemberChattingList().stream()
                .filter(mc -> mc.getChattingRoom().getId() == chattingRoomId)
                .collect(Collectors.toList())
                .get(0);

        updateInRoomYn(memberChatting.getId(), yn);

        // 2. 그 정보에 따른 EnterRoomDto를 생성하여 반환
        return EnterRoomDto.builder()
                .memberId(member.getId())
                .memberChattingRoomId(memberChatting.getId())
                .inRoomYn(yn)
                .build();

    }

    // dirty checking 에 기반한 update
    public void updateInRoomYn(Long memberChattingId, InRoomYn yn){
        MemberChatting memberChatting = memberChattingRepository.findOne(memberChattingId);
        memberChatting.updateInRoomYn(yn);
    }
}
