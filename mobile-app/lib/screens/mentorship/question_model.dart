class Reply {
  final String userName;
  final String replyText;
  int likes;

  Reply({
    required this.userName,
    required this.replyText,
    this.likes = 0,
  });
}

class Question {
  final String userName;
  final String questionText;
  final List<Reply> replies;
  int likes;

  Question({
    required this.userName,
    required this.questionText,
    this.replies = const [],
    this.likes = 0,
  });
}
