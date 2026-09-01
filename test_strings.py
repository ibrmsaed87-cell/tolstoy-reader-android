import re

with open('app/src/main/res/values/strings.xml', 'r') as f:
    content = f.read()

if 'fullscreen' not in content.lower():
    new_strings = """
    <string name="fullscreen">Fullscreen</string>
    <string name="exit_fullscreen">Exit Fullscreen</string>
    <string name="zoom_in">Zoom In</string>
    <string name="zoom_out">Zoom Out</string>
    <string name="fit_width">Fit to Width</string>
    <string name="reset_zoom">Reset Zoom</string>
</resources>"""
    content = content.replace('</resources>', new_strings)
    with open('app/src/main/res/values/strings.xml', 'w') as f:
        f.write(content)
