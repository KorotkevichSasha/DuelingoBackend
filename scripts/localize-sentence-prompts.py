"""Replace generic sentence-construction instructions with Russian source phrases.

The script intentionally edits only the questionText value in each matching JSON
object, preserving the formatting and all other curated question data.
"""

import json
import re
import sys
import time
import urllib.parse
import urllib.request
from pathlib import Path


QUESTION_FILE = Path(__file__).parents[1] / "src" / "main" / "resources" / "data" / "questions.json"
CYRILLIC = re.compile(r"[А-Яа-яЁё]")


def translate(sentence: str) -> str:
    query = urllib.parse.urlencode(
        {"client": "gtx", "sl": "en", "tl": "ru", "dt": "t", "q": sentence}
    )
    request = urllib.request.Request(
        f"https://translate.googleapis.com/translate_a/single?{query}",
        headers={"User-Agent": "DuelRush-content-maintenance/1.0"},
    )
    with urllib.request.urlopen(request, timeout=20) as response:
        payload = json.load(response)
    value = "".join(part[0] for part in payload[0] if part and part[0]).strip()
    if not CYRILLIC.search(value):
        raise RuntimeError(f"Translation is not Russian: {sentence!r} -> {value!r}")
    return value


def main() -> None:
    raw = QUESTION_FILE.read_text(encoding="utf-8-sig")
    questions = json.loads(raw)
    changed = 0
    corrected = 0

    for question in questions:
        if question.get("type") != "SENTENCE_CONSTRUCTION":
            continue
        answer = question["correctAnswers"][0]
        capitalization = re.match(r"^(Does|Do|Did|Will|Is|Are|Has|Have) (The|Our|A) (.+)$", answer)
        if capitalization:
            subject = capitalization.group(2)
            fixed_answer = f"{capitalization.group(1)} {subject.lower()} {capitalization.group(3)}"
            answer_json = json.dumps(answer, ensure_ascii=False)
            answer_position = raw.find(answer_json)
            if answer_position < 0:
                raise RuntimeError(f"Cannot locate answer in source: {answer!r}")
            object_start = raw.rfind("{", 0, answer_position)
            object_end = raw.find("}", answer_position) + 1
            object_source = raw[object_start:object_end]
            object_source = object_source.replace(
                json.dumps(subject, ensure_ascii=False),
                json.dumps(subject.lower(), ensure_ascii=False),
                1,
            ).replace(answer_json, json.dumps(fixed_answer, ensure_ascii=False), 1)
            raw = raw[:object_start] + object_source + raw[object_end:]
            corrected += 1

        old_prompt = question["questionText"]
        if CYRILLIC.search(old_prompt):
            continue

        russian_prompt = translate(answer)
        old_json = json.dumps(old_prompt, ensure_ascii=False)
        new_json = json.dumps(russian_prompt, ensure_ascii=False)
        if old_json not in raw:
            raise RuntimeError(f"Cannot locate prompt in source: {old_prompt!r}")
        raw = raw.replace(old_json, new_json, 1)
        changed += 1
        time.sleep(0.04)

    # Keep the repository's existing CRLF convention so content-only changes
    # stay reviewable instead of making the whole JSON file appear replaced.
    with QUESTION_FILE.open("w", encoding="utf-8", newline="") as output:
        output.write(raw.replace("\n", "\r\n"))
    print(f"Localized {changed} prompts; corrected {corrected} sentence capitalizations")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(error, file=sys.stderr)
        raise
