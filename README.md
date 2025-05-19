# 📚 Aplicación de Gestión de Tareas para Estudiantes 🎓📅

**Materia:** Diseño y Evaluación de Interfaces de Usuario  
**Equipo de desarrollo:** Eduardo Cantero, Rodrigo González, Cesar Gutiérrez, Nataly Iglesias, Sebastián López, Víctor Martínez, Melissa Márquez, Alejandro Ramírez

---

## 🧭 Descripción general del proyecto

Esta aplicación fue desarrollada en el marco de la materia **Diseño y Evaluación de Interfaces de Usuario**, la cual pone un fuerte énfasis en la creación de soluciones digitales centradas en las personas, priorizando la accesibilidad, la usabilidad y la experiencia del usuario como principios fundamentales de diseño.

En ese sentido, nuestra app se enfoca en resolver una necesidad específica: **la organización del tiempo y las actividades para estudiantes universitarios**, quienes frecuentemente enfrentan múltiples responsabilidades académicas, personales y profesionales. La aplicación busca brindar una experiencia amigable, accesible y eficiente a través de una interfaz intuitiva y funcionalidades clave como recordatorios, categorización y reportes visuales de progreso.

El objetivo no solo es ayudar al usuario a cumplir con sus tareas, sino también hacerlo de manera cómoda y adaptada a sus propias necesidades, promoviendo así un diseño verdaderamente inclusivo.

El usuario puede:

- Crear, editar y eliminar tareas.
- Establecer recordatorios y prioridades.
- Clasificar tareas por categorías o etiquetas.
- Visualizar reportes semanales y mensuales.
- Activar notificaciones personalizadas.
- Gestionar tareas recurrentes (diarias, semanales, mensuales).
- Consultar tareas completadas y pendientes.

## 📱 Tipo de prototipo entregado

Este prototipo corresponde a una **aplicación móvil nativa para Android**, desarrollada en Kotlin usando Android Studio.

---

## ⚙️ Tecnologías y versiones utilizadas

| Herramienta / Librería | Versión             |
| ---------------------- | ------------------- |
| Android Studio         | Flamingo o superior |
| Kotlin                 | JVM Target 1.8      |
| minSdk                 | 24                  |
| targetSdk              | 34                  |
| Room                   | 2.6.1               |
| ViewPager2             | 1.0.0               |
| Material Design        | 1.10.0              |
| Lifecycle              | 2.5.0 - 2.6.1       |
| Coroutines             | 1.7.3               |
| WorkManager            | 2.9.0               |
| Confetti               | 2.0.5               |
| MPAndroidChart         | v3.1.0              |

---

## 🧪 Dependencias

```kotlin
implementation(libs.androidx.core.ktx)
implementation(libs.androidx.appcompat)
implementation(libs.material)
implementation(libs.androidx.activity)
implementation(libs.androidx.constraintlayout)
implementation(libs.androidx.preference.ktx)

testImplementation(libs.junit)
androidTestImplementation(libs.androidx.junit)
androidTestImplementation(libs.androidx.espresso.core)

implementation("com.google.android.material:material:1.10.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.0")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("androidx.preference:preference:1.2.1")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
implementation("androidx.work:work-runtime-ktx:2.9.0")
implementation("androidx.startup:startup-runtime:1.1.1")

// ViewPager2 y TabLayout
implementation("androidx.viewpager2:viewpager2:1.0.0")

// Room
implementation("androidx.room:room-runtime:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")

// Efecto confetti
implementation("nl.dionsegijn:konfetti-xml:2.0.5")
implementation("nl.dionsegijn:konfetti-core:2.0.5")

// Gráficas
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
```

---

## 🛠️ Instrucciones para clonar, compilar y ejecutar

Sigue estos pasos para ejecutar la aplicación en tu entorno de desarrollo:

### 1. Clonar el repositorio

Abre una terminal y ejecuta el siguiente comando:

```bash
git clone https://github.com/nataly-iglesias/Aplicacion-de-tareas.git
cd Aplicacion-de-tareas
```

### 2. Abrir el proyecto en Android Studio

- Abre Android Studio.
- Haz clic en **“Abrir un proyecto existente” (Open an existing project)**.
- Selecciona la carpeta del proyecto que acabas de clonar.
- Espera a que Android Studio sincronice todas las dependencias con Gradle.

### 3. Ejecutar la aplicación

Puedes ejecutar la app de dos maneras:

#### En un dispositivo físico:

- Conecta tu teléfono Android vía USB.
- Activa el modo desarrollador y la **depuración USB**.
- Acepta la conexión desde tu dispositivo si se solicita.

#### En un emulador:

- Abre **AVD Manager** desde Android Studio.
- Crea un dispositivo virtual con **Android 7.0 (API 24)** o superior.
- Inicia el emulador.

Una vez configurado, presiona el botón verde ▶️ en Android Studio o usa el atajo de teclado `Shift + F10` para compilar y ejecutar la aplicación.

--- 

## 📦 Enlace de descarga del APK

Puedes descargar la versión compilada de la aplicación desde el siguiente enlace:

🔗 [Descargar APK - Versión de lanzamiento](https://drive.google.com/file/d/1RHXT9ZLHW6Gu3PGHlX4WFte8mVSEf0if/view?usp=sharing)

## 📲 Instrucciones para instalar la app en Android

1. Descarga el archivo APK desde el enlace proporcionado arriba.
2. Abre el archivo en tu dispositivo Android.
3. Si es la primera vez que instalas apps fuera de Google Play, tu dispositivo te pedirá activar la opción “Instalar desde fuentes desconocidas” (puede variar según la versión de Android).
4. Sigue las instrucciones en pantalla para completar la instalación.

---

## 📁 Estructura del repositorio

- .idea/ # Configuración del proyecto en Android Studio
- app/ # Código fuente de la aplicación Android
- gradle/ # Configuración de Gradle
- LICENSE # Licencia MIT del proyecto
- README.md # Documentación general del proyecto
- build.gradle.kts # Script de compilación en Kotlin
- gradle.properties # Configuración de propiedades del sistema y del entorno de compilación para Gradle
- gradlew / gradlew.bat # Scripts para usar Gradle sin instalación local
- settings.gradle.kts # Configuración de módulos

---

## 📝 Licencia

Este proyecto está distribuido bajo la licencia [MIT](LICENSE).
