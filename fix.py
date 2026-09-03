import sys

ad_manager_path = "app/src/main/java/com/spinel/tolstoyreader/ads/AdManager.kt"
with open(ad_manager_path, 'r') as f:
    content = f.read()

bad_string = """    const val BANNER_AD_UNIT_ID = "ca-app-pub-9118481973136364/9357800592"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-9118481973136364/7083980728" // Re-added const val BANNER_AD_UNIT_ID = "ca-app-pub-9118481973136364/9357800592\""""
good_string = """    const val BANNER_AD_UNIT_ID = "ca-app-pub-9118481973136364/9357800592"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-9118481973136364/7083980728\""""

content = content.replace(bad_string, good_string)

with open(ad_manager_path, 'w') as f:
    f.write(content)
print("Cleaned up AdManager.kt")
