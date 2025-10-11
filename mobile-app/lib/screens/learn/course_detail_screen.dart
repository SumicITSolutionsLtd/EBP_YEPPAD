// lib/screens/learn/course_detail_screen.dart
import 'package:flutter/material.dart';
import 'package:audioplayers/audioplayers.dart';
import 'package:video_player/video_player.dart';
import 'course_model.dart';

class CourseDetailScreen extends StatefulWidget {
  final Course course;
  const CourseDetailScreen({super.key, required this.course});

  @override
  State<CourseDetailScreen> createState() => _CourseDetailScreenState();
}

class _CourseDetailScreenState extends State<CourseDetailScreen> {
  late AudioPlayer _audioPlayer;
  bool _isPlaying = false;

  VideoPlayerController? _videoController;
  bool _videoInitialized = false;

  @override
  void initState() {
    super.initState();
    _audioPlayer = AudioPlayer();
    if (widget.course.videoUrl != null) {
      _videoController = VideoPlayerController.network(widget.course.videoUrl!);
      _videoController!.initialize().then((_) {
        setState(() { _videoInitialized = true; });
      });
    }
  }

  @override
  void dispose() {
    _audioPlayer.dispose();
    _videoController?.dispose();
    super.dispose();
  }

  Future<void> _toggleAudio() async {
    if (widget.course.audioUrl == null) return;
    if (!_isPlaying) {
      await _audioPlayer.play(UrlSource(widget.course.audioUrl!));
      setState(() => _isPlaying = true);
    } else {
      await _audioPlayer.pause();
      setState(() => _isPlaying = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final c = widget.course;
    return Scaffold(
      appBar: AppBar(title: const Text('Course'), backgroundColor: Colors.blueAccent),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Image.network(c.imageUrl, height: 180, fit: BoxFit.cover),
            const SizedBox(height: 12),
            Text(c.title, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Text(c.description),
            const SizedBox(height: 20),
            if (c.audioUrl != null) ...[
              ElevatedButton.icon(
                onPressed: _toggleAudio,
                icon: Icon(_isPlaying ? Icons.pause : Icons.play_arrow),
                label: Text(_isPlaying ? 'Pause Audio' : 'Play Audio'),
              ),
              const SizedBox(height: 12),
            ],
            if (c.videoUrl != null && _videoInitialized) ...[
              AspectRatio(
                aspectRatio: _videoController!.value.aspectRatio,
                child: VideoPlayer(_videoController!),
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  IconButton(
                    icon: Icon(_videoController!.value.isPlaying ? Icons.pause : Icons.play_arrow),
                    onPressed: () {
                      setState(() {
                        if (_videoController!.value.isPlaying) _videoController!.pause();
                        else _videoController!.play();
                      });
                    },
                  ),
                ],
              )
            ] else if (c.videoUrl != null && !_videoInitialized) ...[
              const CircularProgressIndicator()
            ]
          ],
        ),
      ),
    );
  }
}
