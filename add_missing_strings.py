import re

strings = {
    'en': {
        'quote_of_the_day': 'Quote of the Day',
        'continue_reading': 'Continue Reading',
        'surprise_me': 'Surprise Me'
    },
    'ar': {
        'quote_of_the_day': 'اقتباس اليوم',
        'continue_reading': 'مواصلة القراءة',
        'surprise_me': 'فاجئني'
    },
    'ru': {
        'quote_of_the_day': 'Цитата дня',
        'continue_reading': 'Продолжить чтение',
        'surprise_me': 'Удиви меня'
    }
}

paths = {
    'en': 'app/src/main/res/values/strings.xml',
    'ar': 'app/src/main/res/values-ar/strings.xml',
    'ru': 'app/src/main/res/values-ru/strings.xml'
}

for lang, path in paths.items():
    with open(path, 'r') as f:
        content = f.read()
    
    new_strings = []
    for key, val in strings[lang].items():
        if f'name="{key}"' not in content:
            new_strings.append(f'    <string name="{key}">{val}</string>')
    
    if new_strings:
        insert_idx = content.rfind('</resources>')
        content = content[:insert_idx] + '\n'.join(new_strings) + '\n' + content[insert_idx:]
        
        with open(path, 'w') as f:
            f.write(content)

