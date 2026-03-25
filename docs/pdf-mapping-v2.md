# PDF → JSON Mapping v2
Proyecto: CeroPapeleo  
Fecha: 25/03/2026  

# Alcance

Este mapping define la correspondencia entre los campos del **PDF oficial 790 del Ministerio de Justicia** y el modelo de datos enviado por la aplicación móvil (`GenerateRequest`).

El formulario 790 contiene campos para **tres certificados distintos**:

- Antecedentes penales
- Últimas voluntades
- Contratos de seguros de cobertura por fallecimiento

---

# Modelo JSON de referencia

Los datos enviados por la aplicación siguen la estructura del modelo: `GenerateRequest`.

La primera columna corresponde al nombre del campo detectado en el AcroForm del PDF mediante PDFBox.  
La segunda columna corresponde a la ruta del dato en el modelo JSON usado por la aplicación.


---

# 1. Datos del solicitante

| Campo PDF                         | Ruta JSON | Obligatorio | Notas |
|-----------------------------------|---|---|---|
| nie                               | applicant.documentId | Sí | DNI/NIE del solicitante |
| 2 PRIMER APELLIDO DEL SOLICITANTE | applicant.firstSurname | Sí | |
| 3 SEGUNDO APELLIDO                | applicant.secondSurname | No | |
| 4 NOMBRE                          | applicant.name | Sí | |

---

# 2. Dirección del solicitante

| Campo PDF                     | Ruta JSON | Obligatorio | Notas |
|-------------------------------|---|---|---|
| 5 DOMICILIO CALLEPLAZAAVENIDA | applicant.address.street | Sí | |
| 6 NÚMERO                      | applicant.address.number | No | |
| ESCALERA                      | applicant.address.staircase | No | |
| 8 PISO                        | applicant.address.floor | No | |
| 9 PUERTA                      | applicant.address.door | No | |
| 11 DOMICILIO MUNICIPIO        | applicant.address.city | Sí | |
| 12 DOMICILIO PROVINCIA        | applicant.address.province | Sí | |
| 12 DOMICILIO PAIS             | applicant.address.country | Sí | Default: España |
| 14 CÓDIGO POSTAL              | applicant.address.postalCode | Sí | |

---

# 3. Contacto

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 10 TELÉFONOS FIJO YO MÓVIL | applicant.contact.mobilePhone | No | |
| 15 CORREO ELECTRÓNICO | applicant.contact.email | No | |

---

# 4. Destino del certificado

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 20 PAÍS DE DESTINO | destination.country | No | |
| 21 AUTORIDAD O ENTIDAD ANTE LA QUE DEBE SURTIR EFECTOS | destination.authorityOrEntity | No | |

---

# 5. Datos específicos del certificado de antecedentes penales (Bloque 22-32)

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 22 NIFCIFNIE | criminalRecordsDetails.subjectDocumentId | Sí | DNI/NIE/CIF del titular del certificado |
| 23 PRIMER APELLIDO O DENOMINACIÓN SOCIAL | criminalRecordsDetails.subjectFirstSurnameOrBusinessName | Sí | Para persona física: primer apellido. Para entidad: denominación social |
| 24 SEGUNDO APELLIDO | criminalRecordsDetails.subjectSecondSurname | No | |
| 25 NOMBRE | criminalRecordsDetails.subjectName | Sí | |
| 26 FECHA DE NACIMIENTO | criminalRecordsDetails.birthDate | No | Formato recomendado dd/MM/yyyy |
| 27 POBLACIÓN DE NACIMIENTO | criminalRecordsDetails.birthCity | No | |
| 28 PROVINCIAPAIS DE NACIMIENTO | criminalRecordsDetails.birthProvinceOrCountry | No | |
| 29 PAÍS DE NACIONALIDAD | criminalRecordsDetails.nationalityCountry | No | |
| 30 NOMBRE DEL PADRE | criminalRecordsDetails.fatherName | No | |
| 31 NOMBRE DE LA MADRE | criminalRecordsDetails.motherName | No | |
| 32 FINALIDAD PARA LA QUE SE SOLICITA | criminalRecordsDetails.purpose | Sí | Motivo de la solicitud |

---

# 6. Datos del fallecido (Bloque Últimas Voluntades)

Los campos correspondientes al difunto se encuentran en el **bloque 33-40 del formulario**.

| Campo PDF                                  | Ruta JSON | Obligatorio | Notas                          |
|--------------------------------------------|---|---|--------------------------------|
| 33 NIFNIE                                  | deathRelatedDetails.deceased.documentId | Sí | DNI/NIE del difunto            |
| 34 PRIMER APELLIDO DE LA PERSONA FALLECIDA | deathRelatedDetails.deceased.firstSurname | Sí |                                |
| 35 SEGUNDO APELLIDO                        | deathRelatedDetails.deceased.secondSurname | No |                                |
| 36 NOMBRE                                  | deathRelatedDetails.deceased.name | Sí |                                |
| 37 FECHA DE DEFUNCIÓN                      | deathRelatedDetails.deceased.deathDate | Sí | Formato recomendado YYYY-MM-DD |
| 38 POBLACIÓN DE DEFUNCIÓN                  | deathRelatedDetails.deceased.deathCity | No |                                |
| 39 FECHA DE NACIMIENTO                     | deathRelatedDetails.deceased.birthDate | No |                                |
| 39 POBLACIÓN DE NACIMIENTO                 | deathRelatedDetails.deceased.birthCity | No |                                |

---

# 7. Información notarial del testamento

Estos campos son **opcionales** y solo se rellenan si el usuario conoce la información.

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| FECHA DEL TESTAMENTO | deathRelatedDetails.lastWillExtra.willDate | No | |
| NOTARIO | deathRelatedDetails.lastWillExtra.notary | No | |
| LUGAR DE OTORGAMIENTO | deathRelatedDetails.lastWillExtra.grantPlace | No | |
| CONYUGE | deathRelatedDetails.lastWillExtra.spousesFullName | No | |

---

# 8. Firma

| Campo PDF   | Ruta JSON       | Obligatorio | Notas |
|---|---|---|---|
| FECHA LUGAR | signature.place | Sí | Lugar de firma |
| FECHA DIA   | signature.date  | Sí | Se extrae el día de `signature.date` en formato `dd/MM/yyyy` |
| FECHA MES   | signature.date  | Sí | Se extrae el mes de `signature.date` en formato `dd/MM/yyyy` |
| FECHA       | signature.date  | Sí | Se extraen solo los 2 últimos dígitos del año de `signature.date` |

Nota: `signature.date` se envía como fecha completa (`dd/MM/yyyy`) y en backend se divide en los campos del PDF `FECHA DIA`, `FECHA MES` y `FECHA` (año con 2 dígitos).
---

# 9. Tipo de certificado

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 17 Antecedentes Penales | certificateType | Sí | Marcar checkbox cuando certificateType = CRIMINAL_RECORDS |
| 18 Últimas voluntades | certificateType | Sí | Marcar checkbox cuando certificateType = LAST_WILL |
| 19 Contrato de seguros de cobertura de fallecimiento | certificateType | Sí | Marcar checkbox cuando certificateType = LIFE_INSURANCE |

---

# 10. Pago

| Campo PDF | Ruta JSON | Obligatorio | Notas                  |
|---|---|---|------------------------|
| EUROS | payment.amountEur | Sí | Valor por defecto 3.86 |

- CASH → Casilla de verificación7 → valor: "Sí"
- ACCOUNT → Casilla de verificación8 → valor: "Sí"

---

## Checkboxes especiales

### Autorización de envío postal
- ACEPTAR → valor: ACEPTARSI
- DENEGAR → valor: ACEPTARNO

---

# Campos detectados en el PDF pero fuera del MVP

Los siguientes campos pertenecen a otras secciones del formulario y no se utilizan en esta versión:

- 17 Antecedentes Penales
- 19 Contrato de seguros de cobertura de fallecimiento
- 29 País de nacionalidad
- 30 Nombre del padre
- 31 Nombre de la madre
- 32 Finalidad para la que se solicita
- CCC*
- CODIGO*
- JUSTIFICANTE
- Casillas de verificación adicionales

Estos campos se implementarán únicamente si el proyecto amplía su alcance a otros certificados.