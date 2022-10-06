package com.daangn.clone.chattingroom.advice;

import com.daangn.clone.chattingroom.controller.ChattingApiController;
import com.daangn.clone.common.response.ApiException;
import com.daangn.clone.common.response.ApiResponse;
import com.daangn.clone.common.response.ApiResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.daangn.clone.common.response.ApiResponseStatus.*;

@Slf4j
@RestControllerAdvice(assignableTypes = ChattingApiController.class)
public class ChattingApiControllerAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse registerItemExHandler(ApiException e) {
        log.error("EXCEPTION = {} , INTERNAL_MESSAGE = {}", e.getStatus(), e.getInternalMessage());
        return ApiResponse.fail(e.getStatus());
    }

}
