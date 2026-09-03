import sys

with open('app/src/main/java/com/spinel/tolstoyreader/data/repository/RemoteBookRepository.kt', 'r') as f:
    content = f.read()

# Fix toDomainModel
old_to = """            chapters = chapters.map { Chapter(it.title, it.content) },"""
new_to = """            chapters = chapters.map { Chapter(it.title, it.content) },
            totalChapters = totalChapters,"""
if old_to in content:
    content = content.replace(old_to, new_to)
    print("Fixed toDomainModel")
else:
    print("Failed to fix toDomainModel")

# Fix loadBookContent (inserting chapters)
old_insert = """                        bookDao.insertChapters(chapterEntities)
                        
                        domainBook = updatedEntity.toDomainModel(chapterEntities)"""
new_insert = """                        bookDao.insertChapters(chapterEntities)
                        
                        val finalEntity = updatedEntity.copy(totalChapters = chapterEntities.size)
                        bookDao.updateBook(finalEntity)
                        domainBook = finalEntity.toDomainModel(chapterEntities)"""
if old_insert in content:
    content = content.replace(old_insert, new_insert)
    print("Fixed loadBookContent")
else:
    print("Failed to fix loadBookContent")

# Fix getBooks self-healing
old_get = """                        }
                    }
                }
            }
            
            val mappedLocalBooks = localBooks.map { it.toDomainModel(emptyList()) }"""
new_get = """                        }
                    }
                }
            }
            
            val needsChapterCount = localBooks.filter { it.totalChapters == 0 && it.format == "json" }
            if (needsChapterCount.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    needsChapterCount.forEach { entity ->
                        val count = bookDao.getChapterCountSync(entity.id)
                        if (count > 0) {
                            bookDao.updateBook(entity.copy(totalChapters = count))
                        }
                    }
                }
            }
            
            val mappedLocalBooks = localBooks.map { it.toDomainModel(emptyList()) }"""
if old_get in content:
    content = content.replace(old_get, new_get)
    print("Fixed getBooks")
else:
    print("Failed to fix getBooks")

with open('app/src/main/java/com/spinel/tolstoyreader/data/repository/RemoteBookRepository.kt', 'w') as f:
    f.write(content)
