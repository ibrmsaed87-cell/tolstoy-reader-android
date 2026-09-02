with open("app/build.gradle.kts", "r") as f:
    content = f.read()

import_statement = 'import java.util.Properties\nimport java.io.FileInputStream\n\n'
if import_statement not in content:
    content = import_statement + content

# Modify signingConfig release
old_signing = """    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }"""

new_signing = """    create("release") {
      val keystoreProperties = Properties()
      val keystorePropertiesFile = rootProject.file("keystore.properties")
      if (keystorePropertiesFile.exists()) {
          keystoreProperties.load(FileInputStream(keystorePropertiesFile))
      }
      val storeFileName = keystoreProperties.getProperty("RELEASE_STORE_FILE") ?: "my-upload-key.jks"
      storeFile = rootProject.file(storeFileName)
      storePassword = keystoreProperties.getProperty("RELEASE_STORE_PASSWORD")
      keyAlias = keystoreProperties.getProperty("RELEASE_KEY_ALIAS") ?: "upload"
      keyPassword = keystoreProperties.getProperty("RELEASE_KEY_PASSWORD")
    }"""

content = content.replace(old_signing, new_signing)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)

