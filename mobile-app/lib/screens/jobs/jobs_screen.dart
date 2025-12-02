import 'package:flutter/material.dart';
import 'job_details_screen.dart';
import './job_model.dart';

class JobsScreen extends StatefulWidget {
  const JobsScreen({super.key});

  @override
  State<JobsScreen> createState() => _JobsScreenState();
}

class _JobsScreenState extends State<JobsScreen> {
  final TextEditingController _searchController = TextEditingController();
  List<Job> jobs = [];

  @override
  void initState() {
    super.initState();
    jobs = [
      Job(
        id: '1',
        title: 'Flutter Developer',
        company: 'TechHub Uganda',
        location: 'Kampala',
        employmentType: 'Full-time',
        salaryRange: 'UGX 2,000,000 - 2,500,000',
        postedAt: DateTime.now().subtract(const Duration(hours: 6)),
        deadline: DateTime.now().add(const Duration(days: 10)),
        shortDescription: 'Join our mobile team to build next-gen Flutter apps.',
        aboutCompany:
        'TechHub Uganda is a leading software company specializing in app and web development.',
        responsibilities:
        '- Build cross-platform apps using Flutter.\n- Collaborate with backend teams.\n- Maintain code quality.',
        qualifications:
        '- Bachelor’s degree in Computer Science.\n- Experience with Flutter & Dart.\n- Git proficiency.',
        howToApply: 'Send your CV and portfolio to careers@techhub.co.ug',
        industry: 'Information Technology',
        companyLogo: 'assets/images/techhub.jpg',
      ),
      Job(
        id: '2',
        title: 'Graphic Designer',
        company: 'Vision Arts Studio',
        location: 'Entebbe',
        employmentType: 'Part-time',
        salaryRange: 'UGX 800,000 - 1,200,000',
        postedAt: DateTime.now().subtract(const Duration(days: 1)),
        deadline: DateTime.now().add(const Duration(days: 7)),
        shortDescription:
        'Creative designer wanted for branding and digital campaigns.',
        aboutCompany:
        'Vision Arts Studio helps businesses grow through visual storytelling and branding.',
        responsibilities:
        '- Create designs for marketing.\n- Work with clients to visualize ideas.',
        qualifications:
        '- Proficient in Adobe Photoshop and Illustrator.\n- Strong portfolio.',
        howToApply: 'Email your CV to hr@visionarts.com',
        industry: 'Design & Creative',
      ),
    ];
  }

  @override
  Widget build(BuildContext context) {
    final query = _searchController.text.toLowerCase();
    final filteredJobs = jobs.where((job) {
      return job.title.toLowerCase().contains(query) ||
          job.company.toLowerCase().contains(query) ||
          job.location.toLowerCase().contains(query);
    }).toList();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Jobs'),
        backgroundColor: const Color(0xFF003C9E), // Deep Blue
        centerTitle: true,
      ),
      backgroundColor: Colors.white,
      body: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          children: [
            // Search bar
            TextField(
              controller: _searchController,
              decoration: InputDecoration(
                prefixIcon: const Icon(Icons.search, color: Color(0xFF00AEEF)), // Bright Blue
                hintText: 'Search jobs...',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
                filled: true,
                fillColor: Colors.grey.shade100,
              ),
              onChanged: (_) => setState(() {}),
            ),
            const SizedBox(height: 12),

            // Job list
            Expanded(
              child: ListView.builder(
                itemCount: filteredJobs.length,
                itemBuilder: (context, index) {
                  final job = filteredJobs[index];
                  return GestureDetector(
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => JobDetailsScreen(job: job),
                      ),
                    ),
                    child: Card(
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(16),
                      ),
                      elevation: 3,
                      margin: const EdgeInsets.symmetric(vertical: 8),
                      child: Padding(
                        padding: const EdgeInsets.all(14.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            // Logo + Title + Company
                            Row(
                              children: [
                                ClipRRect(
                                  borderRadius: BorderRadius.circular(8),
                                  child: Image.asset(
                                    job.displayLogo,
                                    width: 60,
                                    height: 60,
                                    fit: BoxFit.cover,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        job.title,
                                        style: const TextStyle(
                                          fontSize: 16,
                                          fontWeight: FontWeight.bold,
                                          color: Color(0xFF003C9E), // Deep Blue
                                        ),
                                      ),
                                      Text(
                                        job.company,
                                        style: const TextStyle(
                                          color: Color(0xFF002F6C), // Dark Navy Blue
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 10),

                            // Location + Posted
                            Row(
                              children: [
                                const Icon(Icons.location_on,
                                    color: Color(0xFF00AEEF), size: 18), // Bright Blue
                                const SizedBox(width: 4),
                                Text(job.location),
                                const Spacer(),
                                Row(
                                  children: [
                                    const Icon(Icons.access_time,
                                        color: Color(0xFF005ECF), size: 16), // Royal Blue
                                    const SizedBox(width: 4),
                                    Text(
                                      "Posted ${job.postedAgo}", // 👈 Added "Posted"
                                      style: const TextStyle(color: Colors.grey),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                            const SizedBox(height: 6),

                            // Employment type + Salary
                            Row(
                              children: [
                                const Icon(Icons.work_outline,
                                    color: Color(0xFF005ECF), size: 18), // Royal Blue
                                const SizedBox(width: 4),
                                Text(job.employmentType),
                                const Spacer(),
                                Row(
                                  children: [
                                    const Icon(Icons.attach_money,
                                        color: Color(0xFFF28A2E), size: 18), // Bold Orange
                                    const SizedBox(width: 4),
                                    Text(
                                      job.salaryRange,
                                      style: const TextStyle(
                                        color: Color(0xFFF28A2E), // Bold Orange
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                            const SizedBox(height: 10),

                            // Short description
                            Text(
                              job.shortDescription,
                              style: const TextStyle(
                                fontStyle: FontStyle.italic,
                                color: Colors.black87,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
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
