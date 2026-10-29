#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Pre-commit secret scan.
#
# Run manually:   bash scripts/secret-check.sh
# Runs automatically on every "git commit" once you bootstrap the hook:
#   git init
#   git config core.hooksPath githooks      # one time, right after git init
#
# Scans exactly what git would commit (tracked + untracked, .gitignore honored).
# Exit 0 = clean, exit 1 = secrets found (the commit will be blocked by the hook).
# ---------------------------------------------------------------------------
set -uo pipefail
cd "$(dirname "$0")/.." || exit 1

fail=0

# --- 1. Known-leak needles + provider token formats (zero false positives) --
NEEDLES=(
  'mstrl_[A-Za-z0-9]{16,}'            # Mistral API keys
  '3nZMgFLUtQkMkC3'                   # TiDB Cloud user fragment
  'FJfDm9OZ1hayzyx0'                   # TiDB Cloud password fragment
  'ghp_[A-Za-z0-9]{20,}'               # GitHub PAT
  'github_pat_[A-Za-z0-9_]{20,}'
  'hf_[A-Za-z0-9]{20,}'                # Hugging Face
  'sk-[A-Za-z0-9]{20,}'                # OpenAI / generic sk- keys
  'AKIA[0-9A-Z]{16}'                   # AWS access key id
  'AIza[0-9A-Za-z_-]{35}'              # Google API key
  'xox[abprs]-[A-Za-z0-9-]{10,}'       # Slack tokens
  '-----BEGIN [A-Z ]*PRIVATE KEY-----'
)

# --- 2. Secrets in config-style files (properties/yaml/env only) ------------
CONFIG_GLOBS=(
  '*.properties' '*.yml' '*.yaml' '*.env' '*.env.*' '.env' '.env.*'
)

# Collect the file list git would commit
if git rev-parse --git-dir >/dev/null 2>&1; then
  mapfile -t files < <(git ls-files -co --exclude-standard)
else
  echo "! No git repo yet — scanning all non-ignored candidates (manual mode)."
  mapfile -t files < <(find . -type f \
      -not -path './target/*' -not -path './.idea/*' -not -path './.agents/*' \
      -not -path './.mvn/*' -not -path './node_modules/*' -not -path './.git/*' \
      -not -name '.env' -not -name 'skills-lock.json' \
      -not -name 'AGENTS.md' -not -name 'PLAN.md' -not -name 'TOOLS.md' \
      -not -name 'TABLES.md' -not -name 'HELP.md' \
      | sed 's|^\./||')
fi

# never scan this script itself (it contains the needle definitions)
files=("${files[@]}")
filtered=()
for f in "${files[@]}"; do
  [ "$f" = "scripts/secret-check.sh" ] && continue
  filtered+=("$f")
done
files=("${filtered[@]}")

if [ "${#files[@]}" -eq 0 ]; then
  echo "secret-check: no files to scan."
  exit 0
fi

echo "secret-check: scanning ${#files[@]} files that would be committed..."

# --- Check 1: needles in every file (binary-safe skip) ----------------------
for re in "${NEEDLES[@]}"; do
  while IFS= read -r hit; do
    [ -z "$hit" ] && continue
    echo "  [HARD-FAIL] /$re/  ->  $hit"
    fail=1
  done < <(grep -I -n -E "$re" -- "${files[@]}" 2>/dev/null)
done

# --- Check 2: literal values after password=/api-key= in config files --------
config_files=()
for f in "${files[@]}"; do
  case "$f" in
    *.properties|*.yml|*.yaml|*.env|*.env.*|.env|.env.*) config_files+=("$f") ;;
  esac
done
if [ "${#config_files[@]}" -gt 0 ]; then
  while IFS= read -r hit; do
    [ -z "$hit" ] && continue
    echo "  [SUSPECT]   literal credential in config file  ->  $hit"
    fail=1
  done < <(grep -I -n -E '^[^#]*(password|passwd|api[_-]?key|apikey|secret|token)[[:space:]]*[:=][[:space:]]*[^$[:space:]"{[]' \
            -- "${config_files[@]}" 2>/dev/null)
fi

# --- Check 3: files that must never be tracked ------------------------------
if git rev-parse --git-dir >/dev/null 2>&1; then
  for forbidden in .env target; do
    if git ls-files --error-unmatch "$forbidden" >/dev/null 2>&1 || \
       git ls-files -- "$forbidden" | grep -q .; then
      echo "  [HARD-FAIL] '$forbidden' is tracked by git — git rm --cached $forbidden"
      fail=1
    fi
  done
fi

if [ "$fail" -ne 0 ]; then
  echo ""
  echo "secret-check: FAILED — remove/replace the values above (move real secrets to .env),"
  echo "then re-run: bash scripts/secret-check.sh"
  exit 1
fi

echo "secret-check: clean — no secrets in the files about to be committed."
exit 0
