import 'dart:async';
import 'package:flutter/material.dart';
import 'package:smooth_page_indicator/smooth_page_indicator.dart';
import 'login_screen.dart';

class OnboardingScreen extends StatefulWidget {
  @override
  _OnboardingScreenState createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends State<OnboardingScreen> {
  final PageController _controller = PageController();
  int currentPage = 0;
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _timer = Timer.periodic(const Duration(seconds: 4), (Timer timer) {
        if (currentPage < 3) {
          currentPage++;
        } else {
          currentPage = 0;
        }
        if (_controller.hasClients) {
          _controller.animateToPage(
            currentPage,
            duration: const Duration(milliseconds: 500),
            curve: Curves.easeInOut,
          );
        }
      });
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;

    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Column(
          children: [
            // PageView takes available space
            Expanded(
              child: PageView(
                controller: _controller,
                onPageChanged: (index) {
                  setState(() {
                    currentPage = index;
                  });
                },
                children: [
                  buildIntroPage(),
                  buildPage(
                    image: "assets/images/jobs.png",
                    title: "Explore Job Opportunities",
                    subtitle: "Find exciting job listings tailored to your goals.",
                  ),
                  buildPage(
                    image: "assets/images/mentors.png",
                    title: "Connect with Mentors",
                    subtitle: "Get guidance and insights from professionals.",
                  ),
                  buildPage(
                    image: "assets/images/skills.png",
                    title: "Showcase Your Skills",
                    subtitle: "Highlight your strengths and grow your career.",
                  ),
                ],
              ),
            ),

            const SizedBox(height: 20),

            // Page indicator
            SmoothPageIndicator(
              controller: _controller,
              count: 4,
              effect: const ExpandingDotsEffect(
                activeDotColor: Color(0xFFF28A2E), // Orange accent
                dotColor: Color(0xFF005ECF),        // Blue accent
                dotHeight: 10,
                dotWidth: 10,
              ),
            ),

            const SizedBox(height: 20),

            // Skip & Next buttons with opposite alignment
            Padding(
              padding: EdgeInsets.symmetric(horizontal: screenWidth * 0.08),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  TextButton(
                    onPressed: () {
                      Navigator.pushReplacement(
                        context,
                        MaterialPageRoute(builder: (context) => LoginScreen()),
                      );
                    },
                    child: const Text(
                      "Skip",
                      style: TextStyle(color: Color(0xFF005ECF)), // Blue
                    ),
                  ),
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFFF28A2E), // Orange
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(20),
                      ),
                    ),
                    onPressed: () {
                      if (currentPage == 3) {
                        Navigator.pushReplacement(
                          context,
                          MaterialPageRoute(builder: (context) => LoginScreen()),
                        );
                      } else {
                        _controller.nextPage(
                          duration: const Duration(milliseconds: 500),
                          curve: Curves.easeInOut,
                        );
                      }
                    },
                    child: const Text("Next"),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }

  Widget buildIntroPage() {
    return SingleChildScrollView(
      child: Padding(
        padding: const EdgeInsets.all(40.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Image.asset(
              'assets/images/logo.png',
              height: MediaQuery.of(context).size.height * 0.20,
              width: MediaQuery.of(context).size.width * 0.40,
              fit: BoxFit.contain,
            ),
            const SizedBox(height: 30),
            const Text(
              "Welcome to Entrepreneurship Booster",
              style: TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: Color(0xFF003C9E), // Deep Blue
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 15),
            const Text(
              "Empowering youth with mentorship, skills, and job opportunities to build a brighter future.",
              style: TextStyle(
                fontSize: 16,
                color: Color(0xFF002F6C), // Navy Blue
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  Widget buildPage({required String image, required String title, required String subtitle}) {
    return SingleChildScrollView(
      child: Padding(
        padding: const EdgeInsets.all(40.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Image.asset(image, height: MediaQuery.of(context).size.height * 0.25),
            const SizedBox(height: 30),
            Text(
              title,
              style: const TextStyle(
                fontSize: 24,
                fontWeight: FontWeight.bold,
                color: Color(0xFF003C9E), // Deep Blue
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 15),
            Text(
              subtitle,
              style: const TextStyle(
                fontSize: 16,
                color: Color(0xFF002F6C), // Navy Blue
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
