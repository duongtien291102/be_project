package com.hotel.backend.models;

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
    private String customerName;
    private String type; // e.g. "Đặt cọc giữ phòng 50%", "Tiền phòng", "Thanh toán toàn bộ 100%"
    private String method; // "VietQR", "Tiền mặt", "Thẻ tín dụng"
    private Double amount;
    private String date; // "09/10/2026 14:30"
    private Integer year; // 2026
    private Integer month; // 1-10
    private String staff; // "Cổng Online VietQR", "Phạm Đăng Khoa", ...
    private String status; // "Đã hoàn tất", "Thành công"
    private Instant createdAtInstant;
}
