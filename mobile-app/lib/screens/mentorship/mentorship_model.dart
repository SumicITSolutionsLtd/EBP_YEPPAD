// lib/models/mentorship_model.dart
import 'package:flutter/foundation.dart';

class MentorshipProgram {
  final String id;
  final String title;
  final String mentorName;
  final String? mentorPhotoPath; // either 'assets/...' or device file path
  final String mentorBio;
  final DateTime startDate;
  final DateTime endDate;
  final String duration; // e.g. "4 weeks", or computed if you prefer
  final String format; // "Online" or "Physical"
  final String? platform; // e.g. "Google Meet" (if online)
  final String? meetingLink; // optional
  final String? venue; // if physical
  final String? mapLink; // optional Google Maps link if physical
  final String requirements; // e.g. "Laptop + Internet"
  final String whoShouldJoin; // target audience
  final String howToJoin; // registration instructions or link
  final String contactInfo; // email/phone

  MentorshipProgram({
    required this.id,
    required this.title,
    required this.mentorName,
    this.mentorPhotoPath,
    required this.mentorBio,
    required this.startDate,
    required this.endDate,
    required this.duration,
    required this.format,
    this.platform,
    this.meetingLink,
    this.venue,
    this.mapLink,
    required this.requirements,
    required this.whoShouldJoin,
    required this.howToJoin,
    required this.contactInfo,
  });
}
