package com.daangn.clone.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotNull(message = "아이디는 필수입니다.")
    private String username;

    @NotNull(message = "비밀번호는 필수입니다.")
    @Size(min = 64, max = 64, message = "비밀번호는 Sha256으로 해시화 된 , 해시값을 넘겨야 합니다.")
    private String encryptPassword;
}
