#!/usr/bin/env bash
# sync-skills.sh - Symlink or sync skills into an agent's active skills directory.
# Conforms to the Agent Skills standard (SKILL.md in a named folder).

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEFAULT_TARGET="${HOME}/.gemini/skills"
TARGET_DIR="${DEFAULT_TARGET}"
DRY_RUN=false
SPECIFIC_SKILL=""

usage() {
  cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Options:
  -t, --target <path>   Destination directory for symlinks (default: ${DEFAULT_TARGET})
  -s, --skill <name>    Sync a specific skill by folder name (e.g. idea-brutal-audit)
  -l, --list            List all available skills found in this library
  -n, --dry-run         Show what would be linked without creating links
  -h, --help            Show this help message

Examples:
  $(basename "$0")                              # Links all completed skills to ~/.gemini/skills/
  $(basename "$0") -t ~/.claude/skills          # Links all skills to custom target
  $(basename "$0") -s idea-brutal-audit         # Links only 'idea-brutal-audit'
EOF
  exit 0
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -t|--target)
      TARGET_DIR="$2"
      shift 2
      ;;
    -s|--skill)
      SPECIFIC_SKILL="$2"
      shift 2
      ;;
    -n|--dry-run)
      DRY_RUN=true
      shift
      ;;
    -l|--list)
      echo "=== Discoverable Skills in Library ==="
      find "${SCRIPT_DIR}" -type f -name "SKILL.md" ! -path "*/_templates/*" | sort | while read -r skill_file; do
        skill_dir="$(dirname "${skill_file}")"
        skill_name="$(basename "${skill_dir}")"
        rel_path="${skill_file#${SCRIPT_DIR}/}"
        echo "  • ${skill_name} (${rel_path})"
      done
      exit 0
      ;;
    -h|--help)
      usage
      ;;
    *)
      echo "Error: Unknown argument '$1'" >&2
      usage
      ;;
  esac
done

if [[ "${DRY_RUN}" == "false" ]]; then
  mkdir -p "${TARGET_DIR}"
fi

echo "Syncing skills from: ${SCRIPT_DIR}"
echo "Target directory:    ${TARGET_DIR}"
if [[ "${DRY_RUN}" == "true" ]]; then
  echo "Mode:                DRY RUN (no changes applied)"
fi
echo "----------------------------------------------------"

linked_count=0

while IFS= read -r skill_file; do
  skill_dir="$(dirname "${skill_file}")"
  skill_name="$(basename "${skill_dir}")"

  if [[ -n "${SPECIFIC_SKILL}" && "${skill_name}" != "${SPECIFIC_SKILL}" ]]; then
    continue
  fi

  target_link="${TARGET_DIR}/${skill_name}"

  if [[ "${DRY_RUN}" == "true" ]]; then
    echo "[DRY-RUN] Symlink: ${target_link} -> ${skill_dir}"
  else
    ln -sfn "${skill_dir}" "${target_link}"
    echo "[LINKED] ${skill_name} -> ${target_link}"
  fi
  linked_count=$((linked_count + 1))
done < <(find "${SCRIPT_DIR}" -type f -name "SKILL.md" ! -path "*/_templates/*" | sort)

echo "----------------------------------------------------"
if [[ ${linked_count} -eq 0 ]]; then
  if [[ -n "${SPECIFIC_SKILL}" ]]; then
    echo "Notice: Skill '${SPECIFIC_SKILL}' with a valid SKILL.md was not found."
  else
    echo "Notice: No active SKILL.md files found yet (skills pending population)."
  fi
else
  echo "Sync complete: ${linked_count} skill(s) linked successfully."
fi
