package com.hotel.backend.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking {
    @Id
    private String id; // e.g. "BK-1082"

    private String userId;
    private String username;

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String customerName;

    private String phone;
    private String cccd;
    private String cccdDate;
    private String cccdPlace;
    private String dob;

    @Size(max = 3000000, message = "Ảnh CCCD mặt trước vượt quá dung lượng cho phép")
    private String cccdFront;

    @Size(max = 3000000, message = "Ảnh CCCD mặt sau vượt quá dung lượng cho phép")
    private String cccdBack;

    @NotBlank(message = "Mã phòng không được để trống")
    private String roomId;

    private String roomName;

    @NotBlank(message = "Ngày nhận phòng không được để trống")
    private String checkIn;

    @NotBlank(message = "Ngày trả phòng không được để trống")
    private String checkOut;

    @PositiveOrZero(message = "Số đêm không được âm")
    private Integer nights;

    private List<String> addons;

    @PositiveOrZero(message = "Phí minibar không được âm")
    private Double minibar;

    @PositiveOrZero(message = "Tổng tiền không được âm")
    private Double totalAmount;

    @PositiveOrZero(message = "Tiền cọc không được âm")
    private Double depositAmount;

    private String depositMethod;

    @PositiveOrZero(message = "Số tiền đã thanh toán không được âm")
    private Double paidAmount;

    @PositiveOrZero(message = "Số tiền còn lại không được âm")
    private Double remainingAmount;

    private String status; // "Chờ check-in", "Đã check-in", "Đã hoàn thành", "Đã hủy"
    private String createdAt;
    private Instant createdAtInstant;
}
