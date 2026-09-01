with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("scaleX = pdfScale / renderScale,", "scaleX = pdfScale,")
content = content.replace("scaleY = pdfScale / renderScale,", "scaleY = pdfScale,")

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write(content)
