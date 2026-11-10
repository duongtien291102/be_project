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
@Document(collection = "rooms")
public class Room {
    @Id
    private String id; // e.g. "101", "102"
    private String name;
    private String type; // "Standard", "Superior", "Suite", "Villa"
    private Double price;
    private String status; // "Trống", "Đang thuê", "Đã đặt", "Đang dọn"
    private String image;
    private String beds;
    private String view;
    private String size;
    private Integer maxGuests;
    private String currentGuest;
    private Instant createdAt;
    private Instant updatedAt;
}
