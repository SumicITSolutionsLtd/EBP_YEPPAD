import 'package:flutter/material.dart';
import 'add_skill_screen.dart';
import 'skill_card.dart';
import 'skill_model.dart';

class SkillsScreen extends StatefulWidget {
  const SkillsScreen({super.key});

  @override
  State<SkillsScreen> createState() => _SkillsScreenState();
}

class _SkillsScreenState extends State<SkillsScreen> {
  List<Skill> allSkills = [];
  String selectedCategory = 'All';
  String searchQuery = '';

  final List<String> categories = [
    'All',
    'Tailoring',
    'Carpentry',
    'Cooking',
    'Hairdressing',
    'IT',
    'Crafts',
  ];

  void addSkill(Skill skill) {
    setState(() => allSkills.add(skill));
  }

  @override
  Widget build(BuildContext context) {
    List<Skill> filteredSkills = allSkills.where((skill) {
      final matchesSearch =
      skill.name.toLowerCase().contains(searchQuery.toLowerCase());
      final matchesCategory =
          selectedCategory == 'All' || skill.category == selectedCategory;
      return matchesSearch && matchesCategory;
    }).toList();

    return Scaffold(
      appBar: AppBar(title: const Text('My Skills')),
      body: Column(
        children: [
          // Search Bar
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: TextField(
              decoration: InputDecoration(
                prefixIcon: const Icon(Icons.search),
                hintText: 'Search skills...',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(30),
                ),
              ),
              onChanged: (value) => setState(() => searchQuery = value),
            ),
          ),

          // Category Chips
          SizedBox(
            height: 40,
            child: ListView.builder(
              scrollDirection: Axis.horizontal,
              itemCount: categories.length,
              itemBuilder: (context, index) {
                final category = categories[index];
                final isSelected = category == selectedCategory;
                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 4),
                  child: ChoiceChip(
                    label: Text(category),
                    selected: isSelected,
                    onSelected: (_) =>
                        setState(() => selectedCategory = category),
                  ),
                );
              },
            ),
          ),

          // Skills List
          Expanded(
            child: filteredSkills.isEmpty
                ? const Center(child: Text('No skills found.'))
                : ListView.builder(
              itemCount: filteredSkills.length,
              itemBuilder: (context, index) {
                final skill = filteredSkills[index];
                return SkillCard(
                  skill: skill,
                  onEdit: () {},
                  onDelete: () {
                    setState(() => allSkills.remove(skill));
                  },
                  onShare: () {},
                );
              },
            ),
          ),
        ],
      ),

      // Floating Action Button
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          final newSkill = await Navigator.push(
            context,
            MaterialPageRoute(builder: (_) => const AddSkillScreen()),
          );
          if (newSkill != null && newSkill is Skill) addSkill(newSkill);
        },
        child: const Icon(Icons.add),
      ),
    );
  }
}
