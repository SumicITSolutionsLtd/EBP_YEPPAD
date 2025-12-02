import 'package:flutter/material.dart';
import 'package:device_preview/device_preview.dart'; // 👈 added
import 'package:flutter/foundation.dart'; // 👈 added for kReleaseMode
import 'jobs/jobs_screen.dart';
import 'mentorship/mentorship_screen.dart';
import 'skills/skills_screen.dart';
import 'community_screen.dart';
import 'profile/profile_screen.dart';

/// 👇 main() entry point with DevicePreview wrapper
void main() {
  runApp(
    DevicePreview(
      enabled: !kReleaseMode, // 👈 shows toolbar in debug, hides in release
      builder: (context) => const MyApp(),
    ),
  );
}

/// 👇 MyApp root widget holding MaterialApp
class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      useInheritedMediaQuery: true, // 👈 important for responsiveness
      locale: DevicePreview.locale(context), // 👈 allows locale simulation
      builder: DevicePreview.appBuilder, // 👈 wraps app with preview
      debugShowCheckedModeBanner: false,
      home: const HomeScreen(), // 👈 your existing HomeScreen
    );
  }
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _selectedIndex = 0;

  final List<Widget> _screens = [
    const DashboardView(),
    const CommunityScreen(),
    const MentorshipScreen(),
    const SkillsScreen(),
    const ProfileScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _screens[_selectedIndex],
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: _selectedIndex,
        onTap: (index) {
          setState(() => _selectedIndex = index);
        },
        backgroundColor: Colors.white,
        selectedItemColor: const Color(0xFF003C9E), // Deep Blue
        unselectedItemColor: Colors.grey,
        showUnselectedLabels: true,
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.dashboard),
            label: "Home",
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.forum),
            label: "Community",
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.people),
            label: "Mentorship",
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.lightbulb),
            label: "My Skills",
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person),
            label: "Profile",
          ),
        ],
      ),
    );
  }
}

/// Dashboard view (Main Home tab)
class DashboardView extends StatelessWidget {
  const DashboardView({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white, // Clean background
      appBar: AppBar(
        backgroundColor: const Color(0xFF003C9E), // Deep Blue
        elevation: 0,
        automaticallyImplyLeading: false,
        title: const Text(
          'Entrepreneurship Booster',
          style: TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
          ),
        ),
        centerTitle: true,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            const Text(
              'Welcome Back 👋',
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.bold,
                color: Color(0xFF003C9E), // Deep Blue
              ),
            ),
            const SizedBox(height: 10),
            const Text(
              'Explore resources to grow your entrepreneurial journey.',
              style: TextStyle(
                fontSize: 16,
                color: Color(0xFF002F6C), // Dark Navy Blue
              ),
            ),
            const SizedBox(height: 20),

            // Featured Banner
            Container(
              height: 160,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                gradient: const LinearGradient(
                  colors: [Color(0xFFF28A2E), Color(0xFF005ECF)], // Bold Orange + Royal Blue
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
              ),
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: const [
                    Text(
                      "Boost Your Skills 🚀",
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    SizedBox(height: 8),
                    Text(
                      "Access free business training and mentorship",
                      style: TextStyle(
                        color: Colors.white70,
                        fontSize: 14,
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 30),

            // Quick Access Tiles
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              crossAxisSpacing: 16,
              mainAxisSpacing: 16,
              physics: const NeverScrollableScrollPhysics(),
              children: [
                _buildTile(
                  context,
                  title: "Jobs",
                  icon: Icons.work_outline,
                  color: const Color(0xFFF28A2E), // Bold Orange
                  destination: const JobsScreen(),
                ),
                _buildTile(
                  context,
                  title: "Community",
                  icon: Icons.forum,
                  color: const Color(0xFF005ECF), // Royal Blue
                  destination: const CommunityScreen(),
                ),
                _buildTile(
                  context,
                  title: "Mentorship",
                  icon: Icons.people,
                  color: const Color(0xFF00AEEF), // Bright Blue
                  destination: const MentorshipScreen(),
                ),
                _buildTile(
                  context,
                  title: "My Skills",
                  icon: Icons.lightbulb,
                  color: const Color(0xFF003C9E), // Deep Blue
                  destination: const SkillsScreen(),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  // Helper Widget: Dashboard Tile
  Widget _buildTile(BuildContext context,
      {required String title,
        required IconData icon,
        required Color color,
        required Widget destination}) {
    return InkWell(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(builder: (_) => destination),
        );
      },
      borderRadius: BorderRadius.circular(16),
      child: Container(
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, color: color, size: 40),
            const SizedBox(height: 10),
            Text(
              title,
              style: const TextStyle(
                color: Color(0xFF002F6C), // Dark Navy Blue for labels
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
