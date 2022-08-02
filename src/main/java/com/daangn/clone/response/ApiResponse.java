package com.daangn.clone.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonPropertyOrder({"isSuccess", "code", "message", "result"}) // Json 으로 나갈 순서를 설정하는 어노테이션
@JsonInclude(JsonInclude.Include.NON_NULL) // Json으로 응답이 나갈 때 - null인 필드는(CLASS LEVEL에 붙었으니) 응답으로 포함시키지 않는 어노테이션
public class ApiResponse<T> {

    @JsonProperty("isSuccess") // Json으로 변환될 때 - 해당 필드의 Key 이름을 "isSuceess"로 설정하는 어노테이션
    private final boolean isSuccess; // null인 경우를 막고자 primitive 타입으로 설정함

    @JsonProperty("code")
    private final int code; // null인 경우를 막고자 primitive 타입으로 설정함


    private final String message;

    private T result;

    /** 생성자를 private으로 정의하므로 써 , 생성자를 통한 객체 생성을 막고 , 대신 static 생성메서드를 통한 객체 생성을 유도함*/

    // (result 필드는 어차피 null 초기값이 그대로 있어서 - Json으로 변환시 나가지 않을 것)
    private ApiResponse(boolean isSuccess, int code, String message) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }

    private ApiResponse(boolean isSuccess, int code, String message, T result) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    /** API 성공시 나가는 응답 */
    public static <T> ApiResponse<T> successResponse(ApiResponseStatus status, T result){
        return new ApiResponse<>(status.isSuccess(), status.getCode(), status.getMessage(), result);
    }

    /** API 실패시 나가는 응답 */
    public static ApiResponse failResponse(ApiResponseStatus status){
        return new ApiResponse<>(status.isSuccess(), status.getCode(), status.getMessage());
    }




}
