// lib/services/auth_service.dart
class AuthService {
  // In-memory flag for demo; later connect with real auth & token clearing
  static bool isLoggedIn = true;

  static void login() {
    isLoggedIn = true;
  }

  static void logout() {
    isLoggedIn = false;
    // later: clear tokens, shared prefs, etc.
  }
}
