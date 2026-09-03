import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'r') as f:
    content = f.read()
print("pendingAction in content?", "pendingAction" in content)
