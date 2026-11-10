package com.hotel.backend.controllers;

import com.hotel.backend.dto.ApiResponse;
import com.hotel.backend.models.Booking;
import com.hotel.backend.models.Role;
import com.hotel.backend.models.Room;
import com.hotel.backend.models.Transaction;
import com.hotel.backend.models.User;
import com.hotel.backend.repositories.BookingRepository;
import com.hotel.backend.repositories.RoleRepository;
import com.hotel.backend.repositories.RoomRepository;
import com.hotel.backend.repositories.TransactionRepository;
import com.hotel.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicApiController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<Room>>> getRooms() {
        return ResponseEntity.ok(ApiResponse.ok(roomRepository.findAll()));
    }

    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<Room>> createOrUpdateRoom(@RequestBody Room room) {
        if (room.getCreatedAt() == null) room.setCreatedAt(Instant.now());
        room.setUpdatedAt(Instant.now());
        Room saved = roomRepository.save(room);
        return ResponseEntity.ok(ApiResponse.ok("Lưu thông tin phòng thành công", saved));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable String id) {
        roomRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa phòng thành công", null));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookings() {
        return ResponseEntity.ok(ApiResponse.ok(bookingRepository.findAll()));
    }

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<Booking>> createBooking(@RequestBody Booking booking) {
        if (booking.getId() == null || booking.getId().isEmpty()) {
            booking.setId("BK-" + ((int)(1000 + Math.random() * 9000)));
        }
        if (booking.getCreatedAtInstant() == null) {
            booking.setCreatedAtInstant(Instant.now());
        }
        Booking saved = bookingRepository.save(booking);

        // Update room status if reserved/occupied
        if (booking.getRoomId() != null) {
            roomRepository.findById(booking.getRoomId()).ifPresent(room -> {
                if ("Đã check-in".equals(booking.getStatus())) {
                    room.setStatus("Đang thuê");
                    room.setCurrentGuest(booking.getCustomerName());
                } else if ("Chờ check-in".equals(booking.getStatus())) {
                    room.setStatus("Đã đặt");
                    room.setCurrentGuest(booking.getCustomerName());
                }
                roomRepository.save(room);
            });
        }

        return ResponseEntity.ok(ApiResponse.ok("Đặt phòng thành công", saved));
    }

    @PutMapping("/bookings/{id}/status")
    public ResponseEntity<ApiResponse<Booking>> updateBookingStatus(
            @PathVariable String id,
            @RequestParam String status) {
        return bookingRepository.findById(id).map(booking -> {
            booking.setStatus(status);
            bookingRepository.save(booking);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thành công", booking));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable String id) {
        bookingRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa đơn đặt phòng", null));
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactions() {
        return ResponseEntity.ok(ApiResponse.ok(transactionRepository.findAll()));
    }

    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<Transaction>> createTransaction(@RequestBody Transaction tx) {
        if (tx.getId() == null || tx.getId().isEmpty()) {
            tx.setId("TX-" + ((int)(100 + Math.random() * 900)));
        }
        if (tx.getCreatedAtInstant() == null) {
            tx.setCreatedAtInstant(Instant.now());
        }
        Transaction saved = transactionRepository.save(tx);
        return ResponseEntity.ok(ApiResponse.ok("Tạo giao dịch thu chi thành công", saved));
    }

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getRoles() {
        return ResponseEntity.ok(ApiResponse.ok(roleRepository.findAll()));
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<ApiResponse<Role>> updateRole(@PathVariable String id, @RequestBody Role role) {
        role.setId(id);
        Role saved = roleRepository.save(role);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật vai trò phân quyền thành công", saved));
    }

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<User>>> getStaff() {
        return ResponseEntity.ok(ApiResponse.ok(userRepository.findAll()));
    }

    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<User>> saveStaff(@RequestBody User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId("NV-" + ((int)(100 + Math.random() * 900)));
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(Instant.now());
        }
        user.setUpdatedAt(Instant.now());
        if (user.getName() != null && user.getFullName() == null) {
            user.setFullName(user.getName());
        }
        User saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Lưu thông tin nhân sự thành công", saved));
    }

    @PatchMapping("/staff/{id}/toggle-status")
    public ResponseEntity<ApiResponse<User>> toggleStaffStatus(@PathVariable String id) {
        return userRepository.findById(id).map(user -> {
            boolean newActive = !user.isActive();
            user.setActive(newActive);
            user.setStatus(newActive ? "active" : "locked");
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái nhân sự thành công", user));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa nhân viên thành công", null));
    }
}
