// lib/screens/jobs/job_card.dart
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:share_plus/share_plus.dart';
import 'job_model.dart';

class JobCard extends StatelessWidget {
  final Job job;
  final VoidCallback onShare;
  final VoidCallback onCall;

  const JobCard({
    super.key,
    required this.job,
    required this.onShare,
    required this.onCall,
  });

  @override
  Widget build(BuildContext context) {
    Widget imageWidget;
    if (job.imageUrl.startsWith('http')) {
      imageWidget = Image.network(job.imageUrl, width: 80, height: 80, fit: BoxFit.cover);
    } else {
      imageWidget = Image.file(File(job.imageUrl), width: 80, height: 80, fit: BoxFit.cover);
    }

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      elevation: 3,
      margin: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            ClipRRect(borderRadius: BorderRadius.circular(8), child: imageWidget),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(job.title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 4),
                  Text('${job.company} • ${job.category}', style: const TextStyle(color: Colors.grey)),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      const Icon(Icons.location_on, size: 14, color: Colors.grey),
                      const SizedBox(width: 4),
                      Text(job.location, style: const TextStyle(color: Colors.black87)),
                      const Spacer(),
                      Text(job.postedAgo(), style: const TextStyle(color: Colors.grey)),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Text('${job.currency} ${job.pay.toStringAsFixed(0)}',
                          style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.teal)),
                      const Spacer(),
                      IconButton(
                        onPressed: onCall,
                        icon: Row(
                          children: const [
                            Icon(Icons.call, color: Colors.green),
                            SizedBox(width: 4),
                            Text('Call', style: TextStyle(color: Colors.green))
                          ],
                        ) as Widget,
                      ),
                      IconButton(
                        onPressed: onShare,
                        icon: const Icon(Icons.share, color: Colors.grey),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
