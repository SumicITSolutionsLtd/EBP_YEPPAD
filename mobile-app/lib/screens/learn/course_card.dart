// lib/screens/learn/course_card.dart
import 'package:flutter/material.dart';
import 'course_model.dart';

class CourseCard extends StatelessWidget {
  final Course course;
  final VoidCallback onStart;

  const CourseCard({super.key, required this.course, required this.onStart});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
      elevation: 3,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            ClipRRect(borderRadius: BorderRadius.circular(8), child: Image.network(course.imageUrl, width: 100, height: 80, fit: BoxFit.cover)),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(course.title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 6),
                  Text(course.description, maxLines: 2, overflow: TextOverflow.ellipsis),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Text(course.duration, style: const TextStyle(color: Colors.grey)),
                      const Spacer(),
                      ElevatedButton(
                        onPressed: onStart,
                        style: ElevatedButton.styleFrom(backgroundColor: Colors.blueAccent),
                        child: const Text('Start Learning'),
                      )
                    ],
                  )
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
