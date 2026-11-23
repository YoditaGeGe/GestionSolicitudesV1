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
- Las solicitudes se guardan en Room.
- El catálogo externo se lee con Retrofit desde Firestore.

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

## Defensa

Quince minutos, sin microservicios. El cambio en vivo es agregar un campo al formulario de crear solicitud. El ejemplo de la guía es Teléfono. Hoy ese campo no existe: lo agregas delante del docente.

Orden para mostrarlo:

1. Abre la app en el emulador e inicia sesión con `cliente@demo.cl` / `123456`.
2. Explica MVVM: `ui/screen` pinta, `viewmodel` decide y `data/local` guarda en Room.
3. Entra como admin a **Posts en Firebase** y pulsa **GET todos**. Eso es Retrofit contra Firestore, la API externa. `ApiService` está en `data/remote`.
4. Crea una solicitud. Eso inserta en Room, en `solicitudes` y `eventos_historial`.
5. Corre `./gradlew :app:testDebugUnitTest` y explica `SolicitudTest` o `ServicioRepositoryTest`.
6. Muestra `app/build/outputs/apk/release/app-release.apk` y el alias `gestion`.
7. Muestra el repositorio privado y los commits de `main`.

Para el campo nuevo, en este orden:

1. `FormularioSolicitud` en `model/SolicitudUiState.kt`: el dato y, si es obligatorio, el error.
2. `NuevaSolicitudScreen.kt`: el campo en el formulario.
3. `SolicitudViewModel`: guardar lo escrito y copiarlo al crear.
4. `Solicitud` y `SolicitudEntity`: la columna.
5. `Mappers.kt`: ida y vuelta entre el modelo y Room.
6. El detalle, en `UiBits.kt`, para que el dato se vea después de crearla.

Al cambiar `SolicitudEntity`, Room borra la base local y vuelve a crear los usuarios de prueba. Vuelve a iniciar sesión y crea una solicitud con el campo nuevo.
