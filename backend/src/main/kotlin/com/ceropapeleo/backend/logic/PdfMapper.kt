package com.ceropapeleo.backend.logic

import org.slf4j.LoggerFactory

object PdfMapper {
    private val logger = LoggerFactory.getLogger(PdfMapper::class.java)

    private val MODELO_790_MAP = mapOf(
        // =====================================================
        // 1. DATOS DEL SOLICITANTE
        // =====================================================
        "nie" to "documentId",
        "2 PRIMER APELLIDO DEL SOLICITANTE" to "surname1",
        "3 SEGUNDO APELLIDO" to "surname2",
        "4 NOMBRE" to "name",

        // =====================================================
        // 2. DIRECCIÓN
        // =====================================================
        "5 DOMICILIO CALLEPLAZAAVENIDA" to "street",
        "6 NÚMERO" to "number",
        "ESCALERA" to "staircase",
        "8 PISO" to "floor",
        "9 PUERTA" to "door",
        "11 DOMICILIO MUNICIPIO" to "city",
        "12 DOMICILIO PROVINCIA" to "province",
        "12 DOMICILIO PAIS" to "country",
        "14 CÓDIGO POSTAL" to "postalCode",

        // =====================================================
        // 3. CONTACTO
        // =====================================================
        "10 TELEFONOS FIJO YO MÓVIL" to "mobilePhone",
        "15 CORREO ELECTRÓNICO" to "email",

        // =====================================================
        // 4. DESTINO
        // =====================================================
        "20 PAÍS DE DESTINO" to "destinationCountry",
        "21 AUTORIDAD O ENTIDAD ANTE LA QUE DEBE SURTIR EFECTOS" to "authorityOrEntity",

        // =====================================================
        // 5. ANTECEDENTES PENALES (22-32)
        // =====================================================
        "22 NIFCIFNIE" to "criminalSubjectDocumentId",
        "23 PRIMER APELLIDO O DENOMINACIÓN SOCIAL" to "criminalSubjectSurname1OrBusinessName",
        "24 SEGUNDO APELLIDO" to "criminalSubjectSurname2",
        "25 NOMBRE" to "criminalSubjectName",
        "26 FECHA DE NACIMIENTO" to "criminalBirthDate",
        "27 POBLACIÓN DE NACIMIENTO" to "criminalBirthCity",
        "28 PROVINCIAPAIS DE NACIMIENTO" to "criminalBirthProvinceOrCountry",
        "29 PAÍS DE NACIONALIDAD" to "criminalNationalityCountry",
        "30 NOMBRE DEL PADRE" to "criminalFatherName",
        "31 NOMBRE DE LA MADRE" to "criminalMotherName",
        "32 FINALIDAD PARA LA QUE SE SOLICITA" to "criminalPurpose",

        // =====================================================
        // 6. DATOS DEL FALLECIDO
        // =====================================================
        "33 NIFNIE" to "deceasedDocumentId",
        "34 PRIMER APELLIDO DE LA PERSONA FALLECIDA" to "deceasedSurname1",
        "35 SEGUNDO APELLIDO" to "deceasedSurname2",
        "36 NOMBRE" to "deceasedName",
        "37 FECHA DE DEFUNCIÓN" to "deathDate",
        "38 POBLACIÓN DE DEFUNCIÓN" to "deathCity",
        "39 FECHA DE NACIMIENTO" to "birthDate",
        "39 POBLACION  DE NACIMIENTO" to "birthCity", // Con doble espacio como en los logs

        // =====================================================
        // 7. TESTAMENTO
        // =====================================================
        "FECHA DEL TESTAMENTO" to "willDate",
        "NOTARIO" to "notary",
        "LUGAR DE OTORGAMIENTO" to "grantPlace",
        "CONYUGE" to "spousesFullName",

        // =====================================================
        // 8. FIRMA
        // =====================================================
        "FECHA LUGAR" to "signaturePlace",

        // =====================================================
        // 9. PAGO
        // =====================================================
        "EUROS" to "amountEur",

        // =====================================================
        // 10. TIPO DE CERTIFICADO
        // =====================================================
        "17 Antecedentes Penales" to "17 Antecedentes Penales",
        "18 Últimas voluntades" to "18 Últimas voluntades",
        "19 Contrato de seguros de cobertura de fallecimiento" to "19 Contrato de seguros de cobertura de fallecimiento"
    )

    fun transformToPdfFields(userData: Map<String, String>): Map<String, String> {
        val finalMap = mutableMapOf<String, String>()

        MODELO_790_MAP.forEach { (pdfId, jsonKey) ->
            val value = userData[jsonKey]

            if (!value.isNullOrBlank()) {
                finalMap[pdfId] = value
                logger.info("⚡ Mapping: App($jsonKey) -> PDF($pdfId) = $value")
            }
        }

        //9. Autorización de envío postal
        when (userData["postalDeliveryAuthorized"]) {
            "true" -> {
                finalMap["ACEPTAR"] = "ACEPTARSI"
                logger.info("✅ Checkbox activado: ACEPTAR = ACEPTARSI")
            }
            "false" -> {
                finalMap["DENEGAR"] = "ACEPTARNO"
                logger.info("✅ Checkbox activado: DENEGAR = ACEPTARNO")
            }
        }

        // 10. Forma de pago
        when (userData["paymentMethod"]) {
            "CASH" -> {
                finalMap["Casilla de verificación7"] = "Sí"
                logger.info("✅ Checkbox activado pago: CASH -> Casilla de verificación7")
            }
            "ACCOUNT" -> {
                finalMap["Casilla de verificación8"] = "Sí"
                logger.info("✅ Checkbox activado pago: ACCOUNT -> Casilla de verificación8")
            }
        }

        // 11. Fecha de firma separada en campos del PDF
        val signatureDate = userData["signatureDate"]
        if (!signatureDate.isNullOrBlank()) {
            val parts = signatureDate.split("/")
            if (parts.size == 3) {
                val day = parts[0]
                val month = parts[1]
                val year = parts[2].takeLast(2) // Para coger los últimos dos dígitos del año

                finalMap["FECHA DIA"] = day
                finalMap["FECHA MES"] = month
                finalMap["FECHA"] = year

                logger.info("📅 Fecha firma separada: DIA=$day MES=$month AÑO=$year")
            } else {
                logger.warn("⚠️ signatureDate no tiene formato esperado dd/MM/yyyy: $signatureDate")
            }
        }

        // 12. Cuenta bancaria para pago ACCOUNT
        if (userData["paymentMethod"] == "ACCOUNT") {
            val bankEnt = userData["bankEnt"].orEmpty()
            val bankOff = userData["bankOff"].orEmpty()
            val bankDC = userData["bankDC"].orEmpty()
            val bankAcc = userData["bankAcc"].orEmpty()

            val fullCcc = bankEnt + bankOff + bankDC + bankAcc

            if (fullCcc.length == 20) {
                fullCcc.forEachIndexed { index, char ->
                    val pdfField = "CCC${index + 1}"
                    finalMap[pdfField] = char.toString()
                }
                logger.info("✅ CCC rellenado correctamente: $fullCcc")
            } else {
                logger.warn("⚠️ CCC incompleto o inválido. Longitud esperada 20, recibida ${fullCcc.length}")
            }
        }

        return finalMap
    }
}