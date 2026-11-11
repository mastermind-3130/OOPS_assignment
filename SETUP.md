# Team Setup Guide

Read `INSTRUCTIONS.md` first for the full project plan.

## What's done so far

- GitHub repo created with `INSTRUCTIONS.md`
- Spring Boot project generated (Spring Boot 4.1.2, Java 21, Maven) and compiles (`BUILD SUCCESS`)
- Package folders created under `src/main/java/com/itsm` (empty for now)
- PostgreSQL 18.6 installed locally and an `itsm` database created

**Still to do:** connect the app to the database and run it for the first time. Don't start writing features until this works.

---

## What you need to install (all free)

| Tool | Notes |
|---|---|
| **JDK 21** | Check with `java -version`. If missing, install Eclipse Temurin 21 from adoptium.net |
| **VS Code** extensions | "Extension Pack for Java" (Microsoft) and "Spring Boot Extension Pack" (VMware) |
| **Git** | git-scm.com |
| **PostgreSQL 18.6** | postgresql.org → Download → Windows → 18.6, Windows x86-64 |

You do **not** need to install Maven. The project's `mvnw.cmd` downloads it automatically.

---

## Setup steps

### 1. Clone the repo
```
git clone https://github.com/mastermind-3130/OOPS_assignment.git
cd OOPS_assignment
```

### 2. Install PostgreSQL 18.6
- **Right-click the installer → Run as administrator.** Without this you may get a "Microsoft VC++ runtime installer" error.
- Keep all defaults (port **5432**).
- Set a password for the `postgres` user and remember it.
- Untick Stack Builder at the end.

### 3. Create the database
- Open **SQL Shell (psql)** from the Start menu.
- Press Enter 4 times, then type your password. **Nothing shows while typing; that's normal.**
- Run (don't forget the semicolon):
  ```
  CREATE DATABASE itsm;
  ```
- You should see `CREATE DATABASE`. Type `\q` to exit.

### 4. Save your DB password as an environment variable
In the VS Code terminal:
```
setx DB_PASSWORD "your_password_here"
```
Then **close and reopen VS Code completely**.

Never put your password in any file that goes to GitHub.

### 5. Check the project compiles
```
.\mvnw.cmd compile
```
Should end with `BUILD SUCCESS`. Warnings about `sun.misc.Unsafe` are harmless.

### 6. Run the app (after the database config is pushed)
```
.\mvnw.cmd spring-boot:run
```
Look for `Started ItsmApplication`, then open http://localhost:8080. You should see a login page. Stop with `Ctrl+C`.

---

## Git rules

- Always `git pull` before starting work.
- Never push directly to `main`. Work on a branch: `git checkout -b feature/<name>`, push it, and open a pull request.
- Don't commit the `target/` folder or any passwords.
