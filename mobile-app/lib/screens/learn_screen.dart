import 'package:flutter/material.dart';
import 'screen_template.dart';

class LearnScreen extends StatelessWidget {
  const LearnScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const ScreenTemplate(
      title: "Learn",
      icon: Icons.school,
      color: Colors.blueAccent,
    );
  }
}
