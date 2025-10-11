// lib/screens/jobs/add_job_screen.dart
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'job_model.dart';

class AddJobScreen extends StatefulWidget {
  const AddJobScreen({super.key});

  @override
  State<AddJobScreen> createState() => _AddJobScreenState();
}

class _AddJobScreenState extends State<AddJobScreen> {
  final _formKey = GlobalKey<FormState>();
  final _title = TextEditingController();
  final _company = TextEditingController();
  final _location = TextEditingController();
  final _pay = TextEditingController();
  final _phone = TextEditingController();
  String _category = 'All';
  String _currency = 'UGX';
  File? _imageFile;
  final _picker = ImagePicker();

  final categories = ['All','IT','Tailoring','Construction','Marketing','Design','Other'];

  Future<void> _pickImage() async {
    final picked = await _picker.pickImage(source: ImageSource.gallery, imageQuality: 80);
    if (picked != null) setState(() => _imageFile = File(picked.path));
  }

  void _save() {
    if (!_formKey.currentState!.validate()) return;
    final job = Job(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      title: _title.text.trim(),
      company: _company.text.trim(),
      category: _category,
      location: _location.text.trim(),
      postedAt: DateTime.now(),
      pay: double.tryParse(_pay.text) ?? 0,
      currency: _currency,
      contactPhone: _phone.text.trim(),
      imageUrl: _imageFile?.path ?? 'https://via.placeholder.com/150',
    );
    Navigator.of(context).pop(job);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Post New Job'),
        backgroundColor: Colors.deepOrangeAccent,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(children: [
            GestureDetector(
              onTap: _pickImage,
              child: _imageFile == null
                  ? Container(
                height: 120,
                width: 120,
                decoration: BoxDecoration(borderRadius: BorderRadius.circular(12), color: Colors.grey[200]),
                child: const Icon(Icons.add_a_photo, size: 40, color: Colors.grey),
              )
                  : ClipRRect(borderRadius: BorderRadius.circular(12), child: Image.file(_imageFile!, height: 120, width: 120, fit: BoxFit.cover)),
            ),
            const SizedBox(height: 12),
            TextFormField(controller: _title, decoration: const InputDecoration(labelText: 'Job title'), validator: (v)=> v==null||v.isEmpty ? 'Enter title' : null),
            const SizedBox(height: 8),
            TextFormField(controller: _company, decoration: const InputDecoration(labelText: 'Company'), validator: (v)=> v==null||v.isEmpty ? 'Enter company' : null),
            const SizedBox(height: 8),
            DropdownButtonFormField<String>(
              value: _category,
              decoration: const InputDecoration(labelText: 'Category'),
              items: categories.map((c)=> DropdownMenuItem(value: c, child: Text(c))).toList(),
              onChanged: (v)=> setState(()=> _category = v ?? 'All'),
            ),
            const SizedBox(height: 8),
            TextFormField(controller: _location, decoration: const InputDecoration(labelText: 'Location'), validator: (v)=> v==null||v.isEmpty ? 'Enter location' : null),
            const SizedBox(height: 8),
            TextFormField(controller: _pay, keyboardType: TextInputType.number, decoration: const InputDecoration(labelText: 'Pay (number)'),),
            const SizedBox(height: 8),
            TextFormField(controller: _phone, keyboardType: TextInputType.phone, decoration: const InputDecoration(labelText: 'Contact phone'), validator: (v)=> v==null||v.isEmpty ? 'Enter phone' : null),
            const SizedBox(height: 16),
            SizedBox(
              width: double.infinity,
              child: ElevatedButton(onPressed: _save, child: const Text('Post Job')),
            )
          ]),
        ),
      ),
    );
  }
}
