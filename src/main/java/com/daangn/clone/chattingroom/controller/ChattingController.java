package com.daangn.clone.chattingroom.controller;

import com.daangn.clone.chattingroom.dto.ChattingDto;
import com.daangn.clone.chattingroom.dto.ChattingListDto;
import com.daangn.clone.chattingroom.dto.polling.*;
import com.daangn.clone.chattingroom.dto.unread.ChattingRoomMeta;
import com.daangn.clone.chattingroom.dto.unread.NewChattingRoomDto;
import com.daangn.clone.chattingroom.service.ChattingRoomService;
import com.daangn.clone.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChattingController {

    private final ChattingRoomService chattingRoomService;


    /**
     * [API] : 구매를 위한 채팅 요청 -> 어디까지나 해당 요청에 의한 채팅방 정보만 넘겨주는 기능
     *
     * case 1. 해당 아이템에 대해 한번도 채팅이 시도되지 않은 경우 -> 무조건 ChattingRoom을 새로 만들어서 그 정보를 반환
     *
     * case 2. 해당 아이템에 대해 다른 사용자에 의한 채팅은 시도되었지만 && 어쨌거나 요청한 사용자에 대한 채팅은 시도되지 않은 경우
     *          -> 똑같이 새로운 ChattingRoom을 새로 만들어서 그 정보를 반환
     *
     * case 3. 해당 아이템에 대해 , 요청을 보내는 사용자가 과거에 채팅을 시도했었어서 ~> 이전에 생성된 ChattingRoom이 존재하는 경우
     *          -> 그 ChattingRoom을 조회하여 해당 정보를 반환
     * */
   @PostMapping("/chat/{itemId}")
    public ApiResponse<ChattingDto> startChatting(@RequestAttribute String username, @PathVariable Long itemId){
        return ApiResponse.success(chattingRoomService.bindChatting(username, itemId));
    }

    /**
     * [API] : 내가 참여한 모든 채팅방 조회 -> 어디까지나 내가 참여한 채팅방들 정보만 넘겨주는 기능
     * -> 하단의 채팅 탭을 눌렀을 때 , 자신이 Seller로든 + EXPECTED_BUYER로든 참여한 모든 채팅방 정보를 리스트로 반환
     * -> 여기서 넘어가는 ChattingRoom의 정렬 기준은 - 생성일자를 기준으로 내림차순 (즉 생성된 최신순)
     *
     * <리팩터링 결과>
     * => 쿼리 한방으로 처리할 수 있겠다는 피드백을 받았지만 ,
     *    실제로는 targetMemberId와 targeChattingMemberId 때문에 쿼리 한방으로 처리할 수 는 없었다.
     *
     * => 따라서 그럴 빠에는 dto로 조회하고 , 또 fetch join 할빠엔 -> 그냥 fetch join만 해서 ,
     *    적절히 dto로 변환하는 방식을 그대로 사용
     *
     * => 대신 함수로만 적절히 분리하여 가독성에 신경썼다.
     * */
    @GetMapping("/chatList")
    public ApiResponse<ChattingListDto> getChattingList(@RequestAttribute String username){
        return ApiResponse.success(chattingRoomService.getChattingList(username));
    }

    /**
     * [API] : 특정 ChattingRoom의 특정 targetMember에게 메세지를 보내는 기능
     * */
    @PostMapping("/chat/content")
    public ApiResponse<ChatDto> postChattingContent(@RequestAttribute String username,
                                                    @Validated  @RequestBody ChatRequest chatRequest){
        return ApiResponse.success(chattingRoomService.sendMessage(username,
                chatRequest.getChattingRoomId(), chatRequest.getTargetMemberId(), chatRequest.getContent()));
    }

    /**
     * [API] : 특정 ChattingRoom에 참여중인 특정 targetMember에게 온 메세지를 넘겨주는 기능 (즉 메세지 수신)
     * */

    @PatchMapping("/chat/content")
    public ApiResponse<List<ContentDto>> getChattingContent(@RequestAttribute String username,
                                                      @Validated @RequestBody ContentRequest contentRequest){
        return ApiResponse.success(chattingRoomService.receiveMessage(username,
                contentRequest.getChattingRoomId() ,contentRequest.getPage(), contentRequest.getLimit()));
    }

    /** [API] : 특정 사용자가 , 특정 채팅방에서 , 마지막으로 읽은 메세지의 시간을 조회해오는 기능 */

    @GetMapping("/chat/content")
    public ApiResponse<LastChatDto> getLastReadDateTime(@RequestAttribute String username,
                                                        @Validated @ModelAttribute LastChatRequest lastChatRequest){
        return ApiResponse.success(chattingRoomService.getLastReadDateTime(username,
                lastChatRequest.getChattingRoomId(), lastChatRequest.getTargetMemberId()));
    }

    /** [API] : 특정 채팅방에서 해당 username의 Member에 온 메세지 중에서 아직 읽지 않은 메세지가 있는지의 여부를 확인하는 기능*/

    @GetMapping("/chat/new/content")
    public ApiResponse<NewContentDto> getIsNewContent(@RequestAttribute String username,
                                                      @ModelAttribute NewContentRequest newContentRequest){

        return ApiResponse.success(chattingRoomService.isNewMessage(username,
                newContentRequest.getChattingRoomId(), newContentRequest.getLastChattingContentId()));

    }

    /** [API] : 로그인 성공 후 , 사용자의 로컬에 저장된 ChattingRoom 정보와 , 서버에 최신화된 ChattingRoom 정보를 비교하여 ,
     * 로컬에는 없는 최신 ChattingRoom 정보를 조회해오는 기능  */

    @GetMapping("/newChattingRoom")
    public ApiResponse<NewChattingRoomDto> getChattingContent(@RequestAttribute String username,
                                                              @ModelAttribute ChattingRoomMeta chattingRoomMeta){

        return ApiResponse.success(chattingRoomService.updateChatting(username,
                                                    chattingRoomMeta.getChattingRoomIdList()));
    }





}
