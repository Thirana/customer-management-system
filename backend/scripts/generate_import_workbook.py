#!/usr/bin/env python3

import argparse
import datetime as dt
import os
import tempfile
import zipfile
from xml.sax.saxutils import escape


def parse_args():
    parser = argparse.ArgumentParser(description="Generate customer import .xlsx workbooks.")
    parser.add_argument("--output", required=True)
    parser.add_argument("--rows", type=int, default=1000)
    parser.add_argument("--mode", choices=["create-only", "auto", "mixed"], default="mixed")
    parser.add_argument("--include-invalid-row", choices=["true", "false"], default="false")
    args = parser.parse_args()
    if args.rows < 1:
        raise SystemExit("--rows must be greater than zero")
    args.include_invalid_row = args.include_invalid_row == "true"
    return args


def generated_row(index, mode):
    display_number = index + 1
    date_of_birth = (dt.date(1980, 1, 1) + dt.timedelta(days=index % 10000)).isoformat()

    if mode == "create-only":
        return (
            f"Generated Create Customer {display_number}",
            date_of_birth,
            f"GEN-CREATE-{display_number:06d}",
            "CREATE",
        )

    if mode == "auto":
        return (
            f"Generated Auto Customer {display_number}",
            date_of_birth,
            f"GEN-AUTO-{display_number:06d}",
            "",
        )

    variant = index % 3
    if variant == 0:
        return (
            f"Generated Mixed Create {display_number}",
            date_of_birth,
            f"GEN-MIX-CREATE-{display_number:06d}",
            "CREATE",
        )
    if variant == 1:
        seeded_index = (index % 8) + 1
        return (
            f"Updated Seed Customer {seeded_index} Batch {display_number}",
            date_of_birth,
            f"DEV-NIC-{seeded_index:03d}",
            "",
        )
    return (
        f"Generated Mixed Update {display_number}",
        date_of_birth,
        f"DEV-NIC-{((index + 1) % 8) + 1:03d}",
        "UPDATE",
    )


def invalid_row(row_count):
    return ("", "1995-01-01", f"GEN-INVALID-{row_count + 1:06d}", "CREATE")


def inline_cell(column, value):
    if value is None:
        value = ""
    return f'<c r="{column}" t="inlineStr"><is><t>{escape(value)}</t></is></c>'


def worksheet_xml_path(row_count, mode, include_invalid_row):
    temp_file = tempfile.NamedTemporaryFile(delete=False, suffix=".xml")
    try:
        with open(temp_file.name, "w", encoding="utf-8") as handle:
            handle.write('<?xml version="1.0" encoding="UTF-8" standalone="yes"?>')
            handle.write('<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">')
            handle.write("<sheetData>")

            headers = ("Name", "Date of Birth", "NIC Number", "Operation")
            handle.write('<row r="1">')
            for column, value in zip(("A1", "B1", "C1", "D1"), headers):
                handle.write(inline_cell(column, value))
            handle.write("</row>")

            for index in range(row_count):
                row_number = index + 2
                row_values = generated_row(index, mode)
                handle.write(f'<row r="{row_number}">')
                for column, value in zip(
                    (f"A{row_number}", f"B{row_number}", f"C{row_number}", f"D{row_number}"),
                    row_values,
                ):
                    handle.write(inline_cell(column, value))
                handle.write("</row>")

            if include_invalid_row:
                row_number = row_count + 2
                handle.write(f'<row r="{row_number}">')
                for column, value in zip(
                    (f"A{row_number}", f"B{row_number}", f"C{row_number}", f"D{row_number}"),
                    invalid_row(row_count),
                ):
                    handle.write(inline_cell(column, value))
                handle.write("</row>")

            handle.write("</sheetData></worksheet>")
        return temp_file.name
    except Exception:
        os.unlink(temp_file.name)
        raise


def write_xlsx(output_path, row_count, mode, include_invalid_row):
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    sheet_xml_path = worksheet_xml_path(row_count, mode, include_invalid_row)
    try:
        with zipfile.ZipFile(output_path, "w", compression=zipfile.ZIP_DEFLATED) as workbook:
            workbook.writestr(
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
  <Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>""",
            )
            workbook.writestr(
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>""",
            )
            workbook.writestr(
                "docProps/core.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
 xmlns:dc="http://purl.org/dc/elements/1.1/"
 xmlns:dcterms="http://purl.org/dc/terms/"
 xmlns:dcmitype="http://purl.org/dc/dcmitype/"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <dc:title>Customer Import Workbook</dc:title>
</cp:coreProperties>""",
            )
            workbook.writestr(
                "docProps/app.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
 xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes">
  <Application>Codex</Application>
</Properties>""",
            )
            workbook.writestr(
                "xl/workbook.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
 xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Customers" sheetId="1" r:id="rId1"/>
  </sheets>
</workbook>""",
            )
            workbook.writestr(
                "xl/_rels/workbook.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>""",
            )
            workbook.write(sheet_xml_path, "xl/worksheets/sheet1.xml")
    finally:
        os.unlink(sheet_xml_path)


def main():
    args = parse_args()
    write_xlsx(args.output, args.rows, args.mode, args.include_invalid_row)
    invalid_note = " + 1 invalid row" if args.include_invalid_row else ""
    print(f"Generated workbook at {os.path.abspath(args.output)}")
    print(f"Rows: {args.rows}{invalid_note}")
    print(f"Mode: {args.mode}")


if __name__ == "__main__":
    main()
