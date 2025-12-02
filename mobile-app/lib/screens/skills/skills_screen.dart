import 'package:flutter/material.dart';

const Color kSkillsColor = Color(0xFFF28A2E); // Orange accent

class SkillsScreen extends StatelessWidget {
  const SkillsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: kSkillsColor,
        title: const Text(
          "My Skills",
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        centerTitle: true,
      ),
      backgroundColor: Colors.white,
      body: SafeArea(
        child: SingleChildScrollView( // ✅ scrollable for landscape
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Icon(
                Icons.build, // 🛠️ icon for skills
                size: MediaQuery.of(context).size.height * 0.12,
                color: kSkillsColor,
              ),
              const SizedBox(height: 20),
              const Text(
                "Skills Coming Soon!",
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF003C9E), // Deep Blue
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 12),
              const Text(
                "We are preparing a full skills showcasing experience:\n\n"
                    "• Showcase your talents\n"
                    "• Add certifications\n"
                    "• Track growth and progress\n"
                    "• Share achievements with peers\n\n"
                    "Stay tuned for Version 2!",
                style: TextStyle(
                  fontSize: 16,
                  color: Color(0xFF002F6C), // Navy Blue
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 30),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
                decoration: BoxDecoration(
                  color: kSkillsColor.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: const Text(
                  "Coming Soon...",
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                    color: kSkillsColor,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
