package com.daangn.clone.chattingroom.controller;

import com.daangn.clone.chattingroom.dto.ChattingDto;
import com.daangn.clone.chattingroom.dto.ChattingListDto;
import com.daangn.clone.chattingroom.dto.EnterRoomDto;
import com.daangn.clone.chattingroom.service.ChattingRoomService;
import com.daangn.clone.common.enums.InRoomYn;
import com.daangn.clone.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChattingApiController {

    private final ChattingRoomService chattingRoomService;


    /**
     * [API 19번] : 구매를 위한 채팅 요청 -> 어디까지나 해당 요청에 의한 채팅방 정보만 넘겨주는 기능
     *                                + But 결국 판매자는 채팅방에 들어간거니까 판매자의 InRoomYn 값은 업데이트 해주는게 맞을거 같다
     *
     * case 1. 해당 아이템에 대해 한번도 채팅이 시도되지 않은 경우 -> 무조건 ChattingRoom을 새로 만들어서 그 정보를 반환
     *
     * case 2. 해당 아이템에 대해 다른 사용자에 의한 채팅은 시도되었지만 && 요청한 사용자에 대한 채팅은 시도되지 않은 경우
     *          -> 어쨌거나 새로운 ChattingRoom을 새로 만들어서 그 정보를 반환
     *
     * case 3. 해당 아이템에 대해 , 요청을 보내는 사용자가 과거에 채팅을 시도했었어서 ~> 이전에 생성된 ChattingRoom이 존재하는 경우
     *          -> 그 ChattingRoom을 조회하여 해당 정보를 반환
     * */
   @PostMapping("/chat/{itemId}")
    public ApiResponse<ChattingDto> startChatting(@RequestHeader String username, @PathVariable Long itemId){
        return ApiResponse.success(chattingRoomService.setChatting(username, itemId));
    }

    /**
     * [API 20번] : 내가 참여한 모든 채팅방 조회 -> 어디까지나 내가 참여한 채팅방들 정보만 넘겨주는 기능
     * -> 하단의 채팅 탭을 눌렀을 때 , 자신이 Seller로든 + EXPECTED_BUYER로든 참여한 모든 채팅방 정보를 리스트로 반환
     * */
    @GetMapping("/chatList")
    public ApiResponse<ChattingListDto> getChattingList(@RequestHeader String username){
        return ApiResponse.success(chattingRoomService.getChattingList(username));
    }

    /**
     * [API 21번] : 특정 채팅방에 들어갈 때
     * -> API19를 통한 특정 채팅방 or API20을 통해 조회한 여러 채팅방 중 선택한 특정 채팅방에 들어갔을 때!
     * (단 API19에서 , EXPECTED_BUYER는 이미 채팅방을 개설 or 이미 존재하는 채팅방에 입장까지 하므로
     * -> 그 EXPECTED_BUYER에 대한 InRoomYn 값은 API19에서 변경해준다)
     *
     * -> 아이템 상세페이지에서 구매하려는 사용자가(EXPECTED_BUYER)채팅을 시도하면서 채팅방에 입장하는 경우는 API19에 포함되어 있으므로
     * => 여기서는 SELLER든 BUYER든 채팅 목록에서 특정 채팅방에 입장하는 경우 -> 입장한 그 SELLER or BUYER에 대응되는 MemberChatting의
     * InRoomYn 값을 Y로 업데이트 해줘야 한다. (일단 알림을 통해 들어가는 경우는 우리는 생각하지 않으니, 전체 채팅 목록에서 입장하는 경우만 생각!)
     *
     * => 당연히 쿼리파라미터를 이용하여 , Enter 하는 순간 뿐만 아니라 Exit 하는순간에도 -> 그에 따라 InRoomYn 값을 N으로 업데이트 해준다.
     * */
    @PatchMapping("/chat/{chattingRoomId}")
    public ApiResponse<EnterRoomDto> enterTheRoom(@RequestHeader String username,
                                                  @PathVariable Long chattingRoomId, @RequestParam InRoomYn inRoomYn){
        return ApiResponse.success(chattingRoomService.EnterOrExitTheChattingRooom(username, chattingRoomId, inRoomYn));
    }

}
