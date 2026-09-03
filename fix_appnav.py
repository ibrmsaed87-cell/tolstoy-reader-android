import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

old_on_read = """                            onReadClick = { id -> 
                                activity?.let {
                                    AdManager.showInterstitialOnTransition(it) {
                                        navController.navigate(Screen.Reader.createRoute(id))
                                    }
                                } ?: run {
                                    navController.navigate(Screen.Reader.createRoute(id))
                                }
                            }"""

new_on_read = """                            onReadClick = { id -> 
                                navController.navigate(Screen.Reader.createRoute(id))
                            }"""

if old_on_read in content:
    content = content.replace(old_on_read, new_on_read)
    with open('app/src/main/java/com/spinel/tolstoyreader/ui/navigation/AppNavigation.kt', 'w') as f:
        f.write(content)
    print("Success AppNavigation")
else:
    print("Not found AppNavigation")
