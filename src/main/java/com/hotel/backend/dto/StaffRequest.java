package com.hotel.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StaffRequest {

    @Size(max = 64, message = "Mã nhân viên quá dài")
    private String id;

    @Size(max = 100, message = "Tên đăng nhập không được vượt quá 100 ký tự")
    private String username;

    @NotBlank(message = "Họ và tên nhân viên không được để trống")
    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 100, message = "Họ và tên đầy đủ không được vượt quá 100 ký tự")
    private String fullName;

    @Email(message = "Email nhân viên không đúng định dạng")
    @Size(max = 150, message = "Email quá dài")
    private String email;

    @Size(max = 25, message = "Số điện thoại quá dài")
    private String phone;

    @NotBlank(message = "Vai trò không được để trống")
    @Size(max = 64, message = "Mã vai trò không hợp lệ")
    private String roleId;

    @Size(max = 120, message = "Tên vai trò quá dài")
    private String roleName;

    @Size(max = 30, message = "Trạng thái không hợp lệ")
    private String status;

    private Boolean active;

    @Size(max = 64, message = "Thông tin đăng nhập cuối quá dài")
    private String lastLogin;

    @Pattern(regexp = "^$|.{6,100}", message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String password;
}
