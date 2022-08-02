package com.daangn.clone.response;

import lombok.Getter;

/**
 * [ 1000단위 ] - 오류의 범위
 *  1000 : 요청 성공
 *  2 : Request 오류
 *  3 : Reponse 오류
 *  4 : DB, Server 오류
 *
 * [ 100단위 ] - 오류 도메인
 *  0 : 공통 오류
 *  1 : member 오류
 *  2 : item 오류
 *  3 : chatting 오류
 *
 * [10단위] - 오류 HTTP Method
 *  0~19 : Common
 *  20~39 : GET
 *  40~59 : POST
 *  60~79 : PATCH
 *  80~99 : else
 *
 *  [1 단위] - 그외 오류의 고유 식별자
 *          - 순서대로 설정해주면 됨
 *

/**  [ApiResponse 로 나갈 값들을 - 상황에 따른 열거형 값으로 미리 선언해 놓고 - 가져다 쓰는 형태를 위해서 사용]*/
@Getter
public enum ApiResponseStatus {

    /**
     * 1000 : 요청 성공
     * */
    SUCCESS(true, 1000, "요청에 성공하였습니다."),

    /**
     * 2000 : Request 오류
     * */
    FAILTOFIND(false, 2001, "FIND에 실패하였습니다."),
    FAILTOPOST(false, 2002, "POST에 실패하였습니다."),
    FAILTOUPDATE(false, 2003, "UPDATE에 실패하였습니다."),

    INVALIDUSERNAME(false, 2141, "유효하지 않은 USERNAME 입니다."),
    INVALIDPASSWORD(false, 2142, "유효하지 않은 PASSWORD 입니다."),

    EXISTUSERNAME(false, 2143, "이미 존재하는 USERNAME 입니다."),
    EXISTNICKNAME(false, 2144, "이미 존재하는 NICKNAME 입니다.");

    /**
     * 3000 : Response 오류
     * */

    /**
     * 4000 : Database, Server 오류
     * */



    private final boolean isSuccess;
    private final int code;
    private final String message;

    /**
     * ApiResponseStatus에서 각 해당하는 코드를 생성자로 맵핑
     * [열겨형의 생성자 - 반드시 private]
     * <이렇게 열거형 생성자를 정의하면 - 열거형 값의 선언시 , 소괄호를 통해 생성자에 인자를 전달할 수 있음 >
     *     : 그렇게 되면 결과적으로는 열거형 타입의 (열거형도 class) 객체가 생성되고 - 그 객체의 필드가 소괄호에 인자로 전달한 값 대로 초기화되는것!
     * */
    private ApiResponseStatus(boolean isSuccess, int code, String message){
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }

}
