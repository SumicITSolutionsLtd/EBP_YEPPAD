import 'package:flutter/material.dart';

const Color kMentorshipColor = Color(0xFF00AEEF); // Bright Blue

class MentorshipScreen extends StatelessWidget {
  const MentorshipScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: kMentorshipColor,
        title: const Text(
          "Mentorship",
          style: TextStyle(
            fontWeight: FontWeight.bold,
            color: Colors.white,
          ),
        ),
        centerTitle: true,
      ),
      backgroundColor: Colors.white,
      body: SafeArea(
        child: SingleChildScrollView( // ✅ allows scrolling if content overflows
          padding: const EdgeInsets.all(24.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Icon(
                Icons.people,
                size: MediaQuery.of(context).size.height * 0.12, // ✅ dynamic sizing
                color: kMentorshipColor,
              ),
              const SizedBox(height: 20),
              const Text(
                "Mentorship Coming Soon!",
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF003C9E),
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 12),
              const Text(
                "We are preparing a full mentorship experience:\n\n"
                    "• Expert mentors\n"
                    "• Business guidance\n"
                    "• Practical sessions\n"
                    "• One-on-one coaching\n\n"
                    "Stay tuned for Version 2!",
                style: TextStyle(
                  fontSize: 16,
                  color: Color(0xFF002F6C),
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 30),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
                decoration: BoxDecoration(
                  color: kMentorshipColor.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: const Text(
                  "Coming Soon...",
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF00AEEF),
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
