#!/usr/bin/env python3
import math
import os

from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen

SCRIPT_DIR = os.path.dirname(__file__)
OUTPUT_DIR = os.path.join(SCRIPT_DIR, "../../app/src/commonMain/composeResources/drawable")
FONT_PATH = os.path.join(SCRIPT_DIR, "fonts/Tektur-Bold.ttf")

R = 53
PAD = 0.5
HEX_W = math.ceil(2 * R * math.cos(math.radians(30)) + PAD * 2)
HEX_H = math.ceil(2 * R + PAD * 2)
CX, CY = HEX_W / 2, HEX_H / 2
HEX_R = R - PAD

CONCEALED_BG = "#b0bec5"
REVEALED_BG = "#d6e4f0"

NUMBER_COLORS = {
    1: "#1e88e5",
    2: "#43a047",
    3: "#e53935",
    4: "#5e35b1",
    5: "#d81b60",
    6: "#00897b",
    7: "#3e2723",
    8: "#546e7a",
}

FLAG_COLOR = "#e53935"
MINE_COLOR = "#37474f"


def hex_path(cx, cy, r):
    pts = []
    for i in range(6):
        angle = math.radians(60 * i - 90)
        pts.append((cx + r * math.cos(angle), cy + r * math.sin(angle)))
    d = f"M{pts[0][0]:.2f},{pts[0][1]:.2f}"
    for x, y in pts[1:]:
        d += f"L{x:.2f},{y:.2f}"
    d += "Z"
    return d


def circle_path(cx, cy, r):
    return (
        f"M{cx - r:.2f},{cy:.2f}"
        f"A{r:.2f},{r:.2f},0,1,1,{cx + r:.2f},{cy:.2f}"
        f"A{r:.2f},{r:.2f},0,1,1,{cx - r:.2f},{cy:.2f}Z"
    )


def glyph_path(font, char, target_height, cx, cy):
    cmap = font.getBestCmap()
    glyph_name = cmap[ord(char)]
    glyph_set = font.getGlyphSet()
    pen = SVGPathPen(glyph_set)
    glyph_set[glyph_name].draw(pen)
    path_d = pen.getCommands()
    if not path_d:
        return ""

    head = font["head"]
    upm = head.unitsPerEm
    scale = target_height / upm

    os2 = font["OS/2"]
    ascender = os2.sTypoAscender
    descender = os2.sTypoDescender
    font_height = ascender - descender
    baseline_y = cy + (ascender - font_height / 2) * scale

    glyph = glyph_set[glyph_name]
    width = glyph.width
    offset_x = cx - (width * scale) / 2

    def tx(v):
        return float(v) * scale + offset_x

    def ty(v):
        return baseline_y - float(v) * scale

    import re
    tokens = re.findall(r'[A-Za-z]|[-+]?\d*\.?\d+(?:[eE][-+]?\d+)?', path_d)
    result = []
    i = 0
    cur_x, cur_y = 0.0, 0.0
    while i < len(tokens):
        t = tokens[i]
        if not t.isalpha():
            i += 1
            continue
        cmd = t
        i += 1
        if cmd == 'M' or cmd == 'L':
            result.append(cmd)
            while i < len(tokens) and not tokens[i].isalpha():
                cur_x, cur_y = float(tokens[i]), float(tokens[i + 1])
                result.append(f"{tx(cur_x):.2f},{ty(cur_y):.2f}")
                i += 2
        elif cmd == 'H':
            while i < len(tokens) and not tokens[i].isalpha():
                cur_x = float(tokens[i])
                result.append(f"L{tx(cur_x):.2f},{ty(cur_y):.2f}")
                i += 1
        elif cmd == 'V':
            while i < len(tokens) and not tokens[i].isalpha():
                cur_y = float(tokens[i])
                result.append(f"L{tx(cur_x):.2f},{ty(cur_y):.2f}")
                i += 1
        elif cmd == 'C':
            result.append("C")
            while i < len(tokens) and not tokens[i].isalpha():
                for _ in range(2):
                    result.append(f"{tx(tokens[i]):.2f},{ty(tokens[i+1]):.2f}")
                    i += 2
                cur_x, cur_y = float(tokens[i]), float(tokens[i + 1])
                result.append(f"{tx(cur_x):.2f},{ty(cur_y):.2f}")
                i += 2
        elif cmd == 'Q':
            result.append("Q")
            while i < len(tokens) and not tokens[i].isalpha():
                result.append(f"{tx(tokens[i]):.2f},{ty(tokens[i+1]):.2f}")
                i += 2
                cur_x, cur_y = float(tokens[i]), float(tokens[i + 1])
                result.append(f"{tx(cur_x):.2f},{ty(cur_y):.2f}")
                i += 2
        elif cmd == 'Z':
            result.append("Z")
    return " ".join(result)


def xml_vector(width, height, paths):
    lines = [
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        f'    android:width="{width:.2f}dp"',
        f'    android:height="{height:.2f}dp"',
        f'    android:viewportWidth="{width:.2f}"',
        f'    android:viewportHeight="{height:.2f}">',
    ]
    for p in paths:
        attrs = f'android:pathData="{p["d"]}"'
        if "fill" in p:
            attrs += f' android:fillColor="{p["fill"]}"'
        if "stroke" in p:
            attrs += f' android:strokeColor="{p["stroke"]}"'
        if "strokeWidth" in p:
            attrs += f' android:strokeWidth="{p["strokeWidth"]}"'
        if "strokeCap" in p:
            attrs += f' android:strokeLineCap="{p["strokeCap"]}"'
        if "fillAlpha" in p:
            attrs += f' android:fillAlpha="{p["fillAlpha"]}"'
        lines.append(f"    <path {attrs}/>")
    lines.append("</vector>")
    return "\n".join(lines) + "\n"


def write_xml(name, content):
    path = os.path.join(OUTPUT_DIR, f"{name}.xml")
    with open(path, "w") as f:
        f.write(content)


def gen_backgrounds():
    hp = hex_path(CX, CY, HEX_R)
    for name, fill in [("hex_concealed", CONCEALED_BG), ("hex_revealed", REVEALED_BG)]:
        paths = [{"d": hp, "fill": fill}]
        write_xml(name, xml_vector(HEX_W, HEX_H, paths))


def gen_numbers(font):
    for n, color in NUMBER_COLORS.items():
        gp = glyph_path(font, str(n), R * 1.03, CX, CY)
        if gp:
            paths = [{"d": gp, "fill": color}]
        else:
            paths = []
        write_xml(f"hex_number_{n}", xml_vector(HEX_W, HEX_H, paths))


def gen_mine():
    r = R * 0.29
    paths = [{"d": circle_path(CX, CY, r), "fill": MINE_COLOR}]
    spikes = 8
    spike_in = r - R * 0.03
    spike_out = r + R * 0.16
    sw = R * 0.065
    for i in range(spikes):
        angle = math.radians(360 / spikes * i)
        x1 = CX + spike_in * math.cos(angle)
        y1 = CY + spike_in * math.sin(angle)
        x2 = CX + spike_out * math.cos(angle)
        y2 = CY + spike_out * math.sin(angle)
        paths.append({
            "d": f"M{x1:.1f},{y1:.1f}L{x2:.1f},{y2:.1f}",
            "stroke": MINE_COLOR,
            "strokeWidth": f"{sw:.1f}",
            "strokeCap": "round",
        })
    highlight_r = R * 0.08
    highlight_off = R * 0.08
    paths.append({
        "d": circle_path(CX - highlight_off, CY - highlight_off, highlight_r),
        "fill": "#FFFFFF",
        "fillAlpha": "0.6",
    })
    write_xml("hex_mine", xml_vector(HEX_W, HEX_H, paths))


def gen_flag():
    pole_x = CX + R * 0.065
    pole_top = CY - R * 0.52
    pole_bottom = CY + R * 0.48
    sw = R * 0.065
    flag_h = R * 0.48
    flag_w = R * 0.45
    base_w = R * 0.26
    paths = [
        {
            "d": f"M{pole_x:.1f},{pole_top:.1f}L{pole_x:.1f},{pole_bottom:.1f}",
            "stroke": MINE_COLOR,
            "strokeWidth": f"{sw:.1f}",
            "strokeCap": "round",
        },
        {
            "d": f"M{pole_x:.1f},{pole_top:.1f}L{pole_x:.1f},{pole_top + flag_h:.1f}L{pole_x - flag_w:.1f},{pole_top + flag_h / 2:.1f}Z",
            "fill": FLAG_COLOR,
        },
        {
            "d": f"M{pole_x - base_w:.1f},{pole_bottom:.1f}L{pole_x + base_w:.1f},{pole_bottom:.1f}",
            "stroke": MINE_COLOR,
            "strokeWidth": f"{sw:.1f}",
            "strokeCap": "round",
        },
    ]
    write_xml("hex_flag", xml_vector(HEX_W, HEX_H, paths))


if __name__ == "__main__":
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    font = TTFont(FONT_PATH)
    gen_backgrounds()
    gen_numbers(font)
    gen_mine()
    gen_flag()
    print(f"Viewport: {HEX_W:.2f} x {HEX_H}")
    print(f"Generated textures in {OUTPUT_DIR}")
