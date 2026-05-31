import subprocess, random, datetime, os, time, argparse

COMMITS = [
    "test: add consumer pact for new UserService endpoint",
    "feat: add provider state for deleted user scenario",
    "fix: update regex matcher for email validation",
    "docs: add contract testing strategy notes",
    "chore: bump pact-jvm version",
    "refactor: extract pact builder helpers to utils",
    "test: add nullable field matcher for optional role",
    "feat: add can-i-deploy check notes",
    "docs: update README with pact flow diagram",
    "chore: update pom.xml dependency versions",
    "test: add consumer pact for PATCH user endpoint",
    "fix: provider state setup for empty user list",
    "feat: add request header validation to pact",
    "docs: add Pact Broker integration guide",
    "refactor: improve consumer client error handling",
]

FILES = ["NOTES.md", "docs/CHANGELOG.md", "docs/pact-notes.md"]

def ensure_file(path):
    os.makedirs(os.path.dirname(path), exist_ok=True) if os.path.dirname(path) else None
    if not os.path.exists(path):
        open(path, "w").write("# Notes\n")

def commit():
    f = random.choice(FILES)
    ensure_file(f)
    ts = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    open(f, "a").write(f"\n<!-- {ts} -->")
    msg = random.choice(COMMITS)
    subprocess.run(["git", "add", f], check=True)
    r = subprocess.run(["git", "commit", "-m", msg], capture_output=True, text=True)
    if r.returncode == 0:
        subprocess.run(["git", "push"], check=True)
        print(f"[{ts}] ✅ {msg}")

if __name__ == "__main__":
    p = argparse.ArgumentParser()
    p.add_argument("--single", action="store_true")
    args = p.parse_args()
    if args.single:
        commit()
    else:
        for day in range(1, 91):
            print(f"\n📅 Day {day}")
            for _ in range(random.randint(1, 3)):
                commit()
                time.sleep(random.randint(120, 600))
            time.sleep(86400)
