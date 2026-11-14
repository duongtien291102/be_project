package com.hotel.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
    private String fullName;

    @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 25, message = "Số điện thoại không được vượt quá 25 ký tự")
    private String phone;

    @Size(max = 20, message = "Ngày sinh không hợp lệ")
    private String dob;

    @Size(max = 20, message = "Giới tính không hợp lệ")
    private String gender;

    @Size(max = 80, message = "Quốc tịch quá dài")
    private String nationality;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    @Size(max = 30, message = "Số CCCD/Hộ chiếu không hợp lệ")
    private String cccd;

    @Size(max = 20, message = "Ngày cấp CCCD không hợp lệ")
    private String cccdDate;

    @Size(max = 150, message = "Nơi cấp CCCD quá dài")
    private String cccdPlace;

    @Size(max = 3000000, message = "Ảnh CCCD mặt trước vượt quá dung lượng cho phép (tối đa ~2MB)")
    private String cccdFront;

    @Size(max = 3000000, message = "Ảnh CCCD mặt sau vượt quá dung lượng cho phép (tối đa ~2MB)")
    private String cccdBack;

    @Size(max = 500000, message = "Ảnh đại diện vượt quá dung lượng cho phép")
    private String avatar;
}
