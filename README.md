# Cero Papeleo 📄🤖

**Asistencia inteligente para la cumplimentación del modelo 790-006**

Trabajo de Fin de Grado (TFG) para el Ciclo Formativo de Grado Superior en **Desarrollo de Aplicaciones Multiplataforma (DAM)**.

---

## 📝 Sobre el Proyecto

El formulario **790-006** del Ministerio de Justicia es uno de los documentos administrativos más demandados en España (utilizado para certificados de Antecedentes Penales, Últimas Voluntades y Seguros de Vida). Sin embargo, su complejidad técnica y la falta de información directa suelen generar frustración y errores en los ciudadanos.

**Cero Papeleo** es una solución multiplataforma que simplifica este proceso mediante:
* **Manipulación inteligente de PDF**: Descarga dinámica y relleno automático de campos.
* **Asistencia con IA**: Un chatbot especializado llamado **Clara790** que resuelve dudas en tiempo real.
* **Reducción de la brecha digital**: Interfaz amigable diseñada para usuarios con poco conocimiento técnico o barreras idiomáticas.

## 🛠️ Stack Tecnológico

El proyecto utiliza una arquitectura robusta basada en microservicios y contenedores para garantizar la escalabilidad y el control de procesos críticos.

| Componente | Tecnología | Función |
| :--- | :--- | :--- |
| **Frontend Móvil** | Kotlin + Jetpack Compose | Interfaz nativa Android moderna y reactiva. |
| **Servidor API** | Kotlin + Ktor | Lógica de negocio y orquestación de servicios. |
| **Automatización** | Node.js + Selenium | Descarga en tiempo real del PDF con ID único desde el Ministerio. |
| **IA / Chatbot** | Dialogflow (Google) | Procesamiento de lenguaje natural (Agente Clara790). |
| **Base de Datos** | MySQL | Persistencia de historial y logs de actividad. |
| **Manipulación PDF** | PDFBox | Librería para la escritura de datos sobre el formulario oficial. |
| **Infraestructura** | Docker & Docker Compose | Contenerización de servicios para un despliegue replicable. |
| **Comunicaciones** | Retrofit / Ktor Client | Gestión de peticiones asíncronas entre App y Backend. |

## 🏗️ Arquitectura y Metodología

Para este desarrollo se han combinado dos enfoques metodológicos:
1.  **Scrum Agile**: Gestión mediante Sprints de dos semanas para una entrega continua de valor.
2.  **V-Model**: Aplicado para asegurar la calidad técnica y mitigar riesgos en la integración de servicios externos (Selenium/Dialogflow).

### Hitos Alcanzados ✅
* Confirmación de viabilidad del script de automatización con **Selenium**.
* Diseño de arquitectura basada en **Docker** para aislar procesos lentos (descarga de PDF).
* Entrenamiento inicial del agente conversacional en **Dialogflow**.
* Prototipado de interfaces de usuario en Android Studio.

## 🚀 Próximos Pasos
- [ ] Configuración de red local con contenedores Docker (Ktor + MySQL + Selenium).
- [ ] Desarrollo de funciones en Kotlin para orquestar la descarga y llenado del PDF.
- [ ] Integración del chatbot vía proxy en el backend.
- [ ] Pulido de la interfaz de usuario en Jetpack Compose.

---

## 👥 Autoras
* **María Eugenia Martín Fuentes**
* **Cristina Salazar Guijarro**
* **Ana Marilú Hernández Olivares**

**Tutor:** Francisco Aliseda Polanco  
**Fecha:** 8 de diciembre de 2025
