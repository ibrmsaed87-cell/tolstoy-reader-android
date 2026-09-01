import re

strings = {
    'en': {
        'nav_home': 'Home',
        'nav_search': 'Search',
        'nav_favorites': 'Favorites',
        'search_books': 'Search books',
        'no_books_found': 'No books found',
        'no_favorite_books_yet': 'No favorite books yet',
        'add_to_favorites': 'Add to favorites',
        'remove_from_favorites': 'Remove from favorites'
    },
    'ar': {
        'nav_home': 'الرئيسية',
        'nav_search': 'البحث',
        'nav_favorites': 'المفضلة',
        'search_books': 'البحث عن كتب',
        'no_books_found': 'لا توجد كتب',
        'no_favorite_books_yet': 'لا توجد كتب مفضلة بعد',
        'add_to_favorites': 'أضف للمفضلة',
        'remove_from_favorites': 'إزالة من المفضلة'
    },
    'ru': {
        'nav_home': 'Главная',
        'nav_search': 'Поиск',
        'nav_favorites': 'Избранное',
        'search_books': 'Поиск книг',
        'no_books_found': 'Книги не найдены',
        'no_favorite_books_yet': 'Пока нет любимых книг',
        'add_to_favorites': 'В избранное',
        'remove_from_favorites': 'Убрать из избранного'
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

