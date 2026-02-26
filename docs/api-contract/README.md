# API Contract v1

Endpoint: POST /generate  
Fecha congelación: 26/02/2026  
Regla: cualquier cambio requiere acuerdo del equipo (María + Marilú + Cris).

Archivos:
- generate-request-example.json
- generate-success-response-example.json
- error-response-examples.json

Error codes:

- 400 - VALIDATION_ERROR
When request fields are invalid or missing.

- 500 - INTERNAL_SERVER_ERROR
When an unexpected server error occurs.
In this case, "errors" array may be null.

Supported certificateType values:
- CRIMINAL_RECORD
- LAST_WILL
- DEATH_INSURANCE_CONTRACTS

Conditional rules:
- If certificateType == CRIMINAL_RECORD → criminalRecordDetails required.
- If certificateType == LAST_WILL → deathRelatedDetails required + lastWillExtra optional.
- If certificateType == DEATH_INSURANCE_CONTRACTS → deathRelatedDetails required, lastWillExtra must be null or omitted.

Extra Info:
- “destination" optional (required only when applicable).
- paymentMethod allows just "CASH" or "DIRECT_DEBIT"
- payment.customerAccount required only if paymentMethod == DIRECT_DEBIT. 
- Dates: YYYY-MM-DD, timestamps: YYYY-MM-DDTHH:MM:SSZ.