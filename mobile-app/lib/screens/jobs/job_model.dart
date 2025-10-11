// lib/screens/jobs/job_model.dart
class Job {
  final String id;
  final String title;
  final String company;
  final String category;
  final String location;
  final DateTime postedAt;
  final double pay;
  final String currency;
  final String contactPhone;
  final String imageUrl;

  Job({
    required this.id,
    required this.title,
    required this.company,
    required this.category,
    required this.location,
    required this.postedAt,
    required this.pay,
    this.currency = 'UGX',
    required this.contactPhone,
    required this.imageUrl,
  });

  String postedAgo() {
    final diff = DateTime.now().difference(postedAt);
    if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';
    if (diff.inHours < 24) return '${diff.inHours}h ago';
    return '${diff.inDays}d ago';
  }
}
