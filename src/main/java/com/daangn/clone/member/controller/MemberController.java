package com.daangn.clone.member.controller;

import com.daangn.clone.common.response.ApiResponse;
import com.daangn.clone.member.dto.LoginMemberDto;
import com.daangn.clone.member.dto.LoginRequest;
import com.daangn.clone.member.dto.SignUpRequest;
import com.daangn.clone.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /** [API.1] : 회원가입 */
    @PostMapping("/signup")
    public ApiResponse<String> signUp(@Validated @RequestBody SignUpRequest signUpRequest){
        return ApiResponse.success(memberService.signUp(signUpRequest.getUsername(), signUpRequest.getEncryptPassword(), signUpRequest.getNickname(), signUpRequest.getTownName()));
    }

    /** [API.2] : 로그인 */
    @GetMapping("/login")
    public ApiResponse<LoginMemberDto> login(@Validated @RequestBody LoginRequest loginRequest){
        return ApiResponse.success(memberService.login(loginRequest.getUsername(), loginRequest.getEncryptPassword()));
    }

}
