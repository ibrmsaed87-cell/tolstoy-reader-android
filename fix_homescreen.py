import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

import re
# Remove Surprise Me entirely (which is now under // 3. Surprise Me)
# Wait, the prompt says: "بعد الحذف يجب أن يصبح ترتيب Home طبيعيًا: Continue Reading إذا كان موجودًا, Surprise Me, Library"
# Ah! The user said: "احذف بالكامل من HomeScreen الزر الظاهر حاليًا: "شاهد إعلانًا واحصل على اقتراح إضافي" واحذف المساحة الخاصة به من الصفحة الرئيسية."
# So I should ONLY remove the Rewarded Ad button (which is "شاهد إعلانًا واحصل على اقتراح إضافي" or something similar). 
# Let's check what the string is. It's `rewarded_ad_button`.
# The "Surprise Me" button is a separate button! Let's keep it.

pattern = r"            // 3.5 Rewarded Ad.*?            item\(span = \{ GridItemSpan\(2\) \}\) \{.*?\n            \}\n"
new_content = re.sub(pattern, "", content, flags=re.DOTALL)

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(new_content)

print("Done HomeScreen")
