# TextfileGenerator

A single Maven WAR project for a Java web app on Apache Tomcat, built with
ZK (MVVM). It converts an uploaded Excel file into a fixed-width text file.
The text file is then ingested by an internal system, which processes every
record in it.

There is **no service/backend project** here. No Spring Remoting, no
database, no Hibernate. Everything runs inside this one WAR.

## What it does

1. The user uploads an Excel (`.xls`) file in the browser.
2. The user picks a conversion mode (**All Employee** or **GL Process**).
3. The app reads the sheet row by row and builds a text file where every
   field has a fixed width and position, as the internal system expects.
4. The generated `.txt` file is downloaded by the browser.

```
prsTxtConverter.zul -> TxtConverterVM (@Command browseFile / uploadFile)
   -> save uploaded .xls to server disk
   -> read with JExcelApi (jxl)
   -> build fixed-width lines (header + detail)
   -> write .txt -> Filedownload.save(...)
```

## File layout

Only two files drive the application:

```
src/main/java/com/comphand/viewmodel/
  TxtConverterVM.java        # ViewModel: upload handling + text generation
src/main/webapp/
  prsTxtConverter.zul        # UI: mode selection, file browse, upload button
```

Supporting pieces the project still needs in order to build and run:

- `com.comphand.model.DataConverter`: plain POJO imported by the ViewModel.
  Fields: `kode`, `accNumber`, `nominal` (double), `tanda`, `noref`, `nama`,
  each with getters and setters.
- `WEB-INF/web.xml` and `WEB-INF/zk.xml`: the standard ZK servlet setup.
- A `pom.xml` with `<packaging>war</packaging>` and these dependencies:
  - ZK (`zk`, `zul`, `zkbind`, `zkplus`)
  - JExcelApi (`net.sourceforge.jexcelapi:jxl`)
  - Apache Commons Lang 3 (`commons-lang3`)

The ViewModel declares
`@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)`.
It doesn't inject any Spring bean, so you can remove that annotation (and
the `zkplus` Spring dependency) if you don't use Spring here.

## The UI (`prsTxtConverter.zul`)

| Control | Binding | Purpose |
|---|---|---|
| Radio **All Employee** (value `0`) | `vm.strSelected` | Generates `DATAHASIL.txt` |
| Radio **GL Process** (value `1`) | `vm.strSelected` | Generates `DATAHASILM2M.txt` |
| Read-only textbox | `vm.strFileName` | Shows the name of the uploaded file |
| **Browse** button | `onUpload` -> `browseFile` | Uploads the Excel file to the server |
| **Upload** button | `onClick` -> `uploadFile` | Runs the conversion and downloads the result |

## Input: the Excel file

- Format: **`.xls` only** (content type `application/vnd.ms-excel`). Other
  types are rejected with the message "File yang diupload bukan format excel".
  JExcelApi can't read `.xlsx`.
- Only the **first sheet** is read.
- **Row 1 is treated as a header and skipped.** Data starts at row 2.
- Columns:

| Column | Field | Notes |
|---|---|---|
| A | `kode` | Code; in GL mode it decides the GL account |
| B | `accNumber` | Account number |
| C | `nominal` | Amount, must be numeric (parsed with `Double.parseDouble`) |
| D | `tanda` | Sign: `+` or `-` (used in GL mode) |
| E | `noref` | Reference number |
| F | `nama` | Name / description |

## Output: the text file

Each file has one **header line** followed by **detail lines**. All fields
are fixed-width, padded with spaces (text) or zeros (numbers). Lines are
joined internally with `#` and then split into separate lines when the file
is written (UTF-8).

### Mode 0 (All Employee) -> `DATAHASIL.txt`

Header:

| Content | Width / format |
|---|---|
| Literal `0ABCGROUP` | 9 |
| Record count | 5, zero-padded |
| Timestamp `yyyyMMddHHmmss` | 14 |
| Total nominal (`#.00`) | 21, zero-padded |
| Date `yyyyMMdd` | 8 |

Detail (one line per Excel row):

| Content | Width / format |
|---|---|
| Literal `1001` | 4 |
| `noref` | 20, right-padded |
| `nama` (trimmed, `'` removed) | 40, right-padded |
| Filler | 15 spaces |
| `accNumber` | 19, right-padded |
| Filler | 291 spaces |
| Literal `IDR` | 3 |
| Nominal (`#.00`) | 21, zero-padded |
| Zero amount (`0.00`) | 21, zero-padded |
| Literal `Pembayaran tagihan bulan` | 40, right-padded |
| Filler | 43 spaces |

### Mode 1 (GL Process) -> `DATAHASILM2M.txt`

Header:

| Content | Width / format |
|---|---|
| Literal `0UPLM2MBASMINTA` | 15 |
| Literal `MANY TO MANY` | 40, right-padded |
| Literal `IDR` | 3 |
| Record count (**Excel rows x 2**) | 5, zero-padded |
| Date `yyyyMMdd` | 8 |
| Literal `010` | 5, right-padded |

Detail: **two lines per Excel row**, one for the customer account and one
for the GL account:

| Line | Layout |
|---|---|
| Account line | `1A` + `accNumber` (19, right-padded) + `C`/`D` + nominal (21, zero-padded, no decimals) + `nama` |
| GL line | `1G` + GL code + `D`/`C` + nominal (21, zero-padded, no decimals) + `nama` |

Debit/credit follows the `tanda` column:

| `tanda` | Account line | GL line |
|---|---|---|
| `+` | `C` (credit) | `D` (debit) |
| `-` | `D` (debit) | `C` (credit) |

The **GL code** is chosen by checking whether the `kode` column *contains* a
keyword. The first match wins, so the order in the `if / else if` chain in
`TxtConverterVM.java` matters. Keywords checked, in order: `AXA`, `614`,
`1002`, `QRIS`, `K4P`, `5009`, `PRIM`, `JALI`, `2001`, `MDR1`, `MDR2`, `SIRQ`,
`WITH`, `WAIV`, `LLD1`/`RTTT`, `EQUI`, `LOSS`, `2RDM`, `BPCR`, `PTAB`, `PGIT`.
The keyword-to-GL-code mapping lives in the ViewModel; edit it there when
the GL chart changes.

## Build

```bash
mvn clean package
```

This produces `target/<artifactId>.war`.

## Deploy to Tomcat

Copy the WAR into Tomcat's `webapps/` folder:

```
webapps/
  TextfileGenerator.war   -> http://localhost:8080/TextfileGenerator
```

Then open `http://localhost:8080/TextfileGenerator/prsTxtConverter.zul`.

> **Tomcat must unpack the WAR** (the default `unpackWARs="true"`) and the
> Tomcat user must have **write permission** on the app's `WEB-INF/classes`
> folder. See "Where files are written" below.

## How to use

1. Open `prsTxtConverter.zul`.
2. Choose **All Employee** or **GL Process**.
3. Click **Browse** and select the `.xls` file. Its name appears in the
   textbox.
4. Click **Upload**. The browser downloads the generated text file and a
   "Proses Selesai" message appears.
5. Hand the text file over to the internal system for ingestion.

## Where files are written

The ViewModel works out its working folder from the compiled class location
(`getClass().getProtectionDomain().getCodeSource().getLocation()`), which is
the app's `WEB-INF/classes` directory. Both the uploaded Excel file and the
generated text file are written there.

## Known limitations

These come from the current code and are worth knowing before production use:

- **Shared output file.** The output name is fixed (`DATAHASIL.txt` /
  `DATAHASILM2M.txt`) and the folder is shared, so two users converting at
  the same time can overwrite each other's files. Uploaded and generated
  files are never cleaned up.
- **No mode selected.** Clicking Upload without picking a radio button
  causes a `NullPointerException`, because `strSelected` is null. Setting a
  default (for example `"0"`) in the ViewModel avoids it.
- **GL code carry-over.** In GL mode, `kodeGL` isn't reset per row. If a
  row's `kode` matches no keyword, it silently reuses the previous row's GL
  code (or `null` on the first row).
- **`#` is the line delimiter.** A `#` inside a name or reference number
  would split a record into two lines.
- **Bad numbers.** A non-numeric value in the nominal column throws an
  exception and aborts the whole conversion.
- **Unused bindings.** `@NotifyChange` references a `lstModShift` property
  that doesn't exist, and the `.zul` page title still says "User
  management". Both are cosmetic.

## Going further

- Write generated files to a per-request temp file (or stream them
  directly) instead of the app folder to fix the shared-file problem.
- Move the keyword-to-GL mapping out of the `if / else if` chain into a
  `Map` or a properties file so changes don't need a recompile.
- Validate each row (numeric nominal, `tanda` is `+`/`-`, required fields
  present) and report row numbers on failure instead of aborting.
- Consider Apache POI if `.xlsx` support is needed later.
