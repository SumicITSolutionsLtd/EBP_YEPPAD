import 'package:flutter/material.dart';
import 'question_model.dart';

class QuestionCard extends StatelessWidget {
  final Question question;
  final VoidCallback onLike;
  final VoidCallback onReplyTap;

  const QuestionCard({
    super.key,
    required this.question,
    required this.onLike,
    required this.onReplyTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 10, horizontal: 12),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
      elevation: 3,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // header
            Row(
              children: [
                const CircleAvatar(
                  backgroundColor: Colors.teal,
                  child: Icon(Icons.person, color: Colors.white),
                ),
                const SizedBox(width: 10),
                Text(
                  question.userName,
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                  ),
                ),
                const Spacer(),
                Text(
                  "${question.likes} 👍",
                  style: const TextStyle(color: Colors.grey),
                )
              ],
            ),
            const SizedBox(height: 10),

            // question text
            Text(
              question.questionText,
              style: const TextStyle(fontSize: 15),
            ),
            const SizedBox(height: 12),

            // like & reply row
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    IconButton(
                      onPressed: onLike,
                      icon: const Icon(Icons.thumb_up_alt_outlined),
                      color: Colors.teal,
                    ),
                    Text('${question.likes} Likes'),
                  ],
                ),
                TextButton.icon(
                  onPressed: onReplyTap,
                  icon: const Icon(Icons.reply, color: Colors.teal),
                  label: Text(
                    "${question.replies.length} Replies",
                    style: const TextStyle(color: Colors.teal),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
