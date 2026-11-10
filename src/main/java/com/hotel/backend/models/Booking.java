package com.hotel.backend.models;

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
    private String customerName;
    private String phone;
    private String cccd;
    private String cccdDate;
    private String cccdPlace;
    private String dob;
    private String cccdFront;
    private String cccdBack;
    private String roomId;
    private String roomName;
    private String checkIn;
    private String checkOut;
    private Integer nights;
    private List<String> addons;
    private Double minibar;
    private Double totalAmount;
    private Double depositAmount;
    private String depositMethod;
    private Double paidAmount;
    private Double remainingAmount;
    private String status; // "Chờ check-in", "Đã check-in", "Đã hoàn thành", "Đã hủy"
    private String createdAt;
    private Instant createdAtInstant;
}
