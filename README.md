# Jerarquía de Chomsky · Simulador interactivo (Java Swing)

Aplicación de escritorio con interfaz gráfica que permite **simular paso a paso** un modelo computacional representativo de cada uno de los cuatro niveles de la Jerarquía de Chomsky.

| Nivel | Lenguajes | Modelo | Memoria | Qué se ve en pantalla |
|---|---|---|---|---|
| 3 | Regulares | Autómata Finito Determinista (AFD) | Ninguna (solo el estado) | Grafo de estados, estado actual resaltado, flecha usada, símbolo que se lee |
| 2 | Libres de contexto | Autómata de Pila (AP) | Pila LIFO ilimitada | Pila animada (apilar/desapilar), estado, regla δ aplicada |
| 1 | Sensibles al contexto | Autómata Linealmente Acotado (LBA) | Cinta de \|w\|+2 celdas | Cinta con delimitadores `<` `>`, cabezal que se mueve en ambos sentidos |
| 0 | Recursivamente enumerables | Máquina de Turing (MT) | Cinta infinita | Cinta extendible, alfabeto Σ y Γ, cabezal, estado activo, resultado de la función |

> Documento con la explicación de la implementación y los conceptos: [`docs/IMPLEMENTACION.md`](docs/IMPLEMENTACION.md)

## Requisitos

- **JDK 17 o superior** (probado con Java 21). No usa librerías externas: solo Java estándar (Swing).
- Comprobar la versión: `java -version` y `javac -version`.

## Cómo ejecutarlo

Primero descarga el proyecto: en la página del repositorio pulsa **Code → Download ZIP** y descomprímelo, o clónalo:

```bash
git clone https://github.com/TU_USUARIO/TU_REPOSITORIO.git
cd TU_REPOSITORIO
```

**Opción A · Script (recomendada)** — compila y ejecuta:

| Sistema | Comando |
|---|---|
| Linux / macOS | `./ejecutar.sh` (si no deja: `chmod +x ejecutar.sh`) |
| Windows | doble clic en `ejecutar.bat` |

**Opción B · JAR ya compilado:**

```bash
java -jar dist/JerarquiaChomsky.jar
```

**Opción C · A mano:**

```bash
mkdir out
javac -encoding UTF-8 -d out $(find src -name "*.java")     # Windows: ver ejecutar.bat
java -cp out chomsky.Main
```

## Manual de uso

1. En el **menú principal** elige una de las cuatro tarjetas (Tipo 3, 2, 1 o 0).
2. En el módulo, elige el **lenguaje** en la lista desplegable. Debajo aparece su descripción formal.
3. Escribe una **cadena** en el campo de texto, o haz **clic en una cadena de prueba** de la lista de la derecha (se ejecuta sola). Verde = el lenguaje la acepta, rojo = la rechaza.
4. Controles de ejecución:
   - **Iniciar**: prepara la ejecución (paso 0).
   - **Paso >** y **< Atrás**: avanza o retrocede un paso (se puede volver atrás).
   - **Auto / Pausa**: reproduce la ejecución sola; el control **Velocidad** regula el ritmo.
   - **Reiniciar**: limpia la ejecución.
5. El panel **Traza de ejecución** (derecha) lista cada paso con la regla δ usada. Abajo aparece el **veredicto** (ACEPTADA / RECHAZADA) y, si aplica, el resultado de la función o la memoria usada.
6. **< Menú principal** vuelve al inicio.

En el Nivel 3 con el lenguaje *Múltiplos de N en binario* aparece un selector **N** (2 a 8): el autómata se reconstruye con ese N (lenguaje **parametrizable**).

Para escribir la cadena vacía deja el campo en blanco (o escribe `ε`).

## Cadenas de prueba (también están en la interfaz)

### Nivel 3 · AFD

| Lenguaje | Aceptan | Rechazan |
|---|---|---|
| Cantidad par de 1s (Σ={0,1}) | `ε`, `11`, `1001`, `000` | `1`, `10101`, `0110111` |
| Terminan en `ab` (Σ={a,b}) | `ab`, `aabab`, `bbab` | `abb`, `ba`, `a`, `ε` |
| Múltiplos de N en binario (N=3 por defecto) | `11` (3), `110` (6), `1001` (9), `0` | `100` (4), `111` (7) |

### Nivel 2 · Autómata de pila

| Lenguaje | Aceptan | Rechazan |
|---|---|---|
| aⁿbⁿ (n ≥ 1) | `ab`, `aabb`, `aaabbb` | `aab`, `abb`, `abab`, `ε` |
| Paréntesis y corchetes balanceados | `()`, `(())`, `([]())`, `ε` | `(()`, `())`, `([)]` |
| w c wᴿ (Σ={a,b,c}) | `abcba`, `abbcbba`, `aacaa`, `c` | `abcab`, `abba`, `acb` |

### Nivel 1 · Autómata linealmente acotado

| Lenguaje | Aceptan | Rechazan |
|---|---|---|
| aⁿbⁿcⁿ (n ≥ 1) | `abc`, `aabbcc`, `aaabbbccc` | `aabbc`, `abcc`, `abcabc`, `acb` |

### Nivel 0 · Máquina de Turing

| Lenguaje / función | Entrada → resultado |
|---|---|
| Palíndromos binarios (acepta/rechaza) | acepta: `ε`, `0`, `0110`, `10101` · rechaza: `01`, `0111`, `1100` |
| Suma unaria `1ᵃ+1ᵇ` | `11+111` → `11111` (2+3=5) · `1+1` → `11` · `111+` → `111` |
| Incremento binario n → n+1 | `1011` → `1100` · `111` → `1000` (la cinta crece a la izquierda) · `0` → `1` |

## Pruebas automáticas

```bash
./ejecutar-pruebas.sh        # Windows: ejecutar-pruebas.bat
```

Comprueba que **todas** las cadenas de prueba dan el veredicto esperado y verifica además contra la aritmética real: múltiplos de N para los números 0–200, aⁿbⁿ hasta n=30, suma unaria para 0–6, incremento de 0–300 y palíndromos de los números 0–511. Resultado esperado: `2442 comprobaciones, 0 fallos. TODO CORRECTO`.

## Estructura del proyecto

```
src/chomsky/
├── Main.java                  Ventana principal (CardLayout: menú + 4 módulos)
├── modelo/                    LÓGICA (sin gráficos): máquinas y ejemplos
│   ├── AFD.java               Autómata finito determinista
│   ├── AP.java                Autómata de pila
│   ├── MaquinaTuring.java     Máquina de Turing (modo infinito y modo acotado = LBA)
│   ├── Catalogo.java          Lenguajes de ejemplo + cadenas de prueba
│   └── Paso.java, Ejemplo.java, Lenguaje.java, Veredicto.java
└── ui/                        INTERFAZ (Swing)
    ├── MenuPrincipal.java     Tarjetas de los 4 niveles
    ├── PanelModulo.java       Esqueleto común: controles, traza, ejemplos
    ├── PanelAFD.java          Nivel 3: grafo de estados
    ├── PanelAP.java           Nivel 2: pila animada
    ├── PanelCinta.java        Niveles 1 y 0: cinta y cabezal
    └── Dibujo.java, Tema.java Utilidades de dibujo y estilo
pruebas/                       PruebasModelo (lógica), PruebasGUI y Capturas (interfaz)
docs/                          Documento de implementación y capturas
dist/JerarquiaChomsky.jar      Ejecutable
```

## Capturas

| Menú principal | Nivel 3 · AFD |
|---|---|
| ![menú](docs/capturas/00_menu.png) | ![afd](docs/capturas/01_nivel3_afd_paso.png) |

| Nivel 2 · Pila | Nivel 1 · Cinta acotada |
|---|---|
| ![ap](docs/capturas/02_nivel2_ap_paso.png) | ![lba](docs/capturas/03_nivel1_lba_paso.png) |

| Nivel 0 · Máquina de Turing |
|---|
| ![mt](docs/capturas/04_nivel0_mt_incremento.png) |

## Cómo publicar el proyecto en GitHub

1. Crea una cuenta en <https://github.com> y pulsa **New repository**. Ponle nombre, márcalo **Public** y créalo (sin README, porque ya existe uno).
2. En la carpeta del proyecto:

```bash
git init
git add .
git commit -m "Proyecto final: simulador de la Jerarquía de Chomsky"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/TU_REPOSITORIO.git
git push -u origin main
```

3. Reemplaza `TU_USUARIO/TU_REPOSITORIO` en este README por tu enlace real y entrega la URL del repositorio.

## Autor

_Nombre del estudiante · Materia · Universidad · 2026_
