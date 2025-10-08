import '../models/profile_model.dart';

class ProfileService {
  // Mock data (in real app, fetched from backend)
  static ProfileModel userProfile = ProfileModel(
    name: "Ayesiga Yonah",
    email: "yonah@gmail.com",
    phone: "+256 700 123456",
    location: "Kampala, Uganda",
    bio: "Passionate tech enthusiast focused on software development.",
    imagePath: "",
  );

  // Get current profile
  static ProfileModel getProfile() {
    return userProfile;
  }

  // Update profile
  static void updateProfile(ProfileModel updatedProfile) {
    userProfile = updatedProfile;
  }
}
