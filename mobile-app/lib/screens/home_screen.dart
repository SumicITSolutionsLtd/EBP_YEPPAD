import 'package:ebp_platform/screens/login_screen.dart';
import 'package:flutter/material.dart';
import 'jobs/jobs_screen.dart';
import 'learn/learn_screen.dart';
import 'mentorship/mentorship_screen.dart';
import 'skills/skills_screen.dart';
import 'community_screen.dart';
import 'profile/profile_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _selectedIndex = 0;

  // List of screens for bottom navigation
  final List<Widget> _screens = [
    const DashboardView(),
    const LearnScreen(),
    const MentorshipScreen(),
    const SkillsScreen(),
    const CommunityScreen(),
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
        selectedItemColor: Colors.teal,
        unselectedItemColor: Colors.grey,
        showUnselectedLabels: true,
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.dashboard),
            label: "Home",
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.school),
            label: "Learn",
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.people),
            label: "Mentorship",
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.lightbulb),
            label: "Skills",
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.forum),
            label: "Community",
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
      backgroundColor: const Color(0xFFF1FDFB),
      appBar: AppBar(
        backgroundColor: Colors.teal,
        elevation: 0,
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
                color: Colors.teal,
              ),
            ),
            const SizedBox(height: 10),
            const Text(
              'Explore resources to grow your entrepreneurial journey.',
              style: TextStyle(fontSize: 16, color: Colors.black54),
            ),
            const SizedBox(height: 20),

            // Featured Banner
            Container(
              height: 160,
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                gradient: const LinearGradient(
                  colors: [Color(0xFF00BFA5), Color(0xFF1DE9B6)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
              ),
              child: Stack(
                children: [
                  Align(
                    alignment: Alignment.centerLeft,
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
                ],
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
                  color: Colors.orangeAccent,
                  destination: const JobsScreen(),
                ),
                _buildTile(
                  context,
                  title: "Learn",
                  icon: Icons.school,
                  color: Colors.blueAccent,
                  destination: const LearnScreen(),
                ),
                _buildTile(
                  context,
                  title: "Mentorship",
                  icon: Icons.people,
                  color: Colors.purpleAccent,
                  destination: const MentorshipScreen(),
                ),
                _buildTile(
                  context,
                  title: "Skills",
                  icon: Icons.lightbulb,
                  color: Colors.greenAccent,
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
              style: TextStyle(
                color: Colors.teal.shade700,
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
