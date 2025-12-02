import 'package:flutter/material.dart';
import 'screen_template.dart';

class CommunityScreen extends StatelessWidget {
  const CommunityScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const ScreenTemplate(
      title: "Community",
      icon: Icons.forum,
      color: Colors.blueAccent,
    );
  }
}
