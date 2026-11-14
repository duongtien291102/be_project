import os
import random
import datetime
from bson import ObjectId
import pymongo


def load_mongodb_uri():
    uri = os.environ.get("MONGODB_URI")
    if uri:
        return uri.strip()
    env_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), ".env")
    if os.path.exists(env_path):
        with open(env_path, "r", encoding="utf-8") as env_file:
            for line in env_file:
                line = line.strip()
                if not line or line.startswith("#") or "=" not in line:
                    continue
                key, value = line.split("=", 1)
                if key.strip() == "MONGODB_URI":
                    return value.strip().strip('"').strip("'")
    raise RuntimeError(
        "Missing MONGODB_URI environment variable. Set MONGODB_URI or define it in be_project/.env."
    )


URI = load_mongodb_uri()
client = pymongo.MongoClient(URI)
db = client["hotel_management"]

# 1. Seed Rooms (12 rooms)
ROOMS_DATA = [
    {"_id": "101", "name": "Deluxe King Elegance", "type": "Standard", "price": 750000.0, "status": "Trống", "image": "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=700&q=80", "beds": "1 Giường King lớn", "view": "Hướng hoa viên nội khu", "size": "38m²", "maxGuests": 2, "currentGuest": None},
    {"_id": "102", "name": "Superior Twin Balcony", "type": "Superior", "price": 1100000.0, "status": "Đang thuê", "image": "https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=700&q=80", "beds": "2 Giường Twin êm ái", "view": "Ban công hướng biển", "size": "45m²", "maxGuests": 2, "currentGuest": "TRẦN HẢI ĐĂNG"},
    {"_id": "103", "name": "Standard Garden View", "type": "Standard", "price": 750000.0, "status": "Trống", "image": "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=700&q=80", "beds": "1 Giường đôi", "view": "Vườn nhiệt đới", "size": "35m²", "maxGuests": 2, "currentGuest": None},
    {"_id": "104", "name": "Superior Ocean Front", "type": "Superior", "price": 1250000.0, "status": "Trống", "image": "https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=700&q=80", "beds": "2 Giường Queen", "view": "Trực diện biển", "size": "48m²", "maxGuests": 3, "currentGuest": None},
    {"_id": "201", "name": "Executive Suite Hoàng Gia", "type": "Suite", "price": 2200000.0, "status": "Đã đặt", "image": "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=700&q=80", "beds": "Giường King Hoàng gia", "view": "View đại dương toàn cảnh", "size": "68m²", "maxGuests": 3, "currentGuest": "LÊ THỊ HỒNG NHUNG"},
    {"_id": "202", "name": "Premier Family Suite", "type": "Suite", "price": 2800000.0, "status": "Đang thuê", "image": "https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=700&q=80", "beds": "1 King + 2 Single Bed", "view": "Tầng cao hướng vịnh", "size": "75m²", "maxGuests": 4, "currentGuest": "LÊ MINH KHANG"},
    {"_id": "203", "name": "Signature Ocean Suite", "type": "Suite", "price": 2500000.0, "status": "Trống", "image": "https://images.unsplash.com/photo-1591088398332-8a7791972843?auto=format&fit=crop&w=700&q=80", "beds": "1 King Bed Luxury", "view": "Bình minh vịnh biển", "size": "70m²", "maxGuests": 2, "currentGuest": None},
    {"_id": "204", "name": "Club Executive Panorama", "type": "Suite", "price": 2900000.0, "status": "Đang dọn", "image": "https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=700&q=80", "beds": "1 King Bed VIP", "view": "Góc nhìn 270 độ biển", "size": "80m²", "maxGuests": 3, "currentGuest": None},
    {"_id": "301", "name": "Presidential Ocean Villa", "type": "Villa", "price": 5800000.0, "status": "Đang thuê", "image": "https://images.unsplash.com/photo-1602002418082-a4443e081dd1?auto=format&fit=crop&w=700&q=80", "beds": "Hồ bơi riêng & 2 King Beds", "view": "Trực diện bãi biển riêng", "size": "140m²", "maxGuests": 6, "currentGuest": "DAVID NGUYỄN"},
    {"_id": "302", "name": "Royal Penthouse Skyview", "type": "Villa", "price": 8500000.0, "status": "Trống", "image": "https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=700&q=80", "beds": "Jacuzzi riêng & Phòng rượu VIP", "view": "Tầng 25 ngắm 360 độ thành phố", "size": "210m²", "maxGuests": 4, "currentGuest": None},
    {"_id": "303", "name": "Grand Beachfront Pool Villa", "type": "Villa", "price": 7200000.0, "status": "Đã đặt", "image": "https://images.unsplash.com/photo-1540541338287-41700207dee6?auto=format&fit=crop&w=700&q=80", "beds": "Private Infinity Pool & 3 King", "view": "Bãi cát trắng mịn riêng", "size": "250m²", "maxGuests": 8, "currentGuest": "HOÀNG THÙY DƯƠNG"},
    {"_id": "304", "name": "Imperial Sky Villa", "type": "Villa", "price": 9500000.0, "status": "Trống", "image": "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=700&q=80", "beds": "2 King + Sân trực thăng", "view": "Đỉnh khách sạn 360 độ", "size": "300m²", "maxGuests": 6, "currentGuest": None}
]

db.rooms.delete_many({})
for r in ROOMS_DATA:
    r["createdAt"] = datetime.datetime.utcnow()
    r["updatedAt"] = datetime.datetime.utcnow()
db.rooms.insert_many(ROOMS_DATA)
print(f"✅ Đã seed {len(ROOMS_DATA)} phòng vào MongoDB Atlas (collection 'rooms')")

# First names & Last names for generating realistic Vietnamese customer profiles
HO_DEM = ["NGUYỄN VĂN", "TRẦN HẢI", "LÊ THỊ", "PHẠM QUỐC", "HOÀNG MINH", "VŨ ĐĂNG", "ĐẶNG THỊ", "BÙI TIẾN", "ĐỖ MỸ", "PHAN THANH", "VÕ TRỌNG", "DƯƠNG TIẾN", "NGÔ BẢO", "LÝ THÁI", "MAI THỊ", "TRỊNH KIM"]
TEN = ["AN", "BÌNH", "CƯỜNG", "DŨNG", "ĐĂNG", "GIANG", "HẢI", "HÙNG", "KHOA", "KHANG", "LONG", "LINH", "NAM", "NGÂN", "PHÚC", "QUÂN", "SƠN", "THẮNG", "THẢO", "TOÀN", "TÙNG", "UYÊN", "VINH", "VIỆT"]
METHODS = ["VietQR", "Tiền mặt", "Thẻ tín dụng"]
ADDONS_LIST = [["Buffet Ăn Sáng Hoàng Gia"], ["Đưa Đón Sân Bay Khứ Hồi"], ["Buffet Ăn Sáng Hoàng Gia", "Lotus Spa VIP"], ["Lotus Spa VIP"], []]

DEFAULT_CCCD_FRONT = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=600&q=80"
DEFAULT_CCCD_BACK = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=600&q=80"

# 2. Seed 55 Bookings (vượt mức 50 yêu cầu)
BOOKINGS_LIST = []
db.bookings.delete_many({})

statuses = ["Đã hoàn thành", "Đã check-in", "Chờ check-in", "Đã hủy"]
status_weights = [0.45, 0.25, 0.20, 0.10]

random.seed(42)

for i in range(1, 56):
    b_id = f"BK-{1000 + i}"
    cust_name = f"{random.choice(HO_DEM)} {random.choice(TEN)}"
    phone = f"09{random.randint(10, 99)} {random.randint(100, 999)} {random.randint(100, 999)}"
    cccd = f"0{random.randint(10, 99)}{random.randint(1975, 2004)}{random.randint(100000, 999999)}"
    dob = f"{random.randint(1980, 2002)}-{random.randint(1, 12):02d}-{random.randint(1, 28):02d}"
    
    # Room pick
    room = random.choice(ROOMS_DATA)
    r_id = room["_id"]
    r_name = room["name"]
    r_price = room["price"]
    
    # Dates between month 8 and month 11, 2026
    m = random.randint(8, 11)
    d = random.randint(1, 25)
    nights = random.randint(1, 6)
    check_in_dt = datetime.date(2026, m, d)
    check_out_dt = check_in_dt + datetime.timedelta(days=nights)
    check_in_str = check_in_dt.strftime("%Y-%m-%d")
    check_out_str = check_out_dt.strftime("%Y-%m-%d")
    
    addons = random.choice(ADDONS_LIST)
    addon_cost = len(addons) * 450000.0
    minibar = random.choice([0.0, 120000.0, 250000.0, 450000.0])
    total_amount = (r_price * nights) + addon_cost + minibar
    
    st = random.choices(statuses, weights=status_weights)[0]
    dep_pct = 0.5
    deposit_amount = round(total_amount * dep_pct)
    dep_method = random.choice(METHODS)
    
    if st == "Đã hoàn thành":
        paid_amount = total_amount
        rem_amount = 0.0
    elif st in ["Đã check-in", "Chờ check-in"]:
        paid_amount = deposit_amount
        rem_amount = total_amount - deposit_amount
    else: # Đã hủy
        paid_amount = 0.0
        rem_amount = 0.0

    b_doc = {
        "_id": b_id,
        "id": b_id,
        "customerName": cust_name,
        "phone": phone,
        "cccd": cccd,
        "cccdDate": "2021-08-20",
        "cccdPlace": "Cục CSQLHC về TTXH",
        "dob": dob,
        "cccdFront": DEFAULT_CCCD_FRONT,
        "cccdBack": DEFAULT_CCCD_BACK,
        "roomId": r_id,
        "roomName": r_name,
        "checkIn": check_in_str,
        "checkOut": check_out_str,
        "nights": nights,
        "addons": addons,
        "minibar": minibar,
        "totalAmount": total_amount,
        "depositAmount": deposit_amount,
        "depositMethod": dep_method,
        "paidAmount": paid_amount,
        "remainingAmount": rem_amount,
        "status": st,
        "createdAt": check_in_str,
        "createdAtInstant": datetime.datetime.combine(check_in_dt, datetime.time(random.randint(8, 20), random.randint(0, 59)))
    }
    BOOKINGS_LIST.append(b_doc)

db.bookings.insert_many(BOOKINGS_LIST)
print(f"✅ Đã seed {len(BOOKINGS_LIST)} đơn đặt phòng (Bookings) vào MongoDB Atlas")

# 3. Seed 110 Transactions trải dài từ Tháng 1 đến Tháng 10 năm 2026 (vượt mức 100 yêu cầu)
db.transactions.delete_many({})
TX_LIST = []

STAFFS = ["Cổng Online VietQR", "Phạm Đăng Khoa", "Lê Hoàng Nam", "Nguyễn Thu Thảo", "Hoàng Minh Quân"]

tx_counter = 1
for month in range(1, 11):
    # Mỗi tháng tạo 11 giao dịch = 110 giao dịch
    num_tx = 11
    for k in range(num_tx):
        day = random.randint(1, 28)
        hour = random.randint(7, 22)
        minute = random.randint(0, 59)
        dt = datetime.datetime(2026, month, day, hour, minute)
        
        tx_id = f"TX-{260000 + tx_counter}"
        tx_counter += 1
        
        bk_id = f"BK-{random.randint(1001, 1055)}"
        cust_name = f"{random.choice(HO_DEM)} {random.choice(TEN)}"
        method = random.choice(METHODS)
        
        tx_types = [
            ("Đặt cọc giữ phòng 50%", random.choice([1500000.0, 2200000.0, 2900000.0, 4200000.0])),
            ("Quyết toán trả phòng hoàn tất", random.choice([2500000.0, 3600000.0, 5800000.0, 8500000.0, 14000000.0])),
            ("Thanh toán toàn bộ 100%", random.choice([4400000.0, 6800000.0, 11500000.0, 19500000.0])),
            ("Tiền phòng & Dịch vụ phát sinh", random.choice([500000.0, 950000.0, 1800000.0]))
        ]
        chosen_type, amount = random.choice(tx_types)
        staff = "Cổng Online VietQR" if method == "VietQR" else random.choice(STAFFS[1:])
        
        tx_doc = {
            "_id": tx_id,
            "id": tx_id,
            "bookingId": bk_id,
            "customerName": cust_name,
            "type": chosen_type,
            "method": method,
            "amount": amount,
            "date": dt.strftime("%d/%m/%Y %H:%M"),
            "year": 2026,
            "month": month,
            "staff": staff,
            "status": "Đã hoàn tất",
            "createdAtInstant": dt
        }
        TX_LIST.append(tx_doc)

db.transactions.insert_many(TX_LIST)
print(f"✅ Đã seed {len(TX_LIST)} giao dịch thu chi & thuê phòng từ Tháng 1 đến Tháng 10/2026 vào MongoDB Atlas")

# Summary counts in Atlas
print("\n--- KIỂM TRA TỔNG KẾT TRÊN MONGODB ATLAS ---")
for col in ["rooms", "bookings", "transactions", "users", "roles"]:
    cnt = db[col].count_documents({})
    print(f"Collection '{col}': {cnt} records")
