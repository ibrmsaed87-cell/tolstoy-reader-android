import sys

with open('app/src/main/java/com/spinel/tolstoyreader/data/local/AppDatabase.kt', 'r') as f:
    content = f.read()

old_version = "version = 6"
new_version = "version = 7"
content = content.replace(old_version, new_version)

old_migrations = """.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)"""
new_migrations = """.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)"""
content = content.replace(old_migrations, new_migrations)

migration_code = """
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN seriesId TEXT")
                db.execSQL("ALTER TABLE books ADD COLUMN seriesTitle TEXT")
                db.execSQL("ALTER TABLE books ADD COLUMN seriesOrder INTEGER")
            }
        }
        
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN totalChapters INTEGER NOT NULL DEFAULT 0")
            }
        }
"""
old_mig_5_6 = """        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN seriesId TEXT")
                db.execSQL("ALTER TABLE books ADD COLUMN seriesTitle TEXT")
                db.execSQL("ALTER TABLE books ADD COLUMN seriesOrder INTEGER")
            }
        }"""
content = content.replace(old_mig_5_6, migration_code)

with open('app/src/main/java/com/spinel/tolstoyreader/data/local/AppDatabase.kt', 'w') as f:
    f.write(content)
print("Updated AppDatabase.kt")
