package com.hotel.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 100, message = "Tên đăng nhập phải từ 3 đến 100 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;

    @NotBlank(message = "Họ và tên không được để trống")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    private String phone;
    private String dob;
    private String gender;
    private String nationality;
    private String address;
    private String cccd;
    private String cccdDate;
    private String cccdPlace;
    @Size(max = 3000000, message = "Ảnh CCCD mặt trước vượt quá dung lượng cho phép (tối đa ~2MB)")
    private String cccdFront;

    @Size(max = 3000000, message = "Ảnh CCCD mặt sau vượt quá dung lượng cho phép (tối đa ~2MB)")
    private String cccdBack;

    @Size(max = 500000, message = "Ảnh đại diện vượt quá dung lượng cho phép")
    private String avatar;
}
