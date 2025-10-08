import 'package:flutter/material.dart';
import 'screen_template.dart';

class JobsScreen extends StatelessWidget {
  const JobsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const ScreenTemplate(
      title: "Jobs",
      icon: Icons.work_outline,
      color: Colors.deepOrangeAccent,
    );
  }
}
