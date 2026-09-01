import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

# Remove the search field from HomeScreen
search_pattern = re.compile(r'OutlinedTextField\([^)]+onValueChange = \{ viewModel\.setSearchQuery\(it\) \}[^)]+\)', re.DOTALL)
content = search_pattern.sub('', content)

# Remove the space reserved for the search field
content = content.replace('Spacer(modifier = Modifier.height(16.dp))\n\n                                if', 'if')
content = content.replace('if (searchQuery.isNotEmpty()) {', 'if (false) {')

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

