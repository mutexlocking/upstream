package com.daangn.clone.member.service;

import com.daangn.clone.common.enums.Status;
import com.daangn.clone.common.jwt.JwtUtil;
import com.daangn.clone.common.response.ApiException;
import com.daangn.clone.common.response.ApiResponseStatus;
import com.daangn.clone.encryption.Sha256;
import com.daangn.clone.member.Member;
import com.daangn.clone.member.dto.LoginMemberDto;
import com.daangn.clone.member.repository.MemberRepository;
import com.daangn.clone.town.repository.TownRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final TownRepository townRepository;
    private final Sha256 sha256;
    private final JwtUtil jwtUtil;

    private void checkUsername(String username){
        if(memberRepository.existsByUsernameAndStatus(username, Status.ACTIVE)){
            throw new ApiException(ApiResponseStatus.NESTED_USERNAME, "회원가입 시점 : 사용하려는 아이디가 이미 사용되고 있는 아이디 입니다.");
        }
    }


    private void checkNickname(String nickname){
        if(memberRepository.existsByNicknameAndStatus(nickname, Status.ACTIVE)){
            throw new ApiException(ApiResponseStatus.NESTED_NICKNAME, "회원가입 시점 : 사용하려는 닉네임이 이미 사용되고 있는 닉네임 입니다.");
        }
    }

    private void checkTownName(String townName){
        if(!townRepository.existsByName(townName)){
            throw new ApiException(ApiResponseStatus.INVALID_TOWN_NAME, "회원가입 시점 : 유효하지 않은 행정동 이름 입니다.");
        }
    }

    /** [비밀번호 암호화] : 입력한 비밀번호를 저장하기 위해 암호화 하는 내부 서비스 */
    private String encryptPassword(String password){
        return sha256.encrypt(password);
    }

    private Long saveMember(String username, String encryptPassword, String nickname, String townName){
        Member member = Member.builder()
                .username(username)
                .password(encryptPassword)
                .nickname(nickname)
                .townId(townRepository.findByName(townName))
                .status(Status.ACTIVE)
                .build();

        memberRepository.save(member);

        return member.getId();
    }

    /** 회원가입 서비스 */
    @Transactional
    public String signUp(String username, String password, String nickName, String townName){

        //0. 유효성 검사 : username과 nickname이 이미 사용되고 있지는 않은지 검사 + 실제로 존재하는 townName인지 검사
        checkUsername(username);
        checkNickname(nickName);
        checkTownName(townName);

        //1. 클라이언트에서 해시화한 비밀번호가 넘어온다

        //2. username과 nickname, (클라이언트에서 넘어온) 암호화된 password, 그리고 townName을 가지고 Member 엔티티를 생성하여 db에 insert
        saveMember(username, password, nickName, townName);

        return "회원가입에 성공했습니다.";
    }

    /** ----------------------------------------------------------------------------------------------------------*/

    private void checkLogin(String username, String encryptPassword){

        //1. 그런 username과 password를 가진 Member가 없으면 out
        if(!memberRepository.existsByUsernameAndPasswordAndStatus(username, encryptPassword, Status.ACTIVE)){
            throw new ApiException(ApiResponseStatus.FAIL_LOGIN, "로그인 시점 : 아이디 또는 비밀번호가 잘못되어, 로그인에 실패하였음");
        }

    }



    /** [로그인 서비스] */
    public LoginMemberDto login(String username, String encryptPassword){

        //1. 아이디와 비밀번호 일치성 검사
        checkLogin(username, encryptPassword);

        //2. jwt 생성
        String token = jwtUtil.createToken(username);

        return LoginMemberDto.builder().token(token).build();
    }


}
