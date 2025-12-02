import 'package:flutter/material.dart';

class Job {
  final String id;
  final String title;
  final String company;
  final String location;
  final String employmentType;
  final String salaryRange;
  final DateTime postedAt;
  final DateTime? deadline;
  final String shortDescription;
  final String aboutCompany;
  final String responsibilities;
  final String qualifications;
  final String howToApply;
  final String industry;
  final String? companyLogo;

  Job({
    required this.id,
    required this.title,
    required this.company,
    required this.location,
    required this.employmentType,
    required this.salaryRange,
    required this.postedAt,
    this.deadline,
    required this.shortDescription,
    required this.aboutCompany,
    required this.responsibilities,
    required this.qualifications,
    required this.howToApply,
    required this.industry,
    this.companyLogo,
  });

  /// Human-friendly "posted ago" string
  String get postedAgo {
    final diff = DateTime.now().difference(postedAt);
    if (diff.inMinutes < 1) return 'Just now';
    if (diff.inMinutes < 60) return '${diff.inMinutes}m ago';
    if (diff.inHours < 24) return '${diff.inHours}h ago';
    if (diff.inDays == 1) return 'Yesterday';
    return '${diff.inDays}d ago';
  }

  /// Display logo or fallback placeholder
  String get displayLogo => companyLogo ?? 'assets/images/placeholder_logo.png';

  /// Formatted deadline string
  String get deadlineText =>
      deadline != null ? 'Apply before ${deadline!.toLocal().toString().split(' ')[0]}' : 'No deadline';

  /// Industry label
  String get industryLabel => industry.isNotEmpty ? industry : 'General';
}
