import 'package:flutter/material.dart';
import './job_model.dart';

class JobDetailsScreen extends StatelessWidget {
  final Job job;
  const JobDetailsScreen({super.key, required this.job});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(job.title),
        backgroundColor: const Color(0xFF003C9E), // Deep Blue
        centerTitle: true,
      ),
      backgroundColor: Colors.white,
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header card with logo + company info
            Card(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
              ),
              elevation: 3,
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    ClipRRect(
                      borderRadius: BorderRadius.circular(8),
                      child: Image.asset(
                        job.displayLogo,
                        height: 80,
                        width: 80,
                        fit: BoxFit.cover,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            job.title,
                            style: const TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                              color: Color(0xFF003C9E), // Deep Blue
                            ),
                          ),
                          Text(
                            job.company,
                            style: const TextStyle(
                              fontSize: 16,
                              color: Color(0xFF002F6C), // Dark Navy Blue
                            ),
                          ),
                          const SizedBox(height: 6),
                          Row(
                            children: [
                              const Icon(Icons.location_on,
                                  color: Color(0xFF00AEEF), size: 18), // Bright Blue
                              const SizedBox(width: 4),
                              Text(job.location),
                              const Spacer(),
                              const Icon(Icons.work_outline,
                                  color: Color(0xFF005ECF), size: 18), // Royal Blue
                              const SizedBox(width: 4),
                              Text(job.employmentType),
                            ],
                          ),
                          const SizedBox(height: 6),
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
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            // Salary row
            Row(
              children: [
                const Icon(Icons.attach_money,
                    color: Color(0xFFF28A2E), size: 20), // Bold Orange
                const SizedBox(width: 6),
                const Text(
                  "Salary Range:",
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF003C9E), // Deep Blue
                  ),
                ),
                const SizedBox(width: 6),
                Text(
                  job.salaryRange,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFFF28A2E),
                  ),
                ),
              ],
            ),

            const SizedBox(height: 10),

            // Deadline row
            if (job.deadline != null)
              Row(
                children: [
                  const Icon(Icons.calendar_today,
                      color: Color(0xFF005ECF), size: 18), // Royal Blue
                  const SizedBox(width: 6),
                  Text(
                    job.deadlineText,
                    style: const TextStyle(
                      fontSize: 15,
                      color: Colors.redAccent,
                    ),
                  ),
                ],
              ),

            const SizedBox(height: 24),

            // Sections
            _section("About the Company", Icons.business, job.aboutCompany),
            const SizedBox(height: 20),
            _section("Key Responsibilities", Icons.check_circle, job.responsibilities),
            const SizedBox(height: 20),
            _section("Requirements / Qualifications", Icons.school, job.qualifications),
            const SizedBox(height: 20),
            _section("How to Apply", Icons.send, job.howToApply),
          ],
        ),
      ),
    );
  }

  Widget _section(String title, IconData icon, String content) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(icon, color: const Color(0xFF003C9E)), // Deep Blue
            const SizedBox(width: 8),
            Text(
              title,
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
                color: Color(0xFF003C9E),
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Text(
          content,
          style: const TextStyle(
            fontSize: 15,
            color: Colors.black87,
          ),
        ),
      ],
    );
  }
}
