package com.daangn.clone.common.interceptor;

import com.daangn.clone.common.enums.Status;
import com.daangn.clone.common.response.ApiResponse;
import com.daangn.clone.common.response.ApiResponseStatus;
import com.daangn.clone.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final MemberRepository memberRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    /** 요청이 Controller에게 도달하기 전에 먼저 , 이 인터셉터의 preHandle() 이 호출된다
     * preHandle()의 리턴값이 false 면 요청이 Controller에게 도달하지 x  <- 이점을 이용!!
     */

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // username 헤더값이 있을 경우에만 , 그 username으로 된 사용자가 유효한 사용자인지의 결과를 isValidUsername에 담음
        Optional<Boolean> isValidUsername = Optional.ofNullable(request.getHeader("username"))
                .map(u -> memberRepository.existsByUsernameAndStatus(u, Status.ACTIVE));

        // true든 false든 isValidUsername 값이 존재한다면 -> 그것은 username필드가 존재하여 map() 작업이 수행되었다는 의미이므로
        // => 그 유효성 결과에 따른 응답을 보낸다

        // isValidUsername 값이 존재하지 않는다면 -> 그것은 username필드가 존재하지 않아 map() 작업이 수행되지 않았다는 의미이므로
        // => NO_USERNAME 에 따른 응답을 보낸다
        if(isValidUsername.isPresent()){
            return sendResponse(isValidUsername.get(), response, ApiResponseStatus.INVALID_MEMBER);
        } else{
            return sendResponse(false, response, ApiResponseStatus.NO_USERNAME);
        }

    }

    private boolean sendResponse(boolean isValidUsername, HttpServletResponse response, ApiResponseStatus status){
        /** 유효한 username 이었다면 */
        if(isValidUsername){
            return true;
        }

        /** 유효한 username 이 아니었다면 */
        //응답의 meta 정보를 setting한 후
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            //실질적인 응답값을 , JSON 형식의 String으로 변환화여 보낸다.
            String result = objectMapper.writeValueAsString(ApiResponse.fail(status));
            response.getWriter().print(result);
        }
        catch (IOException e){
            log.error("필수 헤더인 username값이 들어오지 않았거나 , 유효하지 않은 username 값이 들어와, 그에따른 응답을 처리하는 과정에서 예외가 발생하였습니다.");
        }

        return false;


    }

}
