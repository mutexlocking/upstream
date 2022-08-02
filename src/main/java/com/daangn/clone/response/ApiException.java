package com.daangn.clone.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class ApiException {

    private final ApiResponseStatus status;
}
