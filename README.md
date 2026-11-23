# Gestión de Solicitudes

App Android con Kotlin y Jetpack Compose para manejar solicitudes.

También tiene una demo de API REST (Guía 14) y tests (Guía 15).

---

## Cuentas de prueba

Clave: `123456`

- `cliente@demo.cl`
- `admin@demo.cl`
- `tecnico@demo.cl`

---

## Cómo abrirla

1. Abre el proyecto en Android Studio  
2. Sync Gradle  
3. Dale Run ▶  

Empieza en el **login**.

---

## Qué hace cada perfil

**Cliente**
- Crear solicitudes
- Ver las suyas
- Escanear QR

**Admin**
- Ver todas
- Filtrar / asignar / cambiar estado
- Escanear QR
- Abrir los posts de Firebase (botón coral: `Posts en Firebase`)

**Técnico**
- Escanear QR

---

## Guía 14 — Posts en Firebase

El GET lee la colección `posts` de tu Firestore (proyecto Gestion Solicitudes DEMO).
Las solicitudes siguen en Room. Los posts no son solicitudes.

Cómo entrar:
1. Login como admin  
2. Toca **Posts en Firebase (GET/POST/PUT/DELETE)**  

Botones:
- **GET** → lee la lista desde Firestore  
- **POST** → guarda uno  
- **PUT** → edita  
- **DELETE** → borra  

---

## Guía 15 — Tests

Correr unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Si sale `BUILD SUCCESSFUL`, está bien.

Tests:
- `PostViewModelTest`
- `PostRepositoryTest`
- `PostScreenTest` (este en emulador)

---

## Carpetas importantes

```
data/local      → Room (solicitudes, usuarios, historial)
data/remote     → Retrofit (catálogo de servicios en tu Firestore)
repository/     → PostRepository y ServicioRepository
app/google-services.json → tu proyecto de Firebase
viewmodel/      → lógica
ui/screen/      → pantallas de la app (incluida la demo de API)
```

---

## Comandos útiles

```bash
# tests unitarios
./gradlew :app:testDebugUnitTest

# compilar
./gradlew :app:assembleDebug

# limpiar build
./gradlew clean
```

---

## Stack rápido

Compose · MVVM · Room · Retrofit · Firebase Firestore · QR (CameraX + ML Kit) · Kotest · MockK

Repo: https://github.com/YoditaGeGe/GestionSolicitudes
