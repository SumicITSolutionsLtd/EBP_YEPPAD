// lib/screens/mentorship/mentorship_post_screen.dart
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import './mentorship_model.dart';

const Color kMentorshipColor = Colors.purpleAccent;

class MentorshipPostScreen extends StatefulWidget {
  const MentorshipPostScreen({super.key});

  @override
  State<MentorshipPostScreen> createState() => _MentorshipPostScreenState();
}

class _MentorshipPostScreenState extends State<MentorshipPostScreen> {
  final _formKey = GlobalKey<FormState>();
  final ImagePicker _picker = ImagePicker();
  File? _pickedImage;
  String? _chosenAsset;

  // controllers
  final _title = TextEditingController();
  final _mentorName = TextEditingController();
  final _mentorBio = TextEditingController();
  DateTime? _startDate;
  DateTime? _endDate;
  final _duration = TextEditingController();
  String _format = 'Online';
  final _platform = TextEditingController();
  final _meetingLink = TextEditingController();
  final _venue = TextEditingController();
  final _mapLink = TextEditingController();
  final _requirements = TextEditingController();
  final _whoShouldJoin = TextEditingController();
  final _howToJoin = TextEditingController();
  final _contactInfo = TextEditingController();

  Future<void> _pickFromGallery() async {
    final picked =
    await _picker.pickImage(source: ImageSource.gallery, imageQuality: 80);
    if (picked != null) {
      setState(() {
        _pickedImage = File(picked.path);
        _chosenAsset = null;
      });
    }
  }

  void _chooseAsset(String assetPath) {
    setState(() {
      _chosenAsset = assetPath;
      _pickedImage = null;
    });
  }

  Future<void> _pickDate(BuildContext ctx, bool isStart) async {
    final now = DateTime.now();
    final first = DateTime(now.year - 1);
    final last = DateTime(now.year + 2);
    final picked = await showDatePicker(
      context: ctx,
      initialDate: now,
      firstDate: first,
      lastDate: last,
    );
    if (picked == null) return;
    setState(() {
      if (isStart) {
        _startDate = picked;
      } else {
        _endDate = picked;
      }
    });
  }

  void _submit() {
    if (!_formKey.currentState!.validate()) return;
    if (_startDate == null || _endDate == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please pick start and end dates.')),
      );
      return;
    }

    final id = DateTime.now().millisecondsSinceEpoch.toString();
    final photoPath = _pickedImage?.path ?? _chosenAsset;

    final prog = MentorshipProgram(
      id: id,
      title: _title.text.trim(),
      mentorName: _mentorName.text.trim(),
      mentorPhotoPath: photoPath,
      mentorBio: _mentorBio.text.trim(),
      startDate: _startDate!,
      endDate: _endDate!,
      duration: _duration.text.trim().isEmpty
          ? _calculateDuration(_startDate!, _endDate!)
          : _duration.text.trim(),
      format: _format,
      platform: _format == 'Online' ? _platform.text.trim() : null,
      meetingLink: _format == 'Online' ? _meetingLink.text.trim() : null,
      venue: _format == 'Physical' ? _venue.text.trim() : null,
      mapLink: _format == 'Physical' ? _mapLink.text.trim() : null,
      requirements: _requirements.text.trim(),
      whoShouldJoin: _whoShouldJoin.text.trim(),
      howToJoin: _howToJoin.text.trim(),
      contactInfo: _contactInfo.text.trim(),
    );

    Navigator.pop(context, prog);
  }

  static String _calculateDuration(DateTime start, DateTime end) {
    final days = end.difference(start).inDays;
    if (days < 7) return '$days days';
    final weeks = (days / 7).round();
    return '$weeks weeks';
  }

  static String _formatShort(DateTime d) =>
      '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

  // 🔹 Reusable input field builder
  Widget _buildField(
      TextEditingController controller,
      String label, {
        int maxLines = 1,
        TextInputType inputType = TextInputType.text,
      }) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: TextFormField(
        controller: controller,
        keyboardType: inputType,
        maxLines: maxLines,
        validator: (value) {
          if (value == null || value.trim().isEmpty) {
            return 'Please enter $label';
          }
          return null;
        },
        decoration: InputDecoration(
          labelText: label,
          filled: true,
          fillColor: Colors.grey[100],
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide.none,
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: kMentorshipColor, width: 1.5),
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final sampleAssets = [
      'assets/images/mentor1.png',
      'assets/images/mentor2.png',
      'assets/images/mentor3.png',
    ];

    return Scaffold(
      appBar: AppBar(
        title: const Text('Add Mentorship Program'),
        backgroundColor: kMentorshipColor,
        elevation: 2,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            children: [
              Row(
                children: [
                  GestureDetector(
                    onTap: _pickFromGallery,
                    child: _pickedImage != null
                        ? ClipRRect(
                      borderRadius: BorderRadius.circular(12),
                      child: Image.file(
                        _pickedImage!,
                        width: 90,
                        height: 90,
                        fit: BoxFit.cover,
                      ),
                    )
                        : _chosenAsset != null
                        ? ClipRRect(
                      borderRadius: BorderRadius.circular(12),
                      child: Image.asset(
                        _chosenAsset!,
                        width: 90,
                        height: 90,
                        fit: BoxFit.cover,
                      ),
                    )
                        : Container(
                      width: 90,
                      height: 90,
                      decoration: BoxDecoration(
                        color: Colors.grey[200],
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: const Icon(Icons.add_a_photo,
                          color: Colors.grey),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        ElevatedButton.icon(
                          style: ElevatedButton.styleFrom(
                            backgroundColor: kMentorshipColor,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10),
                            ),
                          ),
                          onPressed: _pickFromGallery,
                          icon: const Icon(Icons.photo),
                          label: const Text('Pick from Gallery'),
                        ),
                        const SizedBox(height: 6),
                        const Text(
                          'Or choose a sample avatar below:',
                          style:
                          TextStyle(fontSize: 12, color: Colors.black54),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              SizedBox(
                height: 80,
                child: ListView.separated(
                  scrollDirection: Axis.horizontal,
                  itemCount: sampleAssets.length,
                  separatorBuilder: (_, __) => const SizedBox(width: 8),
                  itemBuilder: (context, i) {
                    final a = sampleAssets[i];
                    final selected = a == _chosenAsset;
                    return GestureDetector(
                      onTap: () => _chooseAsset(a),
                      child: Container(
                        padding: const EdgeInsets.all(4),
                        decoration: BoxDecoration(
                          border: selected
                              ? Border.all(
                              color: kMentorshipColor, width: 2)
                              : null,
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: ClipRRect(
                          borderRadius: BorderRadius.circular(8),
                          child: Image.asset(a,
                              width: 64, height: 64, fit: BoxFit.cover),
                        ),
                      ),
                    );
                  },
                ),
              ),
              const SizedBox(height: 12),
              _buildField(_title, 'Program Title'),
              _buildField(_mentorName, 'Mentor Full Name'),
              _buildField(_mentorBio, 'About Mentor', maxLines: 4),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: () => _pickDate(context, true),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: kMentorshipColor,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      icon: const Icon(Icons.calendar_today),
                      label: Text(_startDate == null
                          ? 'Pick Start Date'
                          : 'Start: ${_formatShort(_startDate!)}'),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: ElevatedButton.icon(
                      onPressed: () => _pickDate(context, false),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: kMentorshipColor,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(10),
                        ),
                      ),
                      icon: const Icon(Icons.calendar_today_outlined),
                      label: Text(_endDate == null
                          ? 'Pick End Date'
                          : 'End: ${_formatShort(_endDate!)}'),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              _buildField(_duration, 'Duration (e.g. 3 weeks)'),
              const SizedBox(height: 8),
              DropdownButtonFormField<String>(
                value: _format,
                items: const [
                  DropdownMenuItem(value: 'Online', child: Text('Online')),
                  DropdownMenuItem(value: 'Physical', child: Text('Physical')),
                ],
                onChanged: (v) => setState(() {
                  _format = v ?? 'Online';
                }),
                decoration: InputDecoration(
                  labelText: 'Format',
                  filled: true,
                  fillColor: Colors.grey[100],
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide.none,
                  ),
                ),
              ),
              const SizedBox(height: 8),
              if (_format == 'Online') ...[
                _buildField(_platform, 'Platform (e.g. Google Meet, Zoom)'),
                _buildField(_meetingLink, 'Meeting Link (optional)'),
              ] else ...[
                _buildField(_venue, 'Venue / Place'),
                _buildField(_mapLink, 'Google Maps Link (optional)'),
              ],
              _buildField(_requirements, 'Requirements', maxLines: 3),
              _buildField(_whoShouldJoin, 'Who should join?', maxLines: 2),
              _buildField(_howToJoin, 'How to join / Registration', maxLines: 2),
              _buildField(_contactInfo, 'Contact Info', maxLines: 2),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: _submit,
                style: ElevatedButton.styleFrom(
                  backgroundColor: kMentorshipColor,
                  minimumSize: const Size.fromHeight(48),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                child: const Text(
                  'Publish Program',
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                ),
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }
}
