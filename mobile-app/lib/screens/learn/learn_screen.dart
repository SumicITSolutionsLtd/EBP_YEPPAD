// lib/screens/learn/learn_screen.dart
import 'package:flutter/material.dart';
import 'course_model.dart';
import 'course_card.dart';
import 'course_detail_screen.dart';

class LearnScreen extends StatefulWidget {
  const LearnScreen({super.key});

  @override
  State<LearnScreen> createState() => _LearnScreenState();
}

class _LearnScreenState extends State<LearnScreen> {
  final List<Course> courses = [
    Course(
      id: 'c1',
      title: 'Starting a Small Business',
      description: 'Short audio lessons on starting your first business.',
      duration: '1h 20m',
      imageUrl: 'https://via.placeholder.com/300x200',
      audioUrl: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
      videoUrl: null,
    ),
    Course(
      id: 'c2',
      title: 'Basic Accounting',
      description: 'Overview of bookkeeping and cashflow management.',
      duration: '50m',
      imageUrl: 'https://via.placeholder.com/300x200',
      audioUrl: null,
      videoUrl: 'https://samplelib.com/lib/preview/mp4/sample-5s.mp4',
    ),
  ];

  final categories = ['All', 'Business', 'Finance', 'Marketing', 'Skills'];
  String selectedCategory = 'All';
  String query = '';

  @override
  Widget build(BuildContext context) {
    final filtered = courses.where((c) {
      final matchesSearch = c.title.toLowerCase().contains(query.toLowerCase()) || c.description.toLowerCase().contains(query.toLowerCase());
      final matchesCategory = selectedCategory == 'All' || c.title.toLowerCase().contains(selectedCategory.toLowerCase());
      return matchesSearch && matchesCategory;
    }).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Learn'),
        backgroundColor: Colors.blueAccent,
        leading: IconButton(icon: const Icon(Icons.arrow_back), onPressed: () => Navigator.pop(context)),
      ),
      body: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            TextField(
              decoration: InputDecoration(prefixIcon: const Icon(Icons.search), hintText: 'Search courses...', border: OutlineInputBorder(borderRadius: BorderRadius.circular(10))),
              onChanged: (v) => setState(() => query = v),
            ),
            const SizedBox(height: 12),
            SizedBox(
              height: 42,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: categories.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (context, i) {
                  final cat = categories[i];
                  final isSel = cat == selectedCategory;
                  return ChoiceChip(label: Text(cat), selected: isSel, onSelected: (_) => setState(()=> selectedCategory = cat));
                },
              ),
            ),
            const SizedBox(height: 12),
            Expanded(
              child: filtered.isEmpty ? const Center(child: Text('No courses found.')) : ListView.builder(
                itemCount: filtered.length,
                itemBuilder: (context, i) {
                  final course = filtered[i];
                  return CourseCard(course: course, onStart: () {
                    Navigator.push(context, MaterialPageRoute(builder: (_) => CourseDetailScreen(course: course)));
                  });
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
