import sqlite3
import os

db_path = "app/src/main/assets/databases/app_database.db"
if not os.path.exists(db_path):
    print("No DB found at assets. Will check locally if running on device...")

# Just print a script to run in the app
