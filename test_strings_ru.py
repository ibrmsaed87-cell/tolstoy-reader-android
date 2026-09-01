with open('app/src/main/res/values-ru/strings.xml', 'r') as f:
    content = f.read()
if 'fullscreen' not in content.lower():
    new_strings = """
    <string name="fullscreen">На весь экран</string>
    <string name="exit_fullscreen">Выйти из полноэкранного режима</string>
    <string name="zoom_in">Увеличить</string>
    <string name="zoom_out">Уменьшить</string>
    <string name="fit_width">По ширине</string>
    <string name="reset_zoom">Сбросить масштаб</string>
</resources>"""
    content = content.replace('</resources>', new_strings)
    with open('app/src/main/res/values-ru/strings.xml', 'w') as f:
        f.write(content)
