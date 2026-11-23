# Documentación técnica

Gestión de Solicitudes de Servicios Digitales. App Android en Kotlin.

El caso es el del diagrama: un cliente pide un servicio digital, un administrador lo clasifica y lo asigna, y un técnico lo resuelve. El cliente confirma o pide reapertura.

## Contexto

La app cubre el flujo de tres perfiles sobre solicitudes de servicios digitales.

- Cliente: crea la solicitud, sigue el estado, comenta, comparte la referencia por WhatsApp simulado y confirma o reabre.
- Administrador: filtra, clasifica, define prioridad, asigna técnico, supervisa y cierra o cancela.
- Técnico: ve lo asignado, consulta el historial y registra avances o propone la resolución.

Los estados son Recibida, En revisión, Pendiente de información, Asignada, En proceso, Resuelta, Cerrada y Cancelada.

Las cuentas de prueba usan la clave `123456`: `cliente@demo.cl`, `admin@demo.cl`, `tecnico@demo.cl` y `tecnico2@demo.cl`.

## Arquitectura

Jetpack Compose, MVVM, Room y Retrofit.

- La pantalla llama al ViewModel.
- `SolicitudViewModel` guarda las solicitudes en Room.
- `PostViewModel` habla con Firestore por Retrofit.
- Las solicitudes no se guardan en Firebase. El catálogo externo sí.

No hay un microservicio Spring Boot. La persistencia de solicitudes es Room y el catálogo externo es la API REST de Firestore.

## Room

Base `gestion_solicitudes.db`.

| Tabla | Contenido |
|---|---|
| `usuarios` | Perfil, correo y clave de prueba |
| `solicitudes` | Datos de la solicitud, estado, prioridad, responsable y nombre de evidencia |
| `eventos_historial` | Cambios de estado, comentarios y evidencia asociada |

La evidencia es un nombre de archivo de ejemplo (`captura_error_demo.png` o `documento_demo.pdf`). No se adjunta una foto real. Los comentarios viven en el historial, no en una tabla aparte.

## API externa

Retrofit llama a `https://firestore.googleapis.com/v1/` del proyecto `gestion-solicitudes-demo`.

La colección `servicios` es el catálogo del diagrama:

| ID | Servicio | Categoría | Descripción |
|---|---|---|---|
| 1 | Mantenimiento web | Web | Actualización y soporte de sitios web |
| 2 | Soporte de correo | Soporte | Configuración y solución de problemas |
| 3 | Hosting | Infraestructura | Alojamiento y gestión de sitios |

En **Posts en Firebase**:

- **GET todos** lee `servicios` y `posts`. Si `servicios` está vacía, vuelve a crear las tres filas.
- **POST servicio**, **PUT** y **DELETE** modifican `servicios`.
- **POST**, **PUT** y **DELETE** de un post modifican `posts`.

El formulario de nueva solicitud usa el mismo GET del catálogo para el menú Servicio del catálogo.

`google-services.json` está en `app/` y no se sube al repositorio porque contiene la clave. Sin ese archivo la app no compila.

## Pruebas

```bash
./gradlew :app:testDebugUnitTest
```

Cubren la conversión de la respuesta de Firestore, las tres filas del catálogo, si una solicitud sigue abierta y si el cliente ya confirmó la resolución.

## APK firmado

La firma de release está en `keystore/gestion-solicitudes-release.jks`, alias `gestion`. La contraseña y la ruta están en `local.properties`, que no se sube a Git.

```bash
./gradlew :app:assembleRelease
```

El APK queda en `app/build/outputs/apk/release/app-release.apk`.

Ese comando no cambia el APK de depuración que instala Android Studio con Run.
