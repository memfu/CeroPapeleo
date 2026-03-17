# PDF → JSON Mapping v1  
Proyecto: CeroPapeleo  
Fecha: 14/03/2026  

# Alcance

Este mapping define la correspondencia entre los campos del **PDF oficial 790 del Ministerio de Justicia** y el modelo de datos enviado por la aplicación móvil (`GenerateRequest`).

El formulario 790 contiene campos para **tres certificados distintos**:

- Antecedentes penales
- Últimas voluntades
- Contratos de seguros de cobertura por fallecimiento

En esta versión del proyecto (**MVP**) únicamente se implementa el flujo de:

**CERTIFICADO DE ÚLTIMAS VOLUNTADES (LAST_WILL)**

Por tanto:

- Solo se mapean los campos necesarios para este certificado.
- Los campos pertenecientes a otras secciones se marcan como **FUERA_MVP**.

---

# Modelo JSON de referencia

Los datos enviados por la aplicación siguen la estructura del modelo: `GenerateRequest`.

La primera columna corresponde al nombre del campo detectado en el AcroForm del PDF mediante PDFBox.  
La segunda columna corresponde a la ruta del dato en el modelo JSON usado por la aplicación.


---

# 1. Datos del solicitante

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 1 NIFNIE | applicant.documentId | Sí | DNI/NIE del solicitante |
| 2 PRIMER APELLIDO DEL SOLICITANTE | applicant.firstSurname | Sí | |
| 3 SEGUNDO APELLIDO | applicant.secondSurname | No | |
| 4 NOMBRE | applicant.name | Sí | |

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

# 5. Datos del fallecido (Bloque Últimas Voluntades)

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

# 6. Información notarial del testamento

Estos campos son **opcionales** y solo se rellenan si el usuario conoce la información.

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| FECHA DEL TESTAMENTO | deathRelatedDetails.lastWillExtra.willDate | No | |
| NOTARIO | deathRelatedDetails.lastWillExtra.notary | No | |
| LUGAR DE OTORGAMIENTO | deathRelatedDetails.lastWillExtra.grantPlace | No | |
| CONYUGE | deathRelatedDetails.lastWillExtra.spousesFullName | No | |

---

# 7. Firma

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| FECHA LUGAR | signature.place | Sí | Lugar de firma |
| FECHA | signature.date | Sí | Formato recomendado YYYY-MM-DD |

---

# 8. Tipo de certificado

| Campo PDF | Ruta JSON | Obligatorio | Notas |
|---|---|---|---|
| 18 Últimas voluntades | certificateType | Sí | Marcar checkbox cuando certificateType = LAST_WILL |

---

# 9. Pago

| Campo PDF | Ruta JSON | Obligatorio | Notas                  |
|---|---|---|------------------------|
| EUROS | payment.amountEur | Sí | Valor por defecto 3.86 |

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