# Scheduler project

## SQLite JDBC driver setup

The Eclipse project expects the SQLite JDBC driver to live in `lib/sqlite-jdbc-3.51.0.0.jar`.
If the jar is missing, build path errors will appear. Download the driver into the `lib/` folder
before importing the project:

```bash
mkdir -p lib
curl -L -o lib/sqlite-jdbc-3.51.0.0.jar \
  https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.51.0.0/sqlite-jdbc-3.51.0.0.jar
```

If `curl` is unavailable, replace it with `wget -O lib/sqlite-jdbc-3.51.0.0.jar <url>`.
The downloaded jar is intentionally ignored by Git (see `.gitignore`), so each developer
should download it locally. Once the file is present, refresh the project in Eclipse to
clear the build path error.
