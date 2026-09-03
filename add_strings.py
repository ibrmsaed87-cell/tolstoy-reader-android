import sys
import xml.etree.ElementTree as ET

def add_string(file_path, name, value):
    tree = ET.parse(file_path)
    root = tree.getroot()
    
    # Check if already exists
    for child in root:
        if child.attrib.get('name') == name:
            child.text = value
            tree.write(file_path, encoding='utf-8', xml_declaration=True)
            return
            
    elem = ET.SubElement(root, 'string')
    elem.set('name', name)
    elem.text = value
    tree.write(file_path, encoding='utf-8', xml_declaration=True)

add_string('app/src/main/res/values/strings.xml', 'ad_not_ready', 'Ad is not ready yet, please try again.')
add_string('app/src/main/res/values-ar/strings.xml', 'ad_not_ready', 'الإعلان غير جاهز بعد، حاول مرة أخرى.')
add_string('app/src/main/res/values-ru/strings.xml', 'ad_not_ready', 'Реклама еще не готова, попробуйте еще раз.')

