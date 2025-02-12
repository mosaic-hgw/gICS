## Legal-Consent-Datum
Das Legal-Consent-Datum sagt aus, ab wann ein IC als gültig betrachtet wird und damit in die Abfragelogik einbezogen wird. Das Datum berechnet sich aus vielen verschiedenen Enflussfaktoren. Wie genau wird im Folgenden beschrieben.

### Einflussfaktoren
- **Anlegedatum:** das Datum an dem ein IC angelegt/erstellt wurde
- **Unterschriften-Datums** alle Datumsangaben von Unterschriften
- **externes Gültigkeitsdatum (ValidFrom)** ein von extern vorgegebenes Gültigkeitsdatum
- **Gültigkeitsdatum (aus Template)** das feste Gültigkeitsdatum aus den ValidFromProperties des Templates
- **Gültigkeitsfrist (aus Template)** Angabe einer Zeitspanne, ab Anlegen eines ICs, wie Lange ein IC noch nicht gültig sein soll. Kommt aus den ValidFromProperties des Templates

## Ablaufdiagramm
LCD = LegalConsentDate
```mermaid
flowchart TD
    S[LCD=1.1.1970] --> VFPN{Template: ValidFromPeriod?}
    VFPN --> |not null| VFP[LCD = CreationDate+Period]
    VFPN --> |null| VFDN{Template: ValidFromDate?}
    VFP --> VFDN
    VFDN --> |not null| VFD[LCD = ValidFromDate]
    VFDN --> |null| EXVF{ExternalValidFromDate > LCD}
    VFD --> EXVF
    EXVF --> |false| SIGD{Any Signature > LCD}
    EXVF --> |true| EXVFS[LCD = ExternalValidFrom]
    EXVFS --> SIGD
    SIGD --> |true| SIGDS[LCD = latestSignatureDate]
    SIGD --> |false| CD{LCD == 1.1.1970}
    SIGDS --> CD
    CD --> |true| CDS[LCD = CreationDate]
    CD --> |false| END
    CDS --> END
```