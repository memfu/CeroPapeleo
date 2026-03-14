# PDF → JSON Mapping v1  
Proyecto: CeroPapeleo  
Fecha: 14/03/2026  

Este documento define la correspondencia entre los campos del PDF oficial del Modelo 790 y las rutas del modelo JSON `GenerateRequest`.

La primera columna corresponde al nombre del campo detectado en el AcroForm del PDF mediante PDFBox.  
La segunda columna corresponde a la ruta del dato en el modelo JSON usado por la aplicación.

---

## Datos del solicitante

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 33 NIFNIE | applicant.documentId | Sí | DNI/NIE/Pasaporte del solicitante |
| 2 PRIMER APELLIDO DEL SOLICITANTE | applicant.firstSurname | Sí | |
| 3 SEGUNDO APELLIDO | applicant.secondSurname | No | |
| 4 NOMBRE | applicant.name | Sí | |

---

## Dirección del solicitante

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 5 DOMICILIO CALLEPLAZAAVENIDA | applicant.address.street | Sí | Calle o vía |
| 6 NÚMERO | applicant.address.number | No | |
| ESCALERA | applicant.address.staircase | No | |
| 8 PISO | applicant.address.floor | No | |
| 9 PUERTA | applicant.address.door | No | |
| 11 DOMICILIO MUNICIPIO | applicant.address.city | Sí | |
| 12 DOMICILIO PROVINCIA | applicant.address.province | Sí | |
| 12 DOMICILIO PAIS | applicant.address.country | Sí | Default: España |
| 14 CÓDIGO POSTAL | applicant.address.postalCode | Sí | |

---

## Contacto

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 10 TELEFONOS FIJO YO MÓVIL | applicant.contact.mobilePhone | No | |
| 15 CORREO ELECTRÓNICO | applicant.contact.email | No | |

---

## Tipo de certificado

Estos campos del PDF están representados como **checkboxes**.

| Campo PDF | Ruta JSON | Notas |
|---|---|---|
| 17 Antecedentes Penales | certificateType | Activar si certificateType = CRIMINAL_RECORD |
| 18 Últimas voluntades | certificateType | Activar si certificateType = LAST_WILL |
| 19 Contrato de seguros de cobertura de | certificateType | Activar si certificateType = DEATH_INSURANCE_CONTRACTS |

---

## Destino del certificado

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 20 PAÍS DE DESTINO | destination.country | No | |
| 21 AUTORIDAD O ENTIDAD ANTE LA QUE DEBE SURTIR EFECTOS | destination.authorityOrEntity | No | |

---

## Datos de la persona fallecida

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 22 NIFCIFNIE | deathRelatedDetails.deceased.documentId | Sí | DNI/NIE del causante |
| 23 PRIMER APELLIDO O DENOMINACIÓN SOCIAL | deathRelatedDetails.deceased.firstSurname | Sí | |
| 24 SEGUNDO APELLIDO | deathRelatedDetails.deceased.secondSurname | No | |
| 25 NOMBRE | deathRelatedDetails.deceased.name | Sí | |
| 26 FECHA DE NACIMIENTO | deathRelatedDetails.deceased.birthDate | No | Formato YYYY-MM-DD |
| 27 POBLACIÓN DE NACIMIENTO | deathRelatedDetails.deceased.birthCity | No | |
| 28 PROVINCIAPAIS DE NACIMIENTO | PENDIENTE | No |Campo detectado por PDFBox pero no identificado visualmente en el formulario |
| 37 FECHA DE DEFUNCIÓN | deathRelatedDetails.deceased.deathDate | Sí | Formato YYYY-MM-DD |
| 38 POBLACIÓN DE DEFUNCIÓN | deathRelatedDetails.deceased.deathCity | No | |

---

## Datos de últimas voluntades

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| FECHA DEL TESTAMENTO | deathRelatedDetails.lastWillExtra.willDate | No | |
| NOTARIO | deathRelatedDetails.lastWillExtra.notary | No | |
| LUGAR DE OTORGAMIENTO | deathRelatedDetails.lastWillExtra.grantPlace | No | |
| CONYUGE | deathRelatedDetails.lastWillExtra.spousesFullName | No | |

---

## Firma del documento

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| FECHA LUGAR | signature.place | Sí | Lugar de firma |
| FECHA | signature.date | Sí | Fecha de firma |

---

## Pago

| Campo PDF | Ruta JSON | Notas |
|---|---|---|
| EUROS | payment.amountEur | Valor por defecto: 3.78 |