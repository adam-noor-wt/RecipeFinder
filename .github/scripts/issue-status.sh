#!/usr/bin/env bash
# Usage: issue-status.sh <issue-number> <message>
# Env:   GH_TOKEN, GH_REPO (required); FINAL=true to force a new, notifying comment.
#
# Progress messages edit the bot's previous status comment if it is still the LAST comment on
# the issue; otherwise (someone commented in between) a new comment is posted.
# FINAL messages (questions, "Confirm" prompt, PR opened, failures) are never edited into place:
# they replace the previous editable status comment with a fresh one, because edits don't notify people.
set -euo pipefail
N="$1"; MSG="$2"
STATUS='<!-- claude-status -->'; FINALM='<!-- claude-final -->'

BODY="$STATUS"$'\n'
[ "${FINAL:-false}" = "true" ] && BODY+="$FINALM"$'\n'
BODY+="$MSG"

# id <tab> true if the last comment is an editable (non-final) status comment by the bot
LAST=$(gh api --paginate "/repos/${GH_REPO}/issues/${N}/comments" --jq '.[] | [.id, (.user.login == "github-actions[bot]" and (.body | contains("<!-- claude-status -->")) and ((.body | contains("<!-- claude-final -->")) | not))] | @tsv' | tail -n1 || true)
ID=$(cut -f1 <<<"$LAST"); EDITABLE=$(cut -f2 <<<"$LAST")

if [ "$EDITABLE" = "true" ] && [ "${FINAL:-false}" != "true" ]; then
  gh api --method PATCH "/repos/${GH_REPO}/issues/comments/${ID}" -f body="$BODY" >/dev/null
else
  if [ "$EDITABLE" = "true" ]; then
    gh api --method DELETE "/repos/${GH_REPO}/issues/comments/${ID}" >/dev/null
  fi
  gh issue comment "$N" --body "$BODY" >/dev/null
fi
