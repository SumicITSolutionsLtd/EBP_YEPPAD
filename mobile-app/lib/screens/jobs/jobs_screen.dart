// lib/screens/jobs/jobs_screen.dart
import 'dart:io';
import 'package:flutter/material.dart';
import 'job_model.dart';
import 'job_card.dart';
import 'add_job_screen.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:share_plus/share_plus.dart';

class JobsScreen extends StatefulWidget {
  const JobsScreen({super.key});

  @override
  State<JobsScreen> createState() => _JobsScreenState();
}

class _JobsScreenState extends State<JobsScreen> {
  final TextEditingController _searchController = TextEditingController();
  String _selectedCategory = 'All';

  List<Job> jobs = [
    Job(
      id: '1',
      title: 'Tailor needed for wedding dresses',
      company: 'Luma Boutique',
      category: 'Tailoring',
      location: 'Kampala',
      postedAt: DateTime.now().subtract(const Duration(hours: 2)),
      pay: 50000,
      currency: 'UGX',
      contactPhone: '+256701234567',
      imageUrl: 'https://via.placeholder.com/150',
    ),
    Job(
      id: '2',
      title: 'Junior Flutter Developer',
      company: 'TechHub',
      category: 'IT',
      location: 'Entebbe',
      postedAt: DateTime.now().subtract(const Duration(days: 1, hours: 3)),
      pay: 250000,
      currency: 'UGX',
      contactPhone: '+256702345678',
      imageUrl: 'https://via.placeholder.com/150',
    ),
  ];

  final categories = ['All', 'IT', 'Tailoring', 'Construction', 'Marketing', 'Design'];

  Future<void> _refreshJobs() async {
    // demo: pretend to fetch; in real app call API
    await Future.delayed(const Duration(seconds: 1));
    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Jobs refreshed')));
    setState(() {});
  }

  void _openAddJob() async {
    final newJob = await Navigator.push(context, MaterialPageRoute(builder: (_) => const AddJobScreen()));
    if (newJob != null && newJob is Job) {
      setState(() { jobs.insert(0, newJob); });
    }
  }

  void _callNumber(String phone) async {
    final uri = Uri(scheme: 'tel', path: phone);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri);
    } else {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Cannot open dialer')));
    }
  }

  void _shareJob(Job job) {
    final text = '${job.title} at ${job.company} • ${job.location} • ${job.currency} ${job.pay}\nContact: ${job.contactPhone}';
    Share.share(text);
  }

  @override
  Widget build(BuildContext context) {
    final query = _searchController.text.toLowerCase();
    final filtered = jobs.where((j) {
      final matchesSearch = j.title.toLowerCase().contains(query) || j.company.toLowerCase().contains(query);
      final matchesCategory = _selectedCategory == 'All' || j.category == _selectedCategory;
      return matchesSearch && matchesCategory;
    }).toList();

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.deepOrangeAccent,
        title: const Text('Jobs'),
        leading: IconButton(icon: const Icon(Icons.arrow_back), onPressed: () => Navigator.pop(context)),
        actions: [
          IconButton(onPressed: _refreshJobs, icon: const Icon(Icons.refresh)),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        tooltip: 'Post Job',
        backgroundColor: Colors.deepOrangeAccent,
        onPressed: _openAddJob,
        child: const Icon(Icons.post_add), // "clear icon" you mentioned - use post_add as Post icon
      ),
      body: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            // Search
            TextField(
              controller: _searchController,
              decoration: InputDecoration(prefixIcon: const Icon(Icons.search), hintText: 'Search jobs...', border: OutlineInputBorder(borderRadius: BorderRadius.circular(10))),
              onChanged: (_) => setState((){}),
            ),
            const SizedBox(height: 10),

            // category chips
            SizedBox(
              height: 42,
              child: ListView.separated(
                scrollDirection: Axis.horizontal,
                itemCount: categories.length,
                separatorBuilder: (_, __) => const SizedBox(width: 8),
                itemBuilder: (context, i) {
                  final cat = categories[i];
                  final isSelected = cat == _selectedCategory;
                  return ChoiceChip(
                    label: Text(cat),
                    selected: isSelected,
                    onSelected: (_) => setState(() => _selectedCategory = cat),
                    selectedColor: Colors.deepOrangeAccent.shade100,
                  );
                },
              ),
            ),
            const SizedBox(height: 12),

            // list
            Expanded(
              child: filtered.isEmpty
                  ? const Center(child: Text('No jobs match your filters.', style: TextStyle(color: Colors.grey)))
                  : ListView.builder(
                itemCount: filtered.length,
                itemBuilder: (context, idx) {
                  final job = filtered[idx];
                  return JobCard(
                    job: job,
                    onCall: () => _callNumber(job.contactPhone),
                    onShare: () => _shareJob(job),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
