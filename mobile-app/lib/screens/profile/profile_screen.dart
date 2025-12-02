import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../../models/profile_model.dart';
import '../../services/profile_service.dart';
import 'edit_profile_screen.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  late ProfileModel profile;
  File? _image;

  @override
  void initState() {
    super.initState();
    profile = ProfileService.getProfile();
  }

  Future<void> _pickProfileImage() async {
    final picked = await ImagePicker().pickImage(source: ImageSource.gallery);
    if (picked != null) {
      setState(() {
        _image = File(picked.path);
        profile.imagePath = picked.path;
      });
      ProfileService.updateProfile(profile);
    }
  }

  void _navigateToEditProfile() async {
    final updatedProfile = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => EditProfileScreen(profile: profile),
      ),
    );

    if (updatedProfile != null && updatedProfile is ProfileModel) {
      setState(() {
        profile = updatedProfile;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white, // clean background
      appBar: AppBar(
        title: const Text("My Profile"),
        backgroundColor: const Color(0xFF003C9E), // Deep Blue
        elevation: 0,
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Profile Picture
            GestureDetector(
              onTap: _pickProfileImage,
              child: Stack(
                children: [
                  CircleAvatar(
                    radius: 55,
                    backgroundColor: const Color(0xFF00AEEF).withOpacity(0.2), // Bright Blue tint
                    backgroundImage: _image != null
                        ? FileImage(_image!)
                        : (profile.imagePath.isNotEmpty
                        ? FileImage(File(profile.imagePath))
                        : null),
                    child: profile.imagePath.isEmpty && _image == null
                        ? const Icon(Icons.person, size: 55, color: Color(0xFF003C9E)) // Deep Blue
                        : null,
                  ),
                  Positioned(
                    bottom: 0,
                    right: 0,
                    child: CircleAvatar(
                      backgroundColor: const Color(0xFF003C9E), // Deep Blue
                      radius: 18,
                      child: const Icon(Icons.camera_alt,
                          color: Colors.white, size: 18),
                    ),
                  )
                ],
              ),
            ),
            const SizedBox(height: 12),

            Text(
              profile.name,
              style: const TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.bold,
                color: Color(0xFF003C9E), // Deep Blue
              ),
            ),
            Text(
              profile.location,
              style: const TextStyle(
                color: Color(0xFF002F6C), // Dark Navy Blue
                fontSize: 14,
              ),
            ),
            const SizedBox(height: 8),

            ElevatedButton.icon(
              onPressed: _navigateToEditProfile,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFF28A2E), // Bold Orange
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(30),
                ),
              ),
              icon: const Icon(Icons.edit, color: Colors.white),
              label: const Text("Edit Profile",
                  style: TextStyle(color: Colors.white)),
            ),

            const SizedBox(height: 20),

            // About Me Section
            const Align(
              alignment: Alignment.centerLeft,
              child: Text(
                "About Me",
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF003C9E), // Deep Blue
                ),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              profile.bio,
              style: const TextStyle(
                fontSize: 15,
                color: Color(0xFF002F6C), // Dark Navy Blue
              ),
            ),

            const SizedBox(height: 20),

            // Contact Info
            Card(
              elevation: 1,
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16)),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      "Contact Info",
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 16,
                        color: Color(0xFF003C9E), // Deep Blue
                      ),
                    ),
                    const SizedBox(height: 8),
                    ListTile(
                      leading: const Icon(Icons.email, color: Color(0xFF00AEEF)), // Bright Blue
                      title: Text(profile.email),
                    ),
                    ListTile(
                      leading: const Icon(Icons.phone, color: Color(0xFF005ECF)), // Royal Blue
                      title: Text(profile.phone),
                    ),
                    ListTile(
                      leading: const Icon(Icons.location_on, color: Color(0xFFF28A2E)), // Bold Orange
                      title: Text(profile.location),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            // Logout
            ElevatedButton.icon(
              onPressed: () {},
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.redAccent, // keep red for logout
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(30),
                ),
              ),
              icon: const Icon(Icons.logout, color: Colors.white),
              label: const Text("Logout",
                  style: TextStyle(color: Colors.white)),
            ),
          ],
        ),
      ),
    );
  }
}
