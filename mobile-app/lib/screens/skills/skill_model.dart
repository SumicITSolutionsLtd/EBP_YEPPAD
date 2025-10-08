class Skill {
  final String id;
  final String name;
  final String category;
  final double? price;
  final String location;
  final String description;
  final String imageUrl;
  bool isActive;

  Skill({
    required this.id,
    required this.name,
    required this.category,
    this.price,
    required this.location,
    required this.description,
    required this.imageUrl,
    this.isActive = true,
  });
}
