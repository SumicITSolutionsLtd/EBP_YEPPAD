// lib/screens/mentorship/mentorship_details_screen.dart
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import './mentorship_model.dart';

const Color kMentorshipColor = Colors.purpleAccent;

class MentorshipDetailsScreen extends StatelessWidget {
  final MentorshipProgram program;
  const MentorshipDetailsScreen({super.key, required this.program});

  @override
  Widget build(BuildContext context) {
    Widget _photoWidget() {
      final path = program.mentorPhotoPath ?? 'assets/images/default_mentor.png';
      if (path.startsWith('assets/')) {
        return Image.asset(path, width: 120, height: 120, fit: BoxFit.cover);
      } else {
        final f = File(path);
        if (f.existsSync()) return Image.file(f, width: 120, height: 120, fit: BoxFit.cover);
        return Image.asset('assets/images/default_mentor.png', width: 120, height: 120, fit: BoxFit.cover);
      }
    }

    Widget _copyRow(String label, String value) {
      return Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(child: SelectableText(value)),
          IconButton(
            icon: const Icon(Icons.copy, size: 18),
            onPressed: () {
              Clipboard.setData(ClipboardData(text: value));
              ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Copied to clipboard')));
            },
          ),
        ],
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(program.title),
        backgroundColor: kMentorshipColor,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Center(child: ClipRRect(borderRadius: BorderRadius.circular(12), child: _photoWidget())),
          const SizedBox(height: 12),
          Text(program.mentorName, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
          const SizedBox(height: 6),
          Row(children: [
            const Icon(Icons.date_range, size: 18, color: Colors.grey),
            const SizedBox(width: 6),
            Text('${_formatDate(program.startDate)} → ${_formatDate(program.endDate)}'),
            const Spacer(),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
              decoration: BoxDecoration(color: kMentorshipColor.withOpacity(0.12), borderRadius: BorderRadius.circular(20)),
              child: Text(program.format, style: TextStyle(color: kMentorshipColor)),
            )
          ]),
          const Divider(height: 20),
          const Text('About Mentor', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          const SizedBox(height: 6),
          SelectableText(program.mentorBio),
          const SizedBox(height: 12),
          const Text('Who should join', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          const SizedBox(height: 6),
          SelectableText(program.whoShouldJoin),
          const SizedBox(height: 12),
          const Text('Requirements', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          const SizedBox(height: 6),
          SelectableText(program.requirements),
          const SizedBox(height: 12),
          const Text('How to Join / Registration', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          const SizedBox(height: 6),
          _copyRow('Registration', program.howToJoin),
          const SizedBox(height: 12),
          const Text('Contact Info', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          const SizedBox(height: 6),
          _copyRow('Contact', program.contactInfo),
          const SizedBox(height: 12),
          if (program.format == 'Online' && (program.platform != null || program.meetingLink != null)) ...[
            const Text('Online / Platform details', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 6),
            if (program.platform != null) _copyRow('Platform', program.platform!),
            if (program.meetingLink != null) _copyRow('Meeting Link', program.meetingLink!),
          ],
          if (program.format == 'Physical' && (program.venue != null || program.mapLink != null)) ...[
            const Text('Venue details', style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const SizedBox(height: 6),
            if (program.venue != null) _copyRow('Venue', program.venue!),
            if (program.mapLink != null) _copyRow('Map Link', program.mapLink!),
          ],
        ]),
      ),
    );
  }

  static String _formatDate(DateTime d) =>
      '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';
}
