#!/usr/bin/env python3
"""Maintain a token-usage table inside a markdown body (issue or PR description).

Each usage entry is stored as a hidden marker  <!-- claude-usage:{json} -->  inside a
<!-- usage-table-start --> ... <!-- usage-table-end -->  region, so later workflows
(triage -> agent -> PR review) can keep adding to the same table.

usage_update.py BODY_FILE [--add JSON] [--add-file FILE] [--add-dir DIR]
                          [--extra-source FILE] [--mode body|table|total]
  body   (default) print BODY with its usage region regenerated at the end
  table  print just the markdown table
  total  print the grand total of tokens
"""
import argparse, glob, json, os, re

START, END = "<!-- usage-table-start -->", "<!-- usage-table-end -->"
MARK = re.compile(r"<!-- claude-usage:(\{.*?\}) -->")
ORDER = ["triage", "implement", "debug", "review"]
FIELDS = ["input", "output", "cache_read", "cache_creation", "total"]


def read(path):
    return open(path, encoding="utf-8").read() if path and os.path.exists(path) else ""


def parse(text):
    return [json.loads(m) for m in MARK.findall(text)]


def aggregate(entries):
    stages = {}
    for e in entries:
        s = stages.setdefault(e.get("stage", "?"), {**{f: 0 for f in FIELDS}, "runs": 0})
        for f in FIELDS:
            s[f] += int(e.get(f, 0) or 0)
        s["runs"] += 1
    names = sorted(stages, key=lambda n: (ORDER.index(n) if n in ORDER else len(ORDER), n))
    return names, stages


def table(entries):
    if not entries:
        return "_No usage data._"
    names, st = aggregate(entries)
    n = lambda v: f"{v:,}"
    rows = ["| Stage | Input | Output | Cache | Total |", "|---|---|---|---|---|"]
    for name in names:
        s = st[name]
        label = name + (f" (×{s['runs']})" if s["runs"] > 1 else "")
        rows.append(f"| {label} | {n(s['input'])} | {n(s['output'])} | {n(s['cache_read'] + s['cache_creation'])} | {n(s['total'])} |")
    tot = {f: sum(st[x][f] for x in names) for f in FIELDS}
    rows.append(f"| **Total** | {n(tot['input'])} | {n(tot['output'])} | {n(tot['cache_read'] + tot['cache_creation'])} | **{n(tot['total'])}** |")
    return "\n".join(rows)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("body")
    ap.add_argument("--add", action="append", default=[])
    ap.add_argument("--add-file", action="append", default=[])
    ap.add_argument("--add-dir")
    ap.add_argument("--extra-source", action="append", default=[])
    ap.add_argument("--mode", choices=["body", "table", "total"], default="body")
    a = ap.parse_args()

    text = read(a.body)
    entries = parse(text)
    for f in a.extra_source:
        entries += parse(read(f))
    entries += [json.loads(j) for j in a.add]
    entries += [json.loads(read(f)) for f in a.add_file if os.path.exists(f)]
    if a.add_dir:
        entries += [json.loads(read(f)) for f in sorted(glob.glob(os.path.join(a.add_dir, "*", "usage.json")))]

    if a.mode == "total":
        print(sum(int(e.get("total", 0) or 0) for e in entries))
    elif a.mode == "table":
        print(table(entries))
    else:
        base = re.sub(re.escape(START) + r".*?" + re.escape(END), "", text, flags=re.S).rstrip()
        markers = "\n".join(
            "<!-- claude-usage:" + json.dumps({k: e.get(k, 0) if k != "stage" else e.get("stage", "?") for k in ["stage"] + FIELDS}, separators=(",", ":")) + " -->"
            for e in entries)
        region = f"{START}\n## Token usage\n\n{table(entries)}\n\n{markers}\n{END}"
        print((base + "\n\n" if base else "") + region)


main()
