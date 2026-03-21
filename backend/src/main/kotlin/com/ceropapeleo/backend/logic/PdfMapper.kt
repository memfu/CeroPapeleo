package com.ceropapeleo.backend.logic

import org.slf4j.LoggerFactory

object PdfMapper {
    private val logger = LoggerFactory.getLogger(PdfMapper::class.java)

    private val MODELO_790_MAP = mapOf(
        // 1. Datos del solicitante
        "nie" to "documentId",
        "2 PRIMER APELLIDO DEL SOLICITANTE" to "surname1",
        "3 SEGUNDO APELLIDO" to "surname2",
        "4 NOMBRE" to "name",

        // 2. Dirección y vivienda
        "5 DOMICILIO CALLEPLAZAAVENIDA" to "street",
        "6 NÚMERO" to "number",
        "ESCALERA" to "staircase",
        "8 PISO" to "floor",
        "9 PUERTA" to "door",
        "11 DOMICILIO MUNICIPIO" to "city",
        "12 DOMICILIO PROVINCIA" to "province",
        "12 DOMICILIO PAIS" to "country",
        "14 CÓDIGO POSTAL" to "postalCode",

        // 3. Contacto
        "10 TELEFONOS FIJO YO MÓVIL" to "mobilePhone",
        "15 CORREO ELECTRÓNICO" to "email",

        // 4. Destino del Certificado
        "20 PAÍS DE DESTINO" to "destinationCountry",
        "21 AUTORIDAD O ENTIDAD ANTE LA QUE DEBE SURTIR EFECTOS" to "authorityOrEntity",

        // 5. Datos del Fallecido
        "33 NIFNIE" to "deceasedDocumentId",
        "34 PRIMER APELLIDO DE LA PERSONA FALLECIDA" to "deceasedSurname1",
        "35 SEGUNDO APELLIDO" to "deceasedSurname2",
        "36 NOMBRE" to "deceasedName",
        "37 FECHA DE DEFUNCIÓN" to "deathDate",
        "38 POBLACIÓN DE DEFUNCIÓN" to "deathCity",
        "39 FECHA DE NACIMIENTO" to "birthDate",
        "39 POBLACION  DE NACIMIENTO" to "birthCity", // Con doble espacio como en los logs

        // 6. Testamento y Otros (Nuevos de María)
        "FECHA DEL TESTAMENTO" to "willDate",
        "NOTARIO" to "notary",
        "LUGAR DE OTORGAMIENTO" to "grantPlace",
        "CONYUGE" to "spousesFullName",

        // 7. Firma
        "FECHA LUGAR" to "signaturePlace",

        // 8. Tipo certificado (Tu Regla de Oro)
        "18 Últimas voluntades" to "certificateType"
    )

    fun transformToPdfFields(userData: Map<String, String>): Map<String, String> {
        val finalMap = mutableMapOf<String, String>()

        MODELO_790_MAP.forEach { (pdfId, jsonKey) ->
            val value = userData[jsonKey]

            if (!value.isNullOrBlank()) {
                if (jsonKey == "certificateType" && value == "LAST_WILL") {
                    finalMap[pdfId] = "On" // REGLA ORO: Activamos la X con "On"
                    logger.info("✅ Checkbox activado: $pdfId")
                } else {
                    finalMap[pdfId] = value
                    logger.info("⚡ Mapping: App($jsonKey) -> PDF($pdfId) = $value")
                }
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