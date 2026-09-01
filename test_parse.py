import re

id = "ar-war-and-peace-book-1"
title = "الحرب والسلام – الجزء الأول"

match = re.search(r'-(book|part|tom)-(\d+)$', id)
if match:
    seriesId = id[:match.start()]
    seriesOrder = int(match.group(2))
    seriesTitle = title.split(' – ')[0]
    print(f"seriesId: {seriesId}, seriesOrder: {seriesOrder}, seriesTitle: {seriesTitle}")

