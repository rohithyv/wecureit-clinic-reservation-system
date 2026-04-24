package com.wecureit.login.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionData {
    private String email;
    private String mobile;
    private String fullName;
    private String role;
    private String id;
}
