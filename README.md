# Sınıf İçi Etkinlik: Paylaşımlı Not Uygulaması

Gerçek bir ekip çalışması senaryosunu simüle eden bu Android projesi, mobil uygulama geliştirmenin temel taşlarını (Authentication, NoSQL Veritabanı, Güvenlik, Modern Tasarım) uygulamalı olarak göstermek amacıyla geliştirilmiştir.

## 📱 Proje Özeti
Kullanıcıların kendi klasörlerini oluşturabildiği, bu klasörleri diğer kullanıcılarla e-posta üzerinden paylaşabildiği ve paylaşılan klasörlere ortak (collaborative) notlar ekleyebildiği bir Android uygulamasıdır. 

## ✨ Temel Özellikler
*   **Kullanıcı Kaydı & Girişi:** Firebase Authentication (E-posta/Şifre) ile güvenli giriş. Oturum durumu cihaz kapansa bile korunur.
*   **Klasör Yönetimi:** Kullanıcılar kendilerine ait klasörler oluşturabilir ve silebilir. Her klasör belirli bir kullanıcıya (Owner) aittir.
*   **Not Yönetimi (CRUD):** Klasörlerin içine başlık ve içerik alanlarıyla notlar eklenebilir, düzenlenebilir ve silinebilir. Notlarda hangi eylemin kimin (yazarın e-postası) tarafından gerçekleştirildiği şeffafça gösterilir.
*   **Paylaşma Sistemi (Collaboration):** Klasör sahibi (Owner), e-posta adresini girerek diğer kullanıcıları "Düzenleyen" (Editor) rolüyle davet edebilir. Paylaşılan klasörler davet edilen kullanıcının ana sayfasında özel bir etiketle belirir.
*   **Rol Bazlı Erişim (RBAC):**
    *   **Klasör Sahibi:** Tüm yetkiler (Okuma, Yazma, Güncelleme, Silme, ve Paylaşma).
    *   **Düzenleyen (Davetli):** Sadece klasörü görebilir, içine not ekleyebilir ve notu düzenleyebilir/silebilir. Klasörü silemez veya adını değiştiremez.

## 🛠️ Kullanılan Teknolojiler & Mimari
*   **Dil:** Kotlin 2.0+
*   **Arayüz (UI):** Jetpack Compose (Material Design 3)
*   **Mimari:** MVVM (Model-View-ViewModel)
*   **Navigasyon:** Jetpack Navigation Compose
*   **Backend Servisleri (BaaS):**
    *   **Firebase Authentication:** Kullanıcı kimlik doğrulama.
    *   **Cloud Firestore:** Gerçek zamanlı NoSQL doküman veritabanı. İşlemlerin (%100) güvenilir olması adına Firestore Transactions kullanılarak paylaşımlar güvence altına alınmıştır.

## 🔒 Güvenlik (Firestore Rules)
Uygulamanın çalışması test kurallarına dayanmaz; tamamen güvenli Firestore kuralları kullanılarak yetkisiz erişimler (okuma/yazma) sunucu tarafında engellenmiştir. Davet edilmemiş bir UID (kullanıcı kimliği) hiçbir koşulda başka birinin özel klasöründeki veya dosyalarındaki içeriği okuyamaz veya çalamaz.

## 🎨 Ekranlar (Screens)
Proje, kullanıcı dostu ve temiz bir tasarım diliyle 5 temel ekrandan oluşmaktadır:
1.  **Login/Register (Giriş/Kayıt):** Email/şifre kontrolü, yükleme (loading) durumları ve hatalı girişte uyarı (feedback) mekanizmaları.
2.  **Klasör Listesi:** Kullanıcının açtığı (Owner) veya kendisine davet gelen (Shared with you) klasörlerin bulunduğu hiyerarşik yapı. Çıkış yapma (Logout) opsiyonu.
3.  **Not Listesi:** Klasör içindeki belgelerin gösterildiği liste. Belgeler en son güncellenme (Last edited) tarihine, saatine ve kullanıcı e-postasına göre sıralanır.
4.  **Not Detay / Düzenleme:** İçeriklerin girildiği (veya silindiği) sayfa.
5.  **Paylaşma Yönetimi (Share Management):** Sadece klasör sahibinin girebildiği; yeni bir e-postanın sisteme Editor olarak davet edildiği ve listelendiği, "Transaction" kullanan yönetim odası.

## 🚀 Projeyi Çalıştırma (Kurulum)
1. Bu depoyu (Repository) bilgisayarınıza klonlayın (veya ZIP olarak indirin).
2. Android Studio'yu açıp projeyi içeri aktarın (Import).
3. Firebase Console üzerinden "SharedNotes" (veya benzeri) adında bir proje oluşturun ve Android ikonuna tıklayarak projenize ait `com.example.myapplication` paket ismini kaydedin.
4. Adımlar sırasında Firebase'in vereceği `google-services.json` dosyasını indirin ve bu dosyanızı projenizin altındaki `/app` klasörünün içine sürükleyip bırakın.
5. Firebase konsolu > **Build** altından **Authentication**'ı etkinleştirin ve "Email/Password" seçeneğini aktif yapın.
6. Yine Firebase konsolundan **Firestore Database**'i oluşturun. Projedeki dökümantasyonda yazan güvenlik kurallarını (Rules) yapıştırıp çalıştırın.
7. Android Studio'dan "Sync Project with Gradle Files" butonuna basarak kütüphaneleri hazırlayın ve uygulamayı (Run ▶) çalıştırın! 

---
_Bu kodlar öğretici ve uygulamalı senaryo pratiği çalışması amaçlıdır._
