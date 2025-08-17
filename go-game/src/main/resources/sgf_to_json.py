import sys, json, re

def sgf_to_json(sgf_content, problem_id):
    size_match = re.search(r"SZ\[(\d+)\]", sgf_content)
    size = int(size_match.group(1)) if size_match else 19
    to_play = "B" if "PL[B]" in sgf_content else "W"
    stones = []
    for color, tag in [("B", "AB"), ("W", "AW")]:
        for m in re.finditer(rf"{tag}\[([a-i])([a-i])\]", sgf_content):
            x = ord(m.group(1)) - ord('a') + 1
            y = ord(m.group(2)) - ord('a') + 1
            stones.append({"x": x, "y": y, "c": color})
    return {
        "id": problem_id,
        "size": size,
        "toPlay": to_play,
        "goal": "UNKNOWN",
        "ko": None,
        "initialStones": stones,
        "hints": [],
        "answer": []
    }

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python sgf_to_json.py file.sgf [id]")
        sys.exit(1)
    fname = sys.argv[1]
    problem_id = sys.argv[2] if len(sys.argv) > 2 else fname.split("/")[-1].split(".")[0]
    with open(fname, "r", encoding="utf-8") as f:
        sgf = f.read()
    obj = sgf_to_json(sgf, problem_id)
    print(json.dumps(obj, indent=2, ensure_ascii=False))
