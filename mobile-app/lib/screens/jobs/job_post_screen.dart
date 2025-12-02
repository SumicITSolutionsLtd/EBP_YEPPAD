import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import './job_model.dart';

const Color kJobThemeColor = Color(0xFFFF8C00);

class JobPostScreen extends StatefulWidget {
  const JobPostScreen({super.key});

  @override
  State<JobPostScreen> createState() => _JobPostScreenState();
}

class _JobPostScreenState extends State<JobPostScreen> {
  final _formKey = GlobalKey<FormState>();
  final _picker = ImagePicker();
  File? _logoFile;

  final _title = TextEditingController();
  final _company = TextEditingController();
  final _location = TextEditingController();
  final _employmentType = TextEditingController();
  final _salaryRange = TextEditingController();
  final _shortDescription = TextEditingController();
  final _aboutCompany = TextEditingController();
  final _responsibilities = TextEditingController();
  final _qualifications = TextEditingController();
  final _howToApply = TextEditingController();
  final _industry = TextEditingController();

  Future<void> _pickLogo() async {
    final picked = await _picker.pickImage(source: ImageSource.gallery);
    if (picked != null) setState(() => _logoFile = File(picked.path));
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;

    final job = Job(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      title: _title.text.trim(),
      company: _company.text.trim(),
      location: _location.text.trim(),
      employmentType: _employmentType.text.trim(),
      salaryRange: _salaryRange.text.trim(),
      postedAt: DateTime.now(),
      shortDescription: _shortDescription.text.trim(),
      aboutCompany: _aboutCompany.text.trim(),
      responsibilities: _responsibilities.text.trim(),
      qualifications: _qualifications.text.trim(),
      howToApply: _howToApply.text.trim(),
      industry: _industry.text.trim(),
      companyLogo:
      _logoFile != null ? _logoFile!.path : 'assets/images/placeholder_logo.png',
    );

    Navigator.pop(context, job);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Post a Job'), backgroundColor: kJobThemeColor),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              GestureDetector(
                onTap: _pickLogo,
                child: _logoFile == null
                    ? Container(
                  height: 100,
                  width: 100,
                  decoration: BoxDecoration(
                    color: Colors.grey[200],
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(Icons.add_a_photo, color: Colors.grey),
                )
                    : ClipRRect(
                  borderRadius: BorderRadius.circular(12),
                  child: Image.file(_logoFile!, height: 100, width: 100, fit: BoxFit.cover),
                ),
              ),
              const SizedBox(height: 16),
              _buildField(_title, 'Job Title'),
              _buildField(_company, 'Company Name'),
              _buildField(_industry, 'Industry / Sector'),
              _buildField(_location, 'Location'),
              _buildField(_employmentType, 'Employment Type (Full-time, Remote...)'),
              _buildField(_salaryRange, 'Salary Range'),
              _buildField(_shortDescription, 'Short Description'),
              _buildField(_aboutCompany, 'About Company', maxLines: 3),
              _buildField(_responsibilities, 'Key Responsibilities', maxLines: 3),
              _buildField(_qualifications, 'Requirements / Qualifications', maxLines: 3),
              _buildField(_howToApply, 'How to Apply', maxLines: 2),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: _submit,
                style: ElevatedButton.styleFrom(
                    backgroundColor: kJobThemeColor,
                    minimumSize: const Size(double.infinity, 48)),
                child: const Text('Post Job'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildField(TextEditingController controller, String label,
      {int maxLines = 1}) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12.0),
      child: TextFormField(
        controller: controller,
        maxLines: maxLines,
        validator: (v) => v == null || v.isEmpty ? 'Enter $label' : null,
        decoration: InputDecoration(labelText: label, border: const OutlineInputBorder()),
      ),
    );
  }
}
