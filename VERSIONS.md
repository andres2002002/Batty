# Control de versiones de librerías importantes

## Kotlin + KSP

La versión de `com.google.devtools.ksp` depende fuertemente de la versión de Kotlin.
Ver tabla oficial de compatibilidad:

🔗 https://github.com/google/ksp/releases

> Estamos usando Kotlin 1.9.22, por eso usamos:
>
> ```toml
> kspVersion = "2.2.0-2.0.2"
> ```

## Otras referencias

- Room version mapping → https://developer.android.com/jetpack/androidx/releases/room
- Navigation Compose → https://developer.android.com/jetpack/compose/navigation
