package org.example.authcenter.dto;

import lombok.Data;

/**
 * Request body for admin-side personal account management.
 */
@Data
public class AdminUserDTO {

    private Long id;
    private String username;
    private String password;
    private String name;
    private String displayName;
    private String email;
    private String phone;
    private String avatar;
    private String introduction;
    private Integer status;
    private Integer userType;
    private String roleCode;
}
