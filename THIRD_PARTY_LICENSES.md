# Third-party licenses

## English–Vietnamese dictionary database

File: `app/src/main/assets/dictionary_en_vi.db`

> This SQLite dictionary database was created by Skypedia
> (https://github.com/skypediacode) based on the MinhQND Dictionary project
> (https://github.com/minhqnd/dictionary), with added learner-friendly example
> sentences and data validation using OVDP references. Originally sourced from
> Wiktionary and other open-source linguistic resources. Licensed under
> CC BY-SA 4.0. See ATTRIBUTION.md for full details.

- Source repository: https://github.com/skypediacode/english-vietnamese-dictionary
- License: Creative Commons Attribution-ShareAlike 4.0 International
  (https://creativecommons.org/licenses/by-sa/4.0/)
- Full attribution: `docs/dictionary/ATTRIBUTION.md`

### Changes made

The only change to the original database file is setting
`PRAGMA user_version = 1`. The data tables are unmodified.