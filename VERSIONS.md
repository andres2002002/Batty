# Control de versiones de librerías importantes

## Kotlin + KSP

La versión de `com.google.devtools.ksp` depende fuertemente de la versión de Kotlin.
Ver tabla oficial de compatibilidad:

🔗 https://github.com/google/ksp/releases

> Actualmente en `libs.versions.toml`:
>
> ```toml
> kotlin = "2.3.0"
> kspVersion = "2.2.21-2.0.4"
> ```

## Listado de Versiones (libs.versions.toml)

| Categoría | Referencia | Versión |
| :--- | :--- | :--- |
| **Build & Tools** | Android Gradle Plugin (AGP) | 8.13.2 |
| | Kotlin | 2.3.0 |
| | KSP | 2.2.21-2.0.4 |
| | Google Services | 4.4.4 |
| **AndroidX / Compose** | Core KTX | 1.17.0 |
| | Lifecycle Runtime KTX | 2.10.0 |
| | Activity Compose | 1.12.2 |
| | Compose BOM | 2026.01.00 |
| | Navigation Compose | 2.9.6 |
| | DataStore | 1.2.0 |
| | Room | 2.8.4 |
| **Inyección de Dependencias** | Dagger Hilt | 2.57.2 |
| | Hilt Navigation Compose | 1.3.0 |
| **Firebase** | Firebase BOM | 34.7.0 |
| | Firebase Crashlytics Plugin | 3.0.6 |
| **Utilidades & Otros** | Timber | 5.0.1 |
| | MPAndroidChart | 3.1.0 |
| | Guava | 33.5.0-android |
| | Protobuf | 4.33.4 |
| | Protobuf Plugin | 0.9.6 |
| **Testing** | JUnit | 4.13.2 |
| | AndroidX JUnit | 1.3.0 |
| | Espresso Core | 3.7.0 |

## Otras referencias

- Room version mapping → https://developer.android.com/jetpack/androidx/releases/room
- Navigation Compose → https://developer.android.com/jetpack/compose/navigation
- MPAndroidChart → https://github.com/PhilJay/MPAndroidChart
