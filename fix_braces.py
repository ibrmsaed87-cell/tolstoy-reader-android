with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

# Let's count { and } in topBar
top_bar_start = content.find('topBar = {')
bottom_bar_start = content.find('bottomBar = {')
print(f"top_bar block chars: {content[top_bar_start:bottom_bar_start].count('{')} vs {content[top_bar_start:bottom_bar_start].count('}')}")

bottom_bar_start = content.find('bottomBar = {')
fab_start = content.find('floatingActionButton = {')
print(f"bottom_bar block chars: {content[bottom_bar_start:fab_start].count('{')} vs {content[bottom_bar_start:fab_start].count('}')}")
