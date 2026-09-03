import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/BookDetailsScreen.kt', 'r') as f:
    content = f.read()

old_image = """                        AsyncImage(
                            model = book!!.coverUrl,
                            contentDescription = book!!.title,
                            modifier = Modifier
                                .height(240.dp)
                                .aspectRatio(0.66f)
                                .shadow(8.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )"""

new_image = """                        AsyncImage(
                            model = book!!.coverUrl,
                            contentDescription = book!!.title,
                            modifier = Modifier
                                .height(240.dp)
                                .aspectRatio(0.66f)
                                .shadow(8.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onReadClick(book!!.id) },
                            contentScale = ContentScale.Crop
                        )"""

if old_image in content:
    content = content.replace(old_image, new_image)
    with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/BookDetailsScreen.kt', 'w') as f:
        f.write(content)
    print("Success BookDetailsScreen")
else:
    print("Not found BookDetailsScreen")
