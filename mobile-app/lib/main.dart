import 'package:flutter/material.dart';
import 'package:ebp_platform/screens/splash_screen.dart';

void main() {
  runApp(const EBP_Platform());
}

class EBP_Platform extends StatelessWidget {
  const EBP_Platform({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: "Entrepreneurship Booster Platform",
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.teal),
        useMaterial3: true,
      ),
      home: const SplashScreen(),
    );
  }
}
