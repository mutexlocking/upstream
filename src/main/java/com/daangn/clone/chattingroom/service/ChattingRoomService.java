package com.daangn.clone.chattingroom.service;

import com.daangn.clone.chattingcontent.ChattingContent;
import com.daangn.clone.chattingcontent.repository.ChattingContentRepository;

import com.daangn.clone.chattingroom.ChattingRoom;
import com.daangn.clone.chattingroom.dto.*;
import com.daangn.clone.chattingroom.dto.polling.ChatDto;
import com.daangn.clone.chattingroom.dto.polling.ContentDto;
import com.daangn.clone.chattingroom.dto.polling.LastChatDto;
import com.daangn.clone.chattingroom.dto.polling.NewContentDto;
import com.daangn.clone.chattingroom.dto.unread.ChattingRoomMeta;
import com.daangn.clone.chattingroom.dto.unread.NewChattingRoomDto;
import com.daangn.clone.chattingroom.repository.ChattingRoomRepository;
import com.daangn.clone.common.enums.Status;
import com.daangn.clone.common.response.ApiException;
import com.daangn.clone.common.response.ApiResponseStatus;
import com.daangn.clone.item.Item;
import com.daangn.clone.item.repository.ItemRepository;
import com.daangn.clone.member.Member;
import com.daangn.clone.member.repository.MemberRepository;
import com.daangn.clone.chattingmember.ChattingMember;
import com.daangn.clone.chattingmember.repository.ChattingMemberRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.daangn.clone.chattingcontent.QChattingContent.chattingContent;
import static com.daangn.clone.common.enums.DelYn.Y;
import static com.daangn.clone.common.enums.ItemStatus.RESERVED;
import static com.daangn.clone.common.enums.ItemStatus.SOLD_OUT;
import static com.daangn.clone.common.enums.Role.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChattingRoomService {

    private final ItemRepository itemRepository;
    private final ChattingRoomRepository chattingRoomRepository;
    private final ChattingContentRepository chattingContentRepository;
    private final ChattingMemberRepository chattingMemberRepository;
    private final MemberRepository memberRepository;

    private Item getItem(Long itemId){
        return itemRepository.findItem(itemId).orElseThrow(
                () -> {
                    throw new ApiException(ApiResponseStatus.FAIL_CREATE_CHATTING_ROOM, "채팅을 하고자 하는 판매자의 상품이 존재하지 않습니다.");
                }
        );
    }

    private void checkAvaliableChatting(Item item){
        /** 함수로 추출 -> 1. 채팅을 못하는 상황을 따로 함수로 추출 for 가독성 */
        if(item.getDelYn()==Y || item.getItemStatus()==SOLD_OUT || item.getItemStatus()==RESERVED){
            throw new ApiException(ApiResponseStatus.FAIL_CREATE_CHATTING_ROOM, "채팅을 하고자 하는 판매자의 상품이 삭제되었거나 or 이미 예약되었거나 or 판매된 상품입니다.");
        }
    }

    private void checkSameMember(String username, Item item){

        //2. 그 뒤 혹시 모를 상황을 대비해 , 채팅을 요청하는 쪽과 - 판매자가 같은 Member인지를 판벌햐는 로직을 수행함 (이걸 검사하는게 맞는지 질문)
        if(item.getSellerMember().getUsername().equals(username)){
            throw new ApiException(ApiResponseStatus.FAIL_CREATE_CHATTING_ROOM, "채팅하고자 하는 판매자와, 구매하려는 사용자가 같을 수는 없습니다.");
        }
    }

    /** 유효성 검사 메소드 */
    private void checkBindChatting(String username, Item item){

        //1. 이 상품이 채팅을 보낼 수 있는 상태인지 확인하고
        checkAvaliableChatting(item);

        //2. 나아가 채팅을 보내는 구매자와 , 상품을 올린 판매자가 서로 같은 사람은 아닌지 check
        // (왜냐하면 자기가 자기한테 채팅을 보내는건 막아야 하니깐)
        checkSameMember(username, item);
    }

    private ChattingMember getBuyer(List<Long> chattingRoomIdList, Long buyerMemberId){
        return chattingMemberRepository.findChattingMemberAtRole(
                chattingRoomIdList.stream()
                        .filter(cri -> chattingMemberRepository.existsByChattingRoomIdAndMemberIdAndRole(cri, buyerMemberId, EXPECTED_BUYER))
                        .findFirst().get(), buyerMemberId, EXPECTED_BUYER);
    }

    private ChattingMember getSeller(List<Long> chattingRoomIdList, Long sellerMemberId){
        return chattingMemberRepository.findChattingMemberAtRole(
                chattingRoomIdList.stream()
                        .filter(cri -> chattingMemberRepository.existsByChattingRoomIdAndMemberIdAndRole(cri, sellerMemberId, SELLER))
                        .findFirst().get(), sellerMemberId, SELLER);
    }

    private ChattingDto createChattingDto(Member buyerMember, Item item, Long itemId, ChattingRoom chattingRoom, ChattingMember buyer, ChattingMember seller){

        return ChattingDto.builder()
                .memberId(buyerMember.getId())
                .targetMemberId(item.getSellerMember().getId())
                .itemId(itemId)
                .chattingRoomId(chattingRoom.getId())
                .chattingMemberId(buyer.getId())
                .targetChattingMemberId(seller.getId())
                .build();
    }

    private ChattingDto createChatting(Long itemId, Item item, Member buyerMember){

        //2_1. ChattingRoom 생성
        ChattingRoom chattingRoom = ChattingRoom.builder()
                .status(Status.ACTIVE)
                .itemId(item.getId())
                .build();
        chattingRoomRepository.save(chattingRoom);

        //2_2. ChattingContent 생성은 하지 않음 -> 왜냐하면 ChattingContent는 사실상 각각의 메세지 단위의 row 이므로 ,
        // 미리 생성한다고 별 의미가 없음

        //2_3. ChattingMember 생성 - 이때 요청한 Member , 즉 Buyer와 / 상품 판매자인 Seller 모두에 대해서 생성해줘야 함
        ChattingMember buyer = ChattingMember.builder()
                .role(EXPECTED_BUYER)
                .memberId(buyerMember.getId())
                .chattingRoomId(chattingRoom.getId())
                .build();

        ChattingMember seller = ChattingMember.builder()
                .role(SELLER)
                .memberId(item.getSellerMember().getId())
                .chattingRoomId(chattingRoom.getId())
                .build();

        chattingMemberRepository.save(buyer);
        chattingMemberRepository.save(seller);

        return createChattingDto(buyerMember, item, itemId, chattingRoom, buyer, seller);
    }

    /** 이 비지니스 로직은, 무조건 Buyer 만의 입장에서 -> 상품을 보고 구매자가 판매자에게 채팅을 요청하면 -> 그 요청한 채팅을 설정해주는 서비스
     * (모든 채팅은 어쨌든 , Buyer가 Seller에게 채팅을 요청하므로 써 시작된다 -> so 이 비지니스 로직에서 채팅에 관한 첫 설정을 모두 마쳐야 한다) */
    @Transactional
    public ChattingDto bindChatting(String username, Long itemId){

        //0. 유효성 검사 -> 유효한 값이라는건 , 이미 존재하는 값을 대상으로 유효성을 검사하는것이 논리적으로 맞으므로
        // 일단 존재하는 Item을 조회한 후 -> 그 Item을 넘겨 , 그 Item의 유효성을 검사해야 한다.
        Item item = getItem(itemId);
        checkBindChatting(username, item);

        Member buyerMember = memberRepository.findByUsername(username);

        /**
         * (1) 해당 item에 대해 , 해당 username의 EXPECTED BUYER가 한번도 채팅을 요청하지 않았다면
         * -> 채팅과 관련된 엔티티들을 생성해서 save 해줘야 하고
         *
         * (2) 해당 item에 대해, 해당 username의 EXPECTED BUYER가 이미 채팅을 요청했따면
         * -> 그 채팅과 관련된 엔티티들을 조회해여 DTO로 변환해서 응답으로 리턴해야 한다.
         * */
        List<Long> chattingRoomIdList = chattingRoomRepository.findId(itemId);
        boolean isExistChattingRoom =
                CollectionUtils.isEmpty(chattingRoomIdList) ? false
                : chattingRoomIdList.stream()
                .anyMatch(cri -> chattingMemberRepository.existsByChattingRoomIdAndMemberIdAndRole(cri, buyerMember.getId(), EXPECTED_BUYER));


        /** (1) 채팅을 요청하지 않은 경우 (해당 아이템에 대해 채팅방이 처음 생성되거나 or 그 EXPECTED_BUYER에 대한 채팅방만 처음이거나) */
        if(!isExistChattingRoom){
            return createChatting(itemId, item, buyerMember);
        }

        /** (2) 기존에 채팅을 요청했던 경우 */
        ChattingMember buyer = getBuyer(chattingRoomIdList, buyerMember.getId());
        ChattingMember seller = getSeller(chattingRoomIdList, item.getSellerMember().getId());
        ChattingRoom chattingRoom = buyer.getChattingRoom();
        return createChattingDto(buyerMember, item, itemId, chattingRoom, buyer, seller);


    }




    /** 채팅 탭에서 , 이 사용자가 { BUYER로써 + SELLER로써 } 과거에 연결되었던 유효한 모든 채팅 정보를 조회하여 반환한다 */
    public ChattingListDto getChattingList(String username){

        //0. 만약 해당 username의 Member가 아직 한번도 채팅을 시도하지 않았다면 (SELLER 로든 BUYER로든) 빈 응답을 반환해야 함
        if(CollectionUtils.isEmpty(memberRepository.findByUsername(username).getChattingMemberList())){
            return ChattingListDto.builder().sizeOfChatting(0).build();
        }

        //1. 해당 Member조회 + 연관된 MemberChattingList 및 ChattingRoom들을 함께 페치조인으로 가져옴
        /** 이제 더이상의 조회 없이 , 이 한번 조회한 데이터를 가지고 같은 순서대로 접근하기 때문에 , 나중에 element들을 대응시킬 때도 순서대로 대응된다.*/
        Member member = memberRepository.findMemberWithChatting(username);

        //2. 이후 그 Member와 관련된 MemberChattingList를 통해 일차적으로 ChattingDto 정보를 추출 (targetMemberId 정보만 제외)
        List<ChattingDto> chattingDtoList = createChattingDtoList(member);

        //3. 해당 List<ChattingDto> 정보를 ChattingListDto로 감싸서 반환
        return ChattingListDto.builder()
                .sizeOfChatting(chattingDtoList.size())
                .chattingDtoList(chattingDtoList)
                .build();


    }

    private List<ChattingDto> createChattingDtoList(Member member){
        List<ChattingDto> chattingDtoList = member.getChattingMemberList().stream()
                .map(mc -> ChattingDto.builder()
                        .memberId(member.getId())
                        .itemId(mc.getChattingRoom().getItem().getId())
                        .chattingMemberId(mc.getId())
                        .chattingRoomId(mc.getChattingRoom().getId())
                        .build())
                .collect(Collectors.toList());

        //1_1. 이후 이차적으로 각 ChattingRoom에서의 targetMemberId를 추출하여 DTO에 담기
        List<Long> targetMemberIdList = getTargetMemberIdList(member);

        //1_2. 이후 각 ChattingRoom에서의 targetMemberChattingId를 추출하여 DTO에 담기
        List<Long> targetMemberChattingIdList = getTargetMemberChattingIdList(member);

        //2_1. 이후 그 targetMemberId값을 , 각각의 ChattingDto에 넣어줌 (비로소 targetMemberId 정보를 넣어줌)
        setTargetMemberIdList(chattingDtoList, targetMemberIdList);

        //2_2. 또한 그 targetMemberChattingId 값을 , 각각의 ChattingDto에 넣어줌 (비로소 targetChattingMemberId 정보를 넣어줌)
        setTargetMemberChattingIdList(chattingDtoList, targetMemberChattingIdList);

        return chattingDtoList;
    }

    private List<Long> getTargetMemberIdList(Member member){
        return member.getChattingMemberList().stream()
                .map(mc -> mc.getChattingRoom())
                .map(c -> c.getChattingMemberList().get(0).getMember().getId() != member.getId()
                        ? c.getChattingMemberList().get(0).getMember().getId()
                        : c.getChattingMemberList().get(1).getMember().getId())
                .collect(Collectors.toList());
    }

    private List<Long> getTargetMemberChattingIdList(Member member){
        return member.getChattingMemberList().stream()
                .map(mc -> mc.getChattingRoom())
                .map(c -> c.getChattingMemberList().get(0).getMember().getId() != member.getId()
                        ? c.getChattingMemberList().get(0).getId()
                        : c.getChattingMemberList().get(1).getId())
                .collect(Collectors.toList());
    }

    private void setTargetMemberIdList(List<ChattingDto> chattingDtoList, List<Long> targetMemberIdList){


        //여기서 for문을 안쓰고 싶었지만 .. 어쩔 수 없었따..
        List<Integer> list = new ArrayList<>();
        for(int i=0; i<targetMemberIdList.size(); i++){
            list.add(i);
        }

        /** ChattingDtoList를 만들 떄와 , TargetMemberIdList를 만들 떄 모두, member.getChattingMemberList() 의 대응되는 ChattingRoom
         *  에서 부터 시작했으므로 -> 결과적으로 대응되는 element의 순서가 같게 됨  (이로써 같은 idx에 대응되도록 setting 하는게 가능!)*/
        list.stream()
                .forEach(i -> chattingDtoList.get(i).setTargetMemberId(targetMemberIdList.get(i)));

    }

    private void setTargetMemberChattingIdList(List<ChattingDto> chattingDtoList, List<Long> targetMemberChattingIdList){

        //여기서 for문을 안쓰고 싶었지만 .. 어쩔 수 없었따..
        List<Integer> list = new ArrayList<>();
        for(int i=0; i<targetMemberChattingIdList.size(); i++){
            list.add(i);
        }

        /** ChattingDtoList를 만들 떄와 , TargetMemberChattingIdList를 만들 떄 모두,
         * member.getChattingMemberList() 의 대응되는 ChattingRoom 에서 부터 시작했으므로
         * -> 결과적으로 대응되는 element의 순서가 같게 됨  (이로써 같은 idx에 대응되도록 setting 하는게 가능!)*/
        list.stream()
                .forEach(i -> chattingDtoList.get(i).setTargetChattingMemberId(targetMemberChattingIdList.get(i)));
    }

    /**------------------------------------------------------------------------------------------------------*/

    /** [메세지 전송 서비스] */
    @Transactional
    public ChatDto sendMessage(String username, Long chattingRoomId, Long targetMemberId, String content){
        //1. 유효성 검사
        Member sendMember = memberRepository.findByUsername(username);

        // 1_1. 메세지를 보내는 sender와 받는 Receiver가 같지 않아야 한다
        checkSenderReceiver(sendMember.getId(), targetMemberId);
        // 1_2. 해당 targetMember가 참여하고 있는 해당 chattingRoom이 정발로 존재하는지 (내 API 구현 논리상, 그리고 ERD 논리상 , 이 한방만 검사해주면 됨)
        checkRoomWithTargetMember(chattingRoomId, targetMemberId);


        //2. 넘어온 정보를 기반으로 ChattingContent 엔티티를 생성하여 insert -> 이 자 체가 곧 메세지 전송 역할
        return insertMessage(chattingRoomId, targetMemberId, content);


    }

    private void checkSenderReceiver(Long senderId, Long receiverId){
        if(senderId.equals(receiverId)){
            throw new ApiException(ApiResponseStatus.SAME_SENDER_RECEIVER, "메세지 전송 시점 : 송신자와 수신자가 같을수는 없습니다.");
        }
    }
    private void checkRoomWithTargetMember(Long chattingRoomId, Long targetMemberId){
        if(!chattingMemberRepository.existsByChattingRoomIdAndMemberId(chattingRoomId, targetMemberId)){
            throw new ApiException(ApiResponseStatus.INVALID_SEND_MESSAGE, "메세지 전송 시점 : 그런 ID를 가진 ChattingRoom  또는 그런 ChattingRoom에 참여하고 있는 targetMember는 존재하지 않습니다.");
        }
    }

    private ChatDto insertMessage(Long chattingRoomId, Long targetMemberId, String content){
        ChattingContent chattingContent = ChattingContent.builder()
                .chattingRoomId(chattingRoomId)
                .targetMemberId(targetMemberId)
                .content(content)
                .build();

        chattingContentRepository.save(chattingContent);

        return ChatDto.builder().chattingContentId(chattingContent.getId()).build();
    }



    /**------------------------------------------------------------------------------- ----------------------*/

    /** [메세지 수신 서비스] */
    @Transactional
    public List<ContentDto> receiveMessage(String username, Long chattingRoomId, int page, int limit){

        Member member = memberRepository.findByUsername(username);

        //1_1. 해당 targetMember가 참여하고 있는 해당 chattingRoom이 정발로 존재하는지
        // (내 API 구현 논리상, 그리고 ERD 논리상 , 이 한방만 검사해주면 됨)
        checkRoomWithTargetMember(chattingRoomId, member.getId());

        ChattingMember chattingMember = chattingMemberRepository.findByChattingRoomIdAndMemberId(chattingRoomId, member.getId());
        //2. 안읽은 메세지를 페이징 처리하여 가져오고 , 이때 첫 번째 페이지 일 경우
        // -> 그 가장 앞선 contentId를 ChattingMember의 lastReadContentId로 설정
        // (이떄 그런 안읽은 메세지가 없으면 예외 발생 )
        List<ChattingContent> notReadMessage = chattingContentRepository.findNotReadMessage(chattingRoomId, member.getId(),
                Optional.ofNullable(chattingMember.getLastReadContentId()).orElse(0L), chattingContent.createdAt.desc(), page, limit);

        if(CollectionUtils.isEmpty(notReadMessage)){
            throw new ApiException(ApiResponseStatus.NO_NOT_READ_MESSAGE, "메세지 조회 시점 : 아직 읽지 않은 메세지가 없습니다.");
        }

        if(notReadMessage.get(0).getId() > Optional.ofNullable(chattingMember.getLastReadContentId()).orElse(0L)){
            chattingMember.setLastReadContentId(notReadMessage.get(0).getId());
        }

        return notReadMessage.stream().map(
                c -> ContentDto.builder().chattingContentId(c.getId())
                        .chattingRoomId(chattingRoomId)
                        .targetMemberId(member.getId())
                        .content(c.getContent())
                        .createdAt(c.getCreatedAt())
                        .build()
        ).collect(Collectors.toList());


    }
    /**------------------------------------------------------------------------------------------------------*/

    /** [특정 채팅룸에서 , 상대방이 어떤 메세지까지 읽었는지에 대한 그 시간을 조회하는 서비스] */

    public LastChatDto getLastReadDateTime(String username, Long chattingRoomId, Long targerMemberId){

        //1_1. 일단 그 ChattingRoom에 , 나하고 상대방이 모두 참여하고 있는게 맞는지 검사
        Member member = memberRepository.findByUsername(username);
        checkRoomInfo(chattingRoomId, member.getId(), targerMemberId);

        ChattingMember chattingMember = chattingMemberRepository.findChattingMember(chattingRoomId, targerMemberId);

        //2_1. 만약 채팅방이 생성된지 얼마 안되서, 상대방이 메세지를 하나도 읽지 않았다면
        if(Optional.ofNullable(chattingMember.getLastReadContentId()).isEmpty()){
            return LastChatDto.builder().isLastMessage(false).build();
        }

        //2_2. 그렇지 않고 상대방이 마지막으로 읽은 메세지의 시간을 조회하여 반환
        ChattingContent chattingContent = chattingContentRepository.findById(chattingMember.getLastReadContentId()).get();
        return LastChatDto.builder().isLastMessage(true)
                .lastDateAt(chattingContent.getCreatedAt().toLocalDate())
                .lastTimeAt(chattingContent.getCreatedAt().toLocalTime())
                .build();
    }

    private void checkRoomInfo(Long chattingRoomId, Long memberId, Long targetMemberId){
        if(!(chattingMemberRepository.existsByChattingRoomIdAndMemberId(chattingRoomId,memberId) &&
           chattingMemberRepository.existsByChattingRoomIdAndMemberId(chattingRoomId, targetMemberId))){
            throw new ApiException(ApiResponseStatus.INVALID_ROOM_INFO, "마지막으로 메세지를 읽은 시점 조회 : 채팅룸 및 참여자 정보가 잘못되었습니다.");

        }
    }

    /**------------------------------------------------------------------------------------------------------*/
    /**[특정 채팅방에 , 새로운 메세지가 왔는지의 여부를 알려주는 서비스] */

    public NewContentDto isNewMessage(String username, Long chattingRoomId, Long lastchattingContentId){

        //1_1. 해당 targetMember가 참여하고 있는 해당 chattingRoom이 정발로 존재하는지
        Member member = memberRepository.findByUsername(username);
        checkRoomWithTargetMember(chattingRoomId, member.getId());

        //2. 클라이언트가 가지고 있던 마지막 메세지의 Id과 비교하여 새로운 메세지가 있다면 이를 보냄
        Boolean isNewContent = chattingContentRepository.existsByIdAfter(lastchattingContentId);
        Integer newMessageCount = chattingContentRepository.findNewMessageCount(chattingRoomId, member.getId(), lastchattingContentId);

        return NewContentDto.builder().isNewContent(isNewContent).cntOfNewMessage(newMessageCount).build();

    }

    /**------------------------------------------------------------------------------------------------------*/

    /** [각 디바이스에 저장된 로컬 Chatting Data를 기반으로 , 서버에 존재하는 최신 Chatting Room Data를 넘겨주는 서비스] */
    public NewChattingRoomDto updateChatting(String username, List<Long> chattingRoomIdList){

        //1_1. 서버에 저장된 ChattingRoom이 하나도 없는 것은 아닌가
        if(Optional.ofNullable(memberRepository.findMemberWithChatting(username)).isEmpty()){
            return  NewChattingRoomDto.builder().chattingDtoList(new ArrayList<>()).sizeOfChatting(0).build();
        }

        //1_2. 유효성 검사 : 진짜 저 chattingRoomMeta에 있는 ChattingRoomId들이 모두 유효한 값인가  (이 username의 Member에 대해서)
        Member member = memberRepository.findMemberWithChatting(username);
        checkChattingRoom(member, chattingRoomIdList);

        //2. 서버에 존재하는 채팅 데이터와 , 로켈에 존재하는 채팅데이터를 비교하여 , 로컬에 존재하지 않는 채팅방 데이터를 보내줌
        NewChattingRoomDto newChattingRoomDto = NewChattingRoomDto.builder().build();
        updateChattingRoom(member, chattingRoomIdList, newChattingRoomDto);

        //3. 그렇게 각 값이 setting된 UnReadChattingDto를 반환
        return newChattingRoomDto;

    }

    private void checkChattingRoom(Member member, List<Long> chattingRoomIdListOfLocal){

        // 서버에 저장된 해당 MEMBER가 참여하는 ChattingRoom의 Id들을 모두 조회
        List<Long> chattingRoomIdList = member.getChattingMemberList().stream()
                .map(cm -> cm.getChattingRoom().getId())
                .collect(Collectors.toList());

        // 그리하여 해당 디바이스의 로컬에 존재하는 ChattingRoom의 Id가 , 서버에 저장된 Id 리스트에 있는지를  check
        // (만약 이 리스트에 없다면 잘못된 ChattingRoomId 니깐)
        boolean isAllValid = chattingRoomIdListOfLocal.stream()
                .allMatch(cri -> chattingRoomIdList.contains(cri));

        if(!isAllValid){
            throw new ApiException(ApiResponseStatus.INVALID_CHATTING_ROOM,
                    "로컬과 서버의 채팅룸 데이터 최신화 시점 : 해당 디바이스의 로컬에 저장된 채팅 룸 정보가 유효하지 않은 정보가 포함되어 있습니다.");
        }

    }


    private void updateChattingRoom(Member member, List<Long> chattingRoomIdList, NewChattingRoomDto newChattingRoomDto){

        //1_1. 로컬에 없는 ChattingRoom들의 Id를 조회 -> 서버에 있는 ChattingRoomIdList와 , 로컬의 ChattingRoomIdList를 비교
        List<Long> notExistChattingRoomIdList = member.getChattingMemberList().stream()
                .map(cm -> cm.getChattingRoom().getId())
                .filter(cri -> !chattingRoomIdList.contains(cri))
                .collect(Collectors.toList());


        //1_2. 그렇게 로컬에 없는 ChattingRoomId 들을 필터링 조건으로 사용하여 ChattingDtoList를 생성
        List<ChattingDto> chattingDtoList = member.getChattingMemberList().stream()
                .filter(mc -> notExistChattingRoomIdList.contains(mc.getChattingRoom().getId()))
                .map(mc -> ChattingDto.builder()
                        .memberId(member.getId())
                        .itemId(mc.getChattingRoom().getItem().getId())
                        .chattingMemberId(mc.getId())
                        .chattingRoomId(mc.getChattingRoom().getId())
                        .build())
                .collect(Collectors.toList());

        //1_3. 이후 이차적으로 각 ChattingRoom에서의 targetMemberId를 추출하여 DTO에 담기
        List<Long> targetMemberIdList = getNotExistTargetMemberIdList(member, notExistChattingRoomIdList);
        setTargetMemberIdList(chattingDtoList, targetMemberIdList);

        //1_4. 이후 각 ChattingRoom에서의 targetMemberChattingId를 추출하여 DTO에 담기
        List<Long> targetMemberChattingIdList = getNotExistTargetMemberChattingIdList(member, notExistChattingRoomIdList);
        setTargetMemberChattingIdList(chattingDtoList, targetMemberChattingIdList);

        //2. UnReadChattingDto에 가공한 정보를 대입
        newChattingRoomDto.setChattingDtoList(chattingDtoList);
        newChattingRoomDto.setSizeOfChatting(chattingDtoList.size());

    }

    private List<Long> getNotExistTargetMemberIdList(Member member, List<Long> notExistChattingRoomIdList){
        return member.getChattingMemberList().stream()
                .filter(mc -> notExistChattingRoomIdList.contains(mc.getChattingRoom().getId()))
                .map(mc -> mc.getChattingRoom())
                .map(c -> c.getChattingMemberList().get(0).getMember().getId() != member.getId()
                        ? c.getChattingMemberList().get(0).getMember().getId()
                        : c.getChattingMemberList().get(1).getMember().getId())
                .collect(Collectors.toList());
    }

    private List<Long> getNotExistTargetMemberChattingIdList(Member member, List<Long> notExistChattingRoomIdList){
        return member.getChattingMemberList().stream()
                .filter(mc -> notExistChattingRoomIdList.contains(mc.getChattingRoom().getId()))
                .map(mc -> mc.getChattingRoom())
                .map(c -> c.getChattingMemberList().get(0).getMember().getId() != member.getId()
                        ? c.getChattingMemberList().get(0).getId()
                        : c.getChattingMemberList().get(1).getId())
                .collect(Collectors.toList());
    }






}
