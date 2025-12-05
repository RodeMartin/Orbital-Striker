import csv
import xml.etree.ElementTree as ET
from collections import defaultdict
import os

CSV_PATH = "male_fc_24_players.csv"
OUTPUT_XML = "leagues.xml"


def short_name(full_name: str) -> str:
    """
    'Lamine Yamal'  -> 'Yamal'
    'Cristiano Ronaldo' -> 'Ronaldo''
    """
    if not full_name:
        return ""
    parts = full_name.strip().split()
    return parts[-1] if len(parts) > 0 else full_name


def main():
    # leagues[league_name][club_name] = list of players
    leagues = defaultdict(lambda: defaultdict(list))

    if not os.path.exists(CSV_PATH):
        print(f"❌ Nem találom a CSV-t: {CSV_PATH}")
        return

    with open(CSV_PATH, encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            league = (row.get("league") or "").strip()
            club = (row.get("club") or "").strip()
            full = (row.get("name") or "").strip()
            position = (row.get("position") or "").strip()

            if not league or not club or not full:
                continue

            sname = short_name(full)
            leagues[league][club].append(
                {
                    "short": sname,
                    "full": full,
                    "position": position,
                }
            )

    root = ET.Element("leagues")

    for league_name, clubs in sorted(leagues.items()):
        league_el = ET.SubElement(root, "league", name=league_name)
        for club_name, players in sorted(clubs.items()):
            team_el = ET.SubElement(league_el, "team", name=club_name)
            for p in sorted(players, key=lambda x: x["short"]):
                attrs = {
                    "name": p["short"],
                    "fullName": p["full"],
                }
                if p["position"]:
                    attrs["position"] = p["position"]
                ET.SubElement(team_el, "player", **attrs)

    tree = ET.ElementTree(root)
    tree.write(OUTPUT_XML, encoding="utf-8", xml_declaration=True)
    print(f"✅ leagues.xml legenerálva ide: {os.path.abspath(OUTPUT_XML)}")


if __name__ == "__main__":
    main()
