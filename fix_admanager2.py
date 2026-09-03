import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'r') as f:
    content = f.read()

old_else = """        } else {
            onContinue()
        }
    }"""

new_else = """        } else {
            loadInterstitialAd(activity)
            onContinue()
        }
    }"""

if old_else in content:
    content = content.replace(old_else, new_else)
    with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'w') as f:
        f.write(content)
    print("Success AdManager else")
else:
    print("Not found AdManager else")
