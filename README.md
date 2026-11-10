# Grand Hotel & Resorts - Backend Service (Spring Boot & MongoDB)

Hệ thống Backend API quản lý khách sạn và phân quyền bảo mật nâng cao (RBAC) được xây dựng trên nền tảng **Java 21**, **Spring Boot**, **Spring Security 6**, **JWT (JSON Web Token)** và cơ sở dữ liệu đám mây **MongoDB Atlas**.

---

## 🚀 Tính Năng Đã Triển Khai (Phase 1: Authentication & Authorization)

1. **Khởi tạo tự động tài khoản SysAdmin (`ROLE_SYSADMIN`):**
   - Tài khoản có cấp độ phân quyền cao nhất trong toàn bộ hệ thống.
   - Có đặc quyền **trao quyền (grant roles)** cho bất kỳ tài khoản người dùng nào.
   - Có đặc quyền **cấu hình ma trận quyền hạn (permissions matrix)** cho tất cả các Role (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_RECEPTIONIST`, `ROLE_CASHIER`, `ROLE_HOUSEKEEPING`, `ROLE_USER`).

2. **Cơ chế Đăng Ký Tài Khoản Thường:**
   - Mọi tài khoản tạo mới thông thường qua API `/api/auth/register` đều mặc định được cấp quyền **`ROLE_USER`**.

3. **Bảo Mật & Xác Thực Chuẩn Doanh Nghiệp:**
   - Mật khẩu được mã hóa an toàn bằng thuật toán **BCrypt**.
   - Xác thực người dùng không trạng thái (Stateless) qua **JWT Bearer Token** (chuẩn HMAC-SHA384).
   - Kiểm soát truy cập phân tầng (Method Security: `@PreAuthorize`).
   - Hỗ trợ CORS đa nguồn (tương thích máy chủ frontend tại port 5555, 5173,...).

---

## 🔑 Thông Tin Tài Khoản Mặc Định

### Tài Khoản SysAdmin:
- **Tên đăng nhập:** `sysadmin`
- **Mật khẩu:** `Sysadmin@123`
- **Email:** `sysadmin@grandhotel.vn`
- **Vai trò:** `ROLE_SYSADMIN`, `ROLE_ADMIN`
- **Quyền hạn:** Toàn quyền quản trị, trao quyền cho các role và tài khoản khác.

---

## 🗄️ Cấu Hình Cơ Sở Dữ Liệu (MongoDB Atlas)

- **Database:** `hotel_management` trên MongoDB Atlas Cluster `project4.q50m2yq.mongodb.net`
- **URI Kết nối:** `mongodb+srv://duongtien291102_db_user:yFuf6uohtENtSUMG@project4.q50m2yq.mongodb.net/hotel_management?retryWrites=true&w=majority`

---

## 📡 Danh Sách API Điển Hình

### 1. Đăng Nhập Hệ Thống
- **Endpoint:** `POST /api/auth/login`
- **Body:**
```json
{
  "usernameOrEmail": "sysadmin",
  "password": "Sysadmin@123"
}
```
- **Phản hồi:** Trả về JWT token, thông tin người dùng và danh sách quyền hạn hiệu lực (effective permissions).

### 2. Đăng Ký Tài Khoản Mới (Mặc định `ROLE_USER`)
- **Endpoint:** `POST /api/auth/register`
- **Body:**
```json
{
  "username": "khachhang01",
  "password": "User@123456",
  "fullName": "Nguyễn Văn Khách",
  "email": "khachhang01@gmail.com",
  "phone": "0912345678"
}
```

### 3. Lấy Thông Tin Cá Nhân Đang Đăng Nhập
- **Endpoint:** `GET /api/auth/me`
- **Header:** `Authorization: Bearer <token>`

### 4. SysAdmin Trao Quyền / Gán Vai Trò Cho Người Dùng
- **Endpoint:** `POST /api/admin/roles/grant-to-user/{userId}`
- **Header:** `Authorization: Bearer <sysadmin_token>`
- **Body:**
```json
{
  "roles": ["ROLE_MANAGER", "ROLE_ADMIN"]
}
```

### 5. SysAdmin Cập Nhật Quyền Hạn Cho Một Vai Trò (Role)
- **Endpoint:** `PUT /api/admin/roles/{roleId}/permissions`
- **Header:** `Authorization: Bearer <sysadmin_token>`
- **Body:**
```json
{
  "permissions": {
    "special_discount_access": true,
    "export_reports": true
  }
}
```

### 6. Lấy Danh Sách Tất Cả Tài Khoản & Danh Sách Role
- `GET /api/admin/users` (Yêu cầu quyền SysAdmin hoặc Admin)
- `GET /api/admin/roles` (Yêu cầu quyền SysAdmin hoặc Admin)

---

## 🛠️ Hướng Dẫn Chạy Dự Án

### Yêu cầu:
- Java JDK 21 trở lên.

### Khởi chạy:
```bash
./mvnw.cmd spring-boot:run
```
Máy chủ backend sẽ chạy tại: **http://localhost:8080**
