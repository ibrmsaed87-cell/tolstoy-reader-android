import sys
import re

with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'r') as f:
    content = f.read()

content = re.sub(r'private var pendingAction: \(\(\) -> Unit\)\? = null\s*fun showInterstitialOnTransition\(activity: Activity, onContinue: \(\) -> Unit\) \{', 
                 r'fun showInterstitialOnTransition(activity: Activity, onContinue: () -> Unit) {', 
                 content)

content = content.replace("pendingAction = onContinue // Store the action to prevent closure overwrite issues", "val targetAction = onContinue")
content = content.replace("pendingAction?.invoke()\n                    pendingAction = null", "targetAction()")

with open('app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt', 'w') as f:
    f.write(content)
print("Fixed AdManager")
