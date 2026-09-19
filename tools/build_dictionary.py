"""Build the offline dictionary SQLite asset from licensed source datasets."""

from __future__ import annotations

import argparse
import json
import re
import sqlite3
import unicodedata
from pathlib import Path


STRUCTURES = {
    "D0": "独体结构", "D1": "镶嵌结构", "A0": "品字形结构",
    "B0": "上下结构", "B1": "上下结构", "B2": "上下结构", "B3": "上下结构", "B4": "田字结构",
    "E0": "上中下结构", "E1": "上中下结构", "E2": "上中下结构",
    "H0": "左右结构", "H1": "左右结构", "H2": "左右结构", "H3": "左右结构",
    "M0": "左中右结构", "M1": "左中右结构", "M2": "左中右结构",
    "Q0": "全包围结构", "R0": "半包围结构", "R1": "半包围结构",
    "R2": "半包围结构", "R3": "半包围结构", "R4": "半包围结构",
    "R5": "半包围结构", "R6": "半包围结构",
}

HAN_WORD = re.compile(r"^[\u3400-\u9fff]{2,4}$")


def load_comma_separated_objects(path: Path) -> list[dict]:
    text = path.read_text(encoding="utf-8").strip()
    if text.endswith(","):
        text = text[:-1]
    return json.loads(f"[{text}]")


def compact_text(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip()


def normalize_pinyin(value: str) -> str:
    decomposed = unicodedata.normalize("NFD", value.replace("ɡ", "g").lower())
    plain = "".join(ch for ch in decomposed if unicodedata.category(ch) != "Mn")
    return plain.replace("ü", "v")


def extract_details(detail: dict | None) -> tuple[list[str], list[str]]:
    if not detail:
        return ["暂无详细释义。"], []

    meanings: list[str] = []
    phrases: list[str] = []
    for pronunciation in detail.get("pronunciations", []):
        for explanation in pronunciation.get("explanations", []):
            content = compact_text(explanation.get("content", ""))
            if content and content not in meanings:
                is_etymology = content.startswith(("(", "（")) and content.endswith((")。", "）。", ")", "）"))
                if not is_etymology and content != "同本义。" and len(content) <= 120:
                    meanings.append(content)
            raw_word_items = explanation.get("words", [])
            flattened_word_items = []
            for word_item in raw_word_items:
                if isinstance(word_item, list):
                    flattened_word_items.extend(word_item)
                else:
                    flattened_word_items.append(word_item)
            for word_item in flattened_word_items:
                if isinstance(word_item, dict):
                    raw_word = compact_text(str(word_item.get("word", "")))
                elif isinstance(word_item, str):
                    raw_word = compact_text(word_item)
                else:
                    continue
                for word in re.split(r"[;；]", raw_word):
                    word = word.strip()
                    if 1 < len(word) <= 8 and word not in phrases:
                        phrases.append(word)
    return (meanings[:8] or ["暂无详细释义。"], phrases)


def rank_phrases(
    character: str,
    phrases: list[str],
    character_frequency: dict[str, int],
    modern_words: dict[str, list[str]],
) -> list[str]:
    candidates = [phrase for phrase in phrases if character in phrase and 2 <= len(phrase) <= 4]

    def score(phrase: str) -> tuple[int, int, int, int, str]:
        frequencies = [character_frequency.get(item, 6) for item in phrase]
        return (
            max(frequencies),
            sum(frequencies),
            0 if len(phrase) == 2 else 1,
            0 if phrase.startswith(character) else 1,
            phrase,
        )

    result: list[str] = []
    for phrase in modern_words.get(character, []) + sorted(candidates, key=score):
        if phrase not in result:
            result.append(phrase)
        if len(result) == 12:
            break
    return result


def load_modern_words(path: Path, common_chars: set[str]) -> dict[str, list[str]]:
    """Index short HSK words by character, ordered by level then corpus frequency."""
    rows = json.loads(path.read_text(encoding="utf-8"))
    ranked_words: list[tuple[int, int, int, str]] = []
    for row in rows:
        word = compact_text(str(row.get("simplified", "")))
        if not HAN_WORD.fullmatch(word):
            continue
        levels = []
        for value in row.get("level", []):
            match = re.fullmatch(r"new-(\d+)\+?", value)
            if match:
                levels.append(int(match.group(1)))
        ranked_words.append(
            (
                min(levels, default=99),
                int(row.get("frequency") or 999_999),
                len(word),
                word,
            )
        )

    modern_words: dict[str, list[str]] = {}
    for _, _, _, word in sorted(ranked_words):
        for character in set(word):
            if character in common_chars:
                modern_words.setdefault(character, []).append(word)
    return modern_words


def build_database(source_dir: Path, output: Path) -> None:
    base_rows = load_comma_separated_objects(source_dir / "char_common_base.json")
    detail_rows = load_comma_separated_objects(source_dir / "char_common_detail.json")
    related_rows = json.loads((source_dir / "related.json").read_text(encoding="utf-8"))

    details = {row["char"]: row for row in detail_rows}
    related = {row["char"]: row for row in related_rows}
    common_chars = {row["char"] for row in base_rows}
    character_frequency = {row["char"]: int(row.get("frequency", 6)) for row in base_rows}
    modern_words = load_modern_words(source_dir / "hsk_complete.json", common_chars)

    graphics: dict[str, list[str]] = {}
    with (source_dir / "graphics.txt").open(encoding="utf-8") as handle:
        for line in handle:
            row = json.loads(line)
            character = row.get("character")
            if character in common_chars:
                graphics[character] = row.get("strokes", [])

    output.parent.mkdir(parents=True, exist_ok=True)
    if output.exists():
        output.unlink()

    connection = sqlite3.connect(output)
    try:
        connection.executescript(
            """
            PRAGMA journal_mode=OFF;
            PRAGMA synchronous=OFF;
            PRAGMA temp_store=MEMORY;
            CREATE TABLE characters (
                character TEXT PRIMARY KEY NOT NULL,
                pinyin TEXT NOT NULL,
                pinyin_search TEXT NOT NULL,
                radical TEXT NOT NULL,
                stroke_count INTEGER NOT NULL,
                structure TEXT NOT NULL,
                frequency INTEGER NOT NULL,
                meanings TEXT NOT NULL,
                phrases TEXT NOT NULL,
                related TEXT NOT NULL,
                stroke_paths TEXT NOT NULL
            ) WITHOUT ROWID;
            CREATE INDEX characters_pinyin ON characters(pinyin_search);
            CREATE TABLE metadata (key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL) WITHOUT ROWID;
            """
        )

        insert_sql = """
            INSERT INTO characters (
                character, pinyin, pinyin_search, radical, stroke_count, structure, frequency,
                meanings, phrases, related, stroke_paths
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """
        payload = []
        for base in base_rows:
            character = base["char"]
            pinyin_values = [value.replace("ɡ", "g") for value in base.get("pinyin", [])]
            meanings, phrases = extract_details(details.get(character))
            phrases = rank_phrases(character, phrases, character_frequency, modern_words)
            relation = related.get(character, {})
            related_chars = []
            for relation_type in ("synonyms", "antonyms", "likeness"):
                for item in relation.get(relation_type, []):
                    if item not in related_chars:
                        related_chars.append(item)
            payload.append(
                (
                    character,
                    " / ".join(pinyin_values),
                    " ".join(normalize_pinyin(value) for value in pinyin_values),
                    base.get("radicals", "—"),
                    int(base.get("strokes", 0)),
                    STRUCTURES.get(base.get("structure", ""), "其他结构"),
                    int(base.get("frequency", 6)),
                    json.dumps(meanings, ensure_ascii=False, separators=(",", ":")),
                    json.dumps(phrases, ensure_ascii=False, separators=(",", ":")),
                    json.dumps(related_chars[:10], ensure_ascii=False, separators=(",", ":")),
                    json.dumps(graphics.get(character, []), ensure_ascii=False, separators=(",", ":")),
                )
            )

        connection.executemany(insert_sql, payload)
        connection.executemany(
            "INSERT INTO metadata(key, value) VALUES (?, ?)",
            [
                ("character_count", str(len(payload))),
                ("stroke_character_count", str(len(graphics))),
                ("dictionary_source", "mapull/chinese-dictionary common character data"),
                ("stroke_source", "skishore/makemeahanzi graphics.txt"),
                ("phrase_source", "jelleverheyen/hsk-vocabulary"),
            ],
        )
        connection.commit()
        connection.execute("PRAGMA user_version=3")
        connection.execute("VACUUM")
    finally:
        connection.close()

    print(f"Created {output} ({output.stat().st_size:,} bytes)")
    print(f"Characters: {len(payload):,}; stroke graphics: {len(graphics):,}")


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    build_database(args.source, args.output)


if __name__ == "__main__":
    main()
