package com.hotel.backend.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id; // e.g. "TX-701"
    private String bookingId;

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String customerName;

    @NotBlank(message = "Loại/nội dung giao dịch không được để trống")
    private String type; // e.g. "Đặt cọc giữ phòng 50%", "Tiền phòng", "Thanh toán toàn bộ 100%"

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String method; // "VietQR", "Tiền mặt", "Thẻ tín dụng"

    @NotNull(message = "Số tiền giao dịch không được để trống")
    @PositiveOrZero(message = "Số tiền giao dịch không được âm")
    private Double amount;

    private String date; // "09/10/2026 14:30"
    private Integer year; // 2026
    private Integer month; // 1-10
    private String staff; // "Cổng Online VietQR", "Phạm Đăng Khoa", ...
    private String status; // "Đã hoàn tất", "Chờ xác nhận", "Thành công"
    private Instant createdAtInstant;
}
