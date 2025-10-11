// lib/screens/learn/course_model.dart
class Course {
  final String id;
  final String title;
  final String description;
  final String duration; // e.g. '1h 20m'
  final String imageUrl;
  final String? audioUrl;
  final String? videoUrl;

  Course({
    required this.id,
    required this.title,
    required this.description,
    required this.duration,
    required this.imageUrl,
    this.audioUrl,
    this.videoUrl,
  });
}
