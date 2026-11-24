# Ourilang Mobile

## Overview
Ourilang là ứng dụng Android hỗ trợ học ngoại ngữ thông qua nội dung đọc thực tế, hiện sử dụng bài báo từ The Guardian. Ứng dụng kết nối với Ourilang Backend để xác thực người dùng và tải nội dung bài báo.

## Features

-   Đăng ký tài khoản bằng email và mật khẩu
-   Kiểm tra định dạng email và độ dài mật khẩu ở phía ứng dụng
-   Đăng nhập bằng email và mật khẩu
-   Đăng nhập Google thông qua AndroidX Browser Custom Tabs và deep link `ourilang://auth`
-   Gửi lại email xác nhận đăng ký
-   Lưu Access Token và Refresh Token trên thiết bị
-   Tự động làm mới Access Token khi request gặp lỗi xác thực và thử lại request
-   Đăng xuất
-   Lấy danh sách bài báo theo từng trang
-   Xem chi tiết bài báo
-   Hiển thị thông báo khi không tải được bài báo hoặc bài báo không còn tồn tại
-   Hiển thị nhận diện "Powered by The Guardian"
-   Chuyển đổi giữa hai khu vực Đọc và Từ đã lưu
-   Chạm vào từ hoặc nhấn giữ và kéo để chọn một từ/cụm từ trong nội dung bài báo
-   Tra cứu từ tiếng Anh bằng dữ liệu từ điển SQLite được đóng gói trong ứng dụng
-   Hỗ trợ tra cứu một số dạng biến đổi của từ và tìm từ gốc
-   Dịch cụm từ tiếng Anh sang tiếng Việt bằng Google ML Kit Translation
-   Nhập từ hoặc cụm từ thủ công để tra cứu/dịch
-   Lưu từ vựng cùng nghĩa và ghi chú trên thiết bị
-   Tìm kiếm, xem chi tiết, chỉnh sửa nghĩa/ghi chú và xóa từ đã lưu
-   Đánh dấu những từ đã lưu khi chúng xuất hiện trong nội dung bài báo

## Tech Stack

-   Kotlin
-   Android
-   Jetpack Compose
-   Material 3
-   AndroidX Lifecycle / ViewModel
-   Kotlin Coroutines
-   Kotlin Flow / StateFlow
-   Retrofit
-   Gson Converter
-   OkHttp
-   OkHttp Logging Interceptor
-   DataStore Preferences
-   AndroidX Browser
-   Room
-   SQLite (cơ sở dữ liệu từ điển cục bộ)
-   Google ML Kit Translation

## Architecture

Ứng dụng được tổ chức theo các nhóm chức năng: - `ui/`: giao diện và
ViewModel quản lý trạng thái màn hình - `network/`: định nghĩa API, cấu
hình Retrofit/OkHttp, xử lý token và xác thực request - `model/`: các
data model dùng để gửi request và nhận response từ backend -
`dictionary/`: cài đặt, đọc dữ liệu từ điển SQLite và xử lý tra cứu các
dạng từ - `data/local/`: cơ sở dữ liệu Room và DAO lưu từ vựng của người
dùng trên thiết bị - `ui/main/`: điều hướng giữa các màn hình xác thực,
Đọc và Từ đã lưu - `ui/savedwords/`: màn hình và ViewModel quản lý từ
vựng đã lưu - `ui/translate/`: giao diện tra cứu/dịch, trạng thái popup
và xử lý dịch cụm từ

Luồng dữ liệu cơ bản:

1.  Người dùng thao tác trên màn hình Compose.
2.  ViewModel gọi API thông qua các interface trong `network/`.
3.  Retrofit gửi HTTP request đến backend.
4.  Response được chuyển thành các model trong `model/`.
5.  ViewModel cập nhật `StateFlow`; giao diện quan sát state và hiển thị kết quả.

Với các chức năng từ điển và từ vựng, dữ liệu được xử lý cục bộ trên
thiết bị:

1.  Người dùng chạm hoặc chọn một đoạn văn bản trong bài báo, hoặc nhập từ/cụm từ vào ô tra cứu.
2.  Từ đơn được tra trong cơ sở dữ liệu từ điển SQLite; chức năng tra cứu có thể thử các dạng từ liên quan khi không tìm thấy kết quả chính xác.
3.  Cụm từ được dịch bằng Google ML Kit Translation sau khi mô hình ngôn ngữ cần thiết được tải về.
4.  Từ được lưu sẽ được ghi vào cơ sở dữ liệu Room (`user_vocab.db`).
5.  Màn hình Từ đã lưu đọc dữ liệu từ Room để tìm kiếm, xem, chỉnh sửa
    hoặc xóa từ.

## Project Structure

``` text
com.langly.langly_android
├── LanglyApplication.kt
├── MainActivity.kt
├── data/
│   └── local/
│       ├── AppDatabase.kt
│       ├── VocabWord.kt
│       └── VocabWordDao.kt
├── dictionary/
│   ├── BaseWord.kt
│   ├── DictionaryDatabase.kt
│   ├── DictionaryInstaller.kt
│   ├── DictionaryModels.kt
│   ├── FormAwareLookup.kt
│   └── WordForms.kt
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
    ├── main/
    │   ├── AuthScreens.kt
    │   ├── LanglyApp.kt
    │   ├── MainBars.kt
    │   ├── MainTab.kt
    │   └── ReaderTab.kt
    ├── register/
    │   ├── RegisterScreen.kt
    │   ├── RegisterUiState.kt
    │   └── RegisterViewModel.kt
    ├── savedwords/
    │   ├── SavedWordsScreen.kt
    │   └── SavedWordsViewModel.kt
    ├── theme/
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    └── translate/
        ├── FabButtonContent.kt
        ├── FullContent.kt
        ├── HandleDrag.kt
        ├── MeaningFormat.kt
        ├── PartOfSpeechLabel.kt
        ├── PhraseTranslator.kt
        ├── SwipeToSaveCard.kt
        ├── TranslateCardContent.kt
        ├── TranslateFab.kt
        └── TranslateViewModel.kt
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


### Đăng nhập Google

`LoginScreen` mở luồng đăng nhập Google bằng AndroidX Browser Custom Tabs.
-   Backend chuyển kết quả đăng nhập về ứng dụng thông qua deep link `ourilang://auth`.
-   `MainActivity` nhận các query parameter `access` và `refresh`, sau đó lưu token thông qua `TokenManager`.

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
- `ArticleBody` chuyển nội dung HTML thành văn bản có định dạng, cho phép chạm vào một từ hoặc nhấn giữ và kéo để chọn đoạn văn bản.
- Những từ đã lưu có thể được đánh dấu trong nội dung bài báo thông qua dữ liệu từ `WordPopupViewModel`.

## Dictionary & Translation

### Tra cứu từ đơn

-   Dữ liệu từ điển được đóng gói trong`app/src/main/assets/dictionary_en_vi.db`.
-   `DictionaryInstaller` cài đặt cơ sở dữ liệu từ assets vào thư mục database của ứng dụng.
-   `DictionaryDatabase` truy vấn từ, nghĩa, từ loại, ví dụ và phiên âm từ SQLite.
-   `FormAwareLookup` thử tra cứu chính xác trước, sau đó thử các dạng từ được tạo bởi `WordForms`.
-   `BaseWord` hỗ trợ tìm từ gốc khi kết quả tra cứu có dạng biến đổi.

### Dịch cụm từ

-   `PhraseTranslator` sử dụng Google ML Kit Translation với ngôn ngữ nguồn English và ngôn ngữ đích Vietnamese.
-   Nếu mô hình dịch tiếng Việt chưa có trên thiết bị, ứng dụng tải mô hình trước khi dịch.
  -   Trạng thái tải mô hình, dịch thành công hoặc lỗi được biểu diễn qua `PhraseState` trong `TranslateViewModel`.

### Giao diện tra cứu

-   `ArticleBody` gửi từ/cụm từ được chọn đến `WordPopupViewModel`.
-   Người dùng cũng có thể nhập từ hoặc cụm từ thủ công trong giao diện
    tra cứu.
-   `TranslateFab` hiển thị nút tra cứu và nội dung tra cứu dạng thẻ có
    thể mở rộng.

## Vocabulary Management

-   `AppDatabase` tạo cơ sở dữ liệu Room cục bộ tên `user_vocab.db`.
-   `VocabWord` biểu diễn một mục từ đã lưu, gồm từ, nghĩa, ghi chú và
    thời điểm lưu.
-   `VocabWordDao` cung cấp thao tác thêm, lấy danh sách, tìm kiếm, lấy
    chi tiết, cập nhật và xóa từ.
-   `WordPopupViewModel` lưu từ được tra cứu vào Room.
-   `SavedWordsViewModel` quản lý truy vấn tìm kiếm, mục từ đang xem,
    cập nhật nghĩa/ghi chú và xóa từ.
-   `SavedWordsScreen` hiển thị danh sách từ đã lưu và màn hình chi tiết
    từ.

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


