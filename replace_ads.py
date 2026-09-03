import sys

manifest_path = "app/src/main/AndroidManifest.xml"
with open(manifest_path, 'r') as f:
    content = f.read()
content = content.replace("ca-app-pub-3940256099942544~3347511713", "ca-app-pub-9118481973136364~1670882267")
with open(manifest_path, 'w') as f:
    f.write(content)
print("Updated AndroidManifest.xml")

ad_manager_path = "app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt"
with open(ad_manager_path, 'r') as f:
    content = f.read()

content = content.replace("ca-app-pub-3940256099942544/9257395921", "ca-app-pub-9118481973136364/6369664858")
content = content.replace("ca-app-pub-3940256099942544/1033173712", "ca-app-pub-9118481973136364/3893720920")
content = content.replace("ca-app-pub-3940256099942544/2247696110", "ca-app-pub-9118481973136364/1277120271")
content = content.replace("ca-app-pub-3940256099942544/9214589741", "ca-app-pub-9118481973136364/9357800592")

# Let's add REWARDED_AD_UNIT_ID if it doesn't exist
if "REWARDED_AD_UNIT_ID" not in content:
    content = content.replace("const val BANNER_AD_UNIT_ID", 'const val BANNER_AD_UNIT_ID = "ca-app-pub-9118481973136364/9357800592"\n    const val REWARDED_AD_UNIT_ID = "ca-app-pub-9118481973136364/7083980728" // Re-added const val BANNER_AD_UNIT_ID')
    # wait, my replace is wrong if I don't match the whole line.
    
with open(ad_manager_path, 'w') as f:
    f.write(content)
print("Updated AdManager.kt")
