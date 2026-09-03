import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

old_click = """                        if (activity != null) {
                            com.spinel.tolstoyreader.ads.AdManager.showRewardedAd(activity) {
                                // Reward: an extra recommendation
                                val randomBook = books.randomOrNull()
                                if (randomBook != null) {
                                    onBookClick(randomBook.id)
                                }
                            }
                        }"""

new_click = """                        if (activity != null) {
                            com.spinel.tolstoyreader.ads.AdManager.showRewardedAd(
                                activity = activity,
                                onAdNotReady = {
                                    android.widget.Toast.makeText(context, context.getString(com.spinel.tolstoyreader.R.string.ad_not_ready), android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onRewardEarned = {
                                    // Reward: an extra recommendation
                                    val randomBook = books.randomOrNull()
                                    if (randomBook != null) {
                                        onBookClick(randomBook.id)
                                    }
                                }
                            )
                        }"""

if old_click in content:
    content = content.replace(old_click, new_click)
    with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/HomeScreen.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Not found")

