# Ourilang Mobile

## Overview
Ourilang là ứng dụng Android hỗ trợ học ngoại ngữ thông qua nội dung đọc thực tế, hiện sử dụng bài báo từ The Guardian. Ứng dụng kết nối với Ourilang Backend để xác thực người dùng và tải nội dung bài báo.

## Features
- Đăng ký tài khoản bằng email và mật khẩu
- Kiểm tra định dạng email và độ dài mật khẩu ở phía ứng dụng
- Đăng nhập bằng email và mật khẩu
- Gửi lại email xác nhận đăng ký
- Lưu Access Token và Refresh Token trên thiết bị
- Tự động làm mới Access Token khi request gặp lỗi xác thực và thử lại request
- Đăng xuất
- Lấy danh sách bài báo theo từng trang
- Xem chi tiết bài báo
- Hiển thị thông báo khi không tải được bài báo hoặc bài báo không còn tồn tại
- Hiển thị nhận diện “Powered by The Guardian”

## Tech Stack
- Kotlin
- Android
- Jetpack Compose
- Material 3
- AndroidX Lifecycle / ViewModel
- Kotlin Coroutines
- Kotlin Flow / StateFlow
- Retrofit
- Gson Converter
- OkHttp
- OkHttp Logging Interceptor
- DataStore Preferences
- AndroidX Browser

## Architecture
Ứng dụng được tổ chức theo các nhóm chức năng:
- `ui/`: giao diện và ViewModel quản lý trạng thái màn hình
- `network/`: định nghĩa API, cấu hình Retrofit/OkHttp, xử lý token và xác thực request
- `model/`: các data model dùng để gửi request và nhận response từ backend

Luồng dữ liệu cơ bản:

1. Người dùng thao tác trên màn hình Compose.
2. ViewModel gọi API thông qua các interface trong `network/`.
3. Retrofit gửi HTTP request đến backend.
4. Response được chuyển thành các model trong `model/`.
5. ViewModel cập nhật `StateFlow`; giao diện quan sát state và hiển thị kết quả.

## Project Structure
```text
com.langly.langly_android
├── LanglyApplication.kt
├── MainActivity.kt
├── model/
│   ├── ArticleDetail.kt
│   ├── ArticlePageResponse.kt
│   ├── ArticleSummary.kt
│   ├── AuthResponse.kt
│   ├── LoginRequest.kt
│   ├── RefreshRequest.kt
│   └── RegisterRequest.kt
├── network/
│   ├── ArticleApi.kt
│   ├── AuthApi.kt
│   ├── AuthInterceptor.kt
│   ├── RetrofitInstance.kt
│   ├── TokenAuthenticator.kt
│   └── TokenManager.kt
└── ui/
    ├── article/
    │   ├── ArticleBody.kt
    │   ├── ArticleDetailScreen.kt
    │   ├── ArticleDetailUiState.kt
    │   ├── ArticleDetailViewModel.kt
    │   ├── ArticleListScreen.kt
    │   ├── ArticleUiState.kt
    │   ├── ArticleViewModel.kt
    │   └── PoweredByGuardian.kt
    ├── login/
    │   ├── LoginScreen.kt
    │   ├── LoginUiState.kt
    │   └── LoginViewModel.kt
    ├── register/
    │   ├── RegisterScreen.kt
    │   ├── RegisterUiState.kt
    │   └── RegisterViewModel.kt
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## Authentication

### Đăng ký
- Người dùng nhập email và mật khẩu.
- Ứng dụng kiểm tra định dạng email và yêu cầu mật khẩu tối thiểu 8 ký tự.
- Ứng dụng gửi request đăng ký đến backend.
- Người dùng cần xác nhận đăng ký qua email theo quy trình của backend.

### Đăng nhập
- Người dùng gửi email và mật khẩu đến backend.
- Khi đăng nhập thành công, ứng dụng lưu Access Token và Refresh Token.
- Trạng thái xác thực được dùng để điều hướng giữa màn hình đăng nhập và nội dung ứng dụng.

### Làm mới token
- `AuthInterceptor` thêm Access Token vào request được bảo vệ.
- Khi server yêu cầu xác thực lại, `TokenAuthenticator` dùng Refresh Token để yêu cầu token mới.
- Nếu làm mới thành công, ứng dụng lưu token mới và thử lại request ban đầu.
- Nếu Refresh Token không còn hợp lệ, ứng dụng xóa token đã lưu.

### Đăng xuất
Ứng dụng gửi Refresh Token đến endpoint đăng xuất của backend và cập nhật trạng thái đăng nhập.

## Article System

### Danh sách bài báo
- `ArticleApi` gọi `GET /api/articles`.
- Danh sách được tải theo trang bằng `cursor` và `size`.
- `ArticleViewModel` quản lý trạng thái tải, lỗi, làm mới và tải thêm dữ liệu.

### Chi tiết bài báo
- `ArticleApi` gọi `GET /api/articles/{id}`.
- `ArticleDetailViewModel` quản lý trạng thái đang tải, thành công, lỗi mạng và bài báo không còn tồn tại.
- `ArticleBody` chịu trách nhiệm hiển thị phần nội dung bài báo.

## Backend Connection

Cấu hình địa chỉ backend hiện tại nằm trong `network/RetrofitInstance.kt`:

```kotlin
private const val BASE_URL = "http://10.0.2.2:8080/"
```

`10.0.2.2` là địa chỉ đặc biệt thường dùng để Android Emulator truy cập máy chủ chạy trên máy tính host. Khi chạy trên điện thoại thật hoặc môi trường triển khai khác, cần đổi sang địa chỉ backend mà thiết bị có thể truy cập được.

Các endpoint ứng dụng đang khai báo:
- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/resend-verification`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/articles`
- `GET /api/articles/{id}`

## Build Configuration
- Android application module: `app`
- Namespace / Application ID: `com.langly.langly_android`
- Minimum SDK: 26
- Target SDK: 37
- Compile SDK: 37
- Java source/target compatibility: 11
- UI toolkit: Jetpack Compose

