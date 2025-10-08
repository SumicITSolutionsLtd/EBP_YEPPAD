import 'package:flutter/material.dart';
import 'skill_model.dart';

class SkillCard extends StatelessWidget {
  final Skill skill;
  final VoidCallback onEdit;
  final VoidCallback onDelete;
  final VoidCallback onShare;

  const SkillCard({
    super.key,
    required this.skill,
    required this.onEdit,
    required this.onDelete,
    required this.onShare,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      margin: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
      elevation: 4,
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ClipRRect(
              borderRadius: BorderRadius.circular(12),
              child: Image.asset(
                skill.imageUrl,
                width: 80,
                height: 80,
                fit: BoxFit.cover,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(skill.name,
                      style: const TextStyle(
                          fontWeight: FontWeight.bold, fontSize: 16)),
                  Text(skill.category,
                      style: const TextStyle(color: Colors.grey)),
                  if (skill.price != null)
                    Text("Price: \$${skill.price!.toStringAsFixed(2)}"),
                  Text("Location: ${skill.location}"),
                  const SizedBox(height: 6),
                  Text(
                    skill.description,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                  ),
                  const SizedBox(height: 6),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Chip(
                        label: Text(skill.isActive ? 'Active' : 'Inactive'),
                        backgroundColor: skill.isActive
                            ? Colors.green.withOpacity(0.2)
                            : Colors.red.withOpacity(0.2),
                      ),
                      Row(
                        children: [
                          IconButton(
                              icon: const Icon(Icons.edit, color: Colors.blue),
                              onPressed: onEdit),
                          IconButton(
                              icon: const Icon(Icons.delete, color: Colors.red),
                              onPressed: onDelete),
                          IconButton(
                              icon: const Icon(Icons.share, color: Colors.grey),
                              onPressed: onShare),
                        ],
                      )
                    ],
                  )
                ],
              ),
            )
          ],
        ),
      ),
    );
  }
}
