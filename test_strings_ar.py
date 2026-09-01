with open('app/src/main/res/values-ar/strings.xml', 'r') as f:
    content = f.read()
if 'fullscreen' not in content.lower():
    new_strings = """
    <string name="fullscreen">ملء الشاشة</string>
    <string name="exit_fullscreen">الخروج من ملء الشاشة</string>
    <string name="zoom_in">تكبير</string>
    <string name="zoom_out">تصغير</string>
    <string name="fit_width">ملاءمة العرض</string>
    <string name="reset_zoom">إعادة ضبط التكبير</string>
</resources>"""
    content = content.replace('</resources>', new_strings)
    with open('app/src/main/res/values-ar/strings.xml', 'w') as f:
        f.write(content)
