import sys

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'r') as f:
    lines = f.readlines()

start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if 'bottomBar = {' in line:
        start_idx = i
        break

for i in range(start_idx, len(lines)):
    if 'floatingActionButton = {' in lines[i]:
        end_idx = i
        break

replacement = """            bottomBar = {
              AnimatedVisibility(visible = !isFullscreen) {
                Column(modifier = Modifier.fillMaxWidth().background(themeContainerColor)) {
                    if (book != null && book!!.format != "pdf" && !isSearchActive) {
                        BottomAppBar(
                            containerColor = themeContainerColor,
                            contentColor = themeContentColor,
                            tonalElevation = 2.dp,
                            modifier = Modifier.height(64.dp)
                        ) {
                            IconButton(onClick = { isAutoScrolling = !isAutoScrolling }) {
                                Icon(
                                    imageVector = if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = stringResource(id = if (isAutoScrolling) R.string.pause_auto_scroll else R.string.auto_scroll)
                                )
                            }
                            Slider(
                                value = sliderPosition,
                                onValueChange = {
                                    sliderPosition = it
                                    isDraggingSlider = true
                                    isAutoScrolling = false
                                },
                                onValueChangeFinished = {
                                    isDraggingSlider = false
                                    val targetOffset = sliderPosition * totalLength
                                    var accumulated = 0
                                    var targetChapter = 0
                                    for ((i, len) in chapterLengths.withIndex()) {
                                        if (accumulated + len >= targetOffset) {
                                            targetChapter = i
                                            break
                                        }
                                        accumulated += len
                                    }
                                    if (targetChapter >= chapterLengths.size) targetChapter = chapterLengths.size - 1
                                    
                                    val fraction = if (chapterLengths[targetChapter] > 0) {
                                        (targetOffset - accumulated) / chapterLengths[targetChapter].toFloat()
                                    } else 0f
                                    
                                    if (currentChapterIndex != targetChapter) {
                                        currentChapterIndex = targetChapter
                                        pendingScrollFraction = fraction
                                    } else {
                                        coroutineScope.launch {
                                            scrollState.scrollTo((fraction * scrollState.maxValue).toInt())
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            Text(
                                text = "${(sliderPosition * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                        
                        // Add spacing between controls and banner
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha=0.1f))
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Show banner for both PDF and text formats if consent allows
                    if (com.google.android.ump.UserMessagingPlatform.getConsentInformation(context).canRequestAds()) {
                        Box(modifier = Modifier.fillMaxWidth().background(themeContainerColor), contentAlignment = Alignment.Center) {
                            ReaderBannerAd()
                        }
                    }
                }
              }
            },
"""

lines = lines[:start_idx] + [replacement] + lines[end_idx:]

with open('app/src/main/java/com/spinel/tolstoyreader/ui/screens/ReaderScreen.kt', 'w') as f:
    f.writelines(lines)
