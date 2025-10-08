import 'package:flutter/material.dart';
import '../../models/profile_model.dart';
import '../../services/profile_service.dart';

class EditProfileScreen extends StatefulWidget {
  final ProfileModel profile;

  const EditProfileScreen({super.key, required this.profile});

  @override
  State<EditProfileScreen> createState() => _EditProfileScreenState();
}

class _EditProfileScreenState extends State<EditProfileScreen> {
  late TextEditingController _nameController;
  late TextEditingController _emailController;
  late TextEditingController _phoneController;
  late TextEditingController _locationController;
  late TextEditingController _bioController;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.profile.name);
    _emailController = TextEditingController(text: widget.profile.email);
    _phoneController = TextEditingController(text: widget.profile.phone);
    _locationController = TextEditingController(text: widget.profile.location);
    _bioController = TextEditingController(text: widget.profile.bio);
  }

  void _saveProfile() {
    final updatedProfile = ProfileModel(
      name: _nameController.text,
      email: _emailController.text,
      phone: _phoneController.text,
      location: _locationController.text,
      bio: _bioController.text,
      imagePath: widget.profile.imagePath,
    );

    ProfileService.updateProfile(updatedProfile);
    Navigator.pop(context, updatedProfile);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Edit Profile"),
        backgroundColor: Colors.teal,
        centerTitle: true,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            _buildTextField("Full Name", _nameController),
            const SizedBox(height: 10),
            _buildTextField("Email", _emailController),
            const SizedBox(height: 10),
            _buildTextField("Phone", _phoneController),
            const SizedBox(height: 10),
            _buildTextField("Location", _locationController),
            const SizedBox(height: 10),
            _buildTextField("Bio", _bioController, maxLines: 3),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: _saveProfile,
              style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.teal,
                  padding: const EdgeInsets.symmetric(vertical: 14),
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12))),
              child: const Text("Save Changes",
                  style: TextStyle(fontSize: 18, color: Colors.white)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTextField(String label, TextEditingController controller,
      {int maxLines = 1}) {
    return TextField(
      controller: controller,
      maxLines: maxLines,
      decoration: InputDecoration(
        labelText: label,
        filled: true,
        fillColor: Colors.white,
        border:
        OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
      ),
    );
  }
}
