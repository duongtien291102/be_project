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
@Document(collection = "rooms")
public class Room {
    @Id
    @NotBlank(message = "Số phòng không được để trống")
    private String id; // e.g. "101", "102"

    @NotBlank(message = "Tên phòng không được để trống")
    private String name;

    @NotBlank(message = "Hạng phòng không được để trống")
    private String type; // "Standard", "Superior", "Suite", "Villa"

    @NotNull(message = "Giá phòng không được để trống")
    @PositiveOrZero(message = "Giá phòng không được âm")
    private Double price;

    @NotBlank(message = "Trạng thái phòng không được để trống")
    private String status; // "Trống", "Đang thuê", "Đã đặt", "Đang dọn"

    private String image;
    private String beds;
    private String view;
    private String size;

    @PositiveOrZero(message = "Số khách tối đa không được âm")
    private Integer maxGuests;

    private String currentGuest;
    private Instant createdAt;
    private Instant updatedAt;
}
