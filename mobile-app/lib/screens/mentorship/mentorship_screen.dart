import 'package:flutter/material.dart';
import 'question_card.dart';
import 'question_model.dart';
import 'ask_question_sheet.dart';

class MentorshipScreen extends StatefulWidget {
  const MentorshipScreen({super.key});

  @override
  State<MentorshipScreen> createState() => _MentorshipScreenState();
}

class _MentorshipScreenState extends State<MentorshipScreen> {
  final List<Question> _questions = [];

  void _addQuestion(String text) {
    setState(() {
      _questions.insert(
        0,
        Question(userName: "You", questionText: text, replies: [], likes: 0),
      );
    });
  }

  void _likeQuestion(int index) {
    setState(() {
      _questions[index].likes++;
    });
  }

  void _addReply(int index, String replyText) {
    setState(() {
      _questions[index].replies.add(
        Reply(userName: "Mentor", replyText: replyText),
      );
    });
  }

  void _openReplies(int index) {
    final question = _questions[index];
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (_) {
        final replyController = TextEditingController();
        return Padding(
          padding: MediaQuery.of(context).viewInsets,
          child: Container(
            height: MediaQuery.of(context).size.height * 0.75,
            padding: const EdgeInsets.all(16),
            decoration: const BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  question.questionText,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                    color: Colors.teal,
                  ),
                ),
                const Divider(),
                Expanded(
                  child: ListView.builder(
                    itemCount: question.replies.length,
                    itemBuilder: (context, i) {
                      final reply = question.replies[i];
                      return ListTile(
                        leading: const Icon(Icons.person, color: Colors.teal),
                        title: Text(reply.userName),
                        subtitle: Text(reply.replyText),
                        trailing: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            IconButton(
                              icon: const Icon(Icons.thumb_up_alt_outlined),
                              onPressed: () {
                                setState(() => reply.likes++);
                              },
                            ),
                            Text("${reply.likes}"),
                          ],
                        ),
                      );
                    },
                  ),
                ),
                Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: replyController,
                        decoration: InputDecoration(
                          hintText: "Write a reply...",
                          border: OutlineInputBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                        ),
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.mic, color: Colors.teal),
                      onPressed: () {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(content: Text("Voice reply coming soon 🎙️")),
                        );
                      },
                    ),
                    IconButton(
                      icon: const Icon(Icons.send, color: Colors.teal),
                      onPressed: () {
                        if (replyController.text.isNotEmpty) {
                          _addReply(index, replyController.text.trim());
                          replyController.clear();
                        }
                      },
                    ),
                  ],
                )
              ],
            ),
          ),
        );
      },
    );
  }

  void _openAskSheet() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      builder: (_) => AskQuestionSheet(onSubmit: _addQuestion),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF1FDFB),
      appBar: AppBar(
        title: const Text(
          "Mentorship Hub",
          style: TextStyle(color: Colors.white),
        ),
        backgroundColor: Colors.purpleAccent,
        centerTitle: true,
      ),
      body: _questions.isEmpty
          ? const Center(
        child: Text(
          "No questions yet. Ask your first question!",
          style: TextStyle(fontSize: 16, color: Colors.grey),
        ),
      )
          : ListView.builder(
        itemCount: _questions.length,
        itemBuilder: (context, index) {
          final question = _questions[index];
          return QuestionCard(
            question: question,
            onLike: () => _likeQuestion(index),
            onReplyTap: () => _openReplies(index),
          );
        },
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _openAskSheet,
        backgroundColor: Colors.purpleAccent,
        icon: const Icon(Icons.add_comment_rounded),
        label: const Text("Ask Mentor"),
      ),
    );
  }
}
