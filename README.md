# nameday

Malá Android aplikácia a widget, ktorý ukazuje, kto má dnes meniny podľa slovenského kalendára.

## Funkcie

- **Widget na plochu**: dnešné meniny a pod nimi, kto má meniny zajtra. Ťuknutím sa otvorí aplikácia.
  - Farby sa prispôsobujú tapete (Material You) aj tmavému režimu.
  - Veľkosť 2×2 sa dá zmenšiť na 2×1 a vtedy sa prepne na kompaktné zobrazenie.
  - Obnovuje sa krátko po polnoci a pri zmene času.
- **Kalendár na celý rok** rozdelený po mesiacoch. Dnešný deň je zvýraznený a zoznam sa naň pri otvorení sám posunie.
- **Obľúbené mená**: podržaním prsta na mene ho pridáš medzi obľúbené.
  - Obľúbené mená majú v kalendári aj vo widgete hviezdičku ★.
  - Hviezdička vpravo hore ukáže zoznam obľúbených zoradený podľa toho, kto má meniny najskôr.

## Spustenie

Požiadavky: Android Studio a telefón s Androidom 12 alebo novším.

1. Otvor priečinok projektu v Android Studiu a počkaj na Gradle Sync.
2. Pripoj telefón cez USB alebo cez Wi-Fi (*Pair Devices Using Wi-Fi*) a stlač **Run ▶**.
3. Widget pridáš tak, že na ploche podržíš prst a zvolíš **Miniaplikácie → nameday**.

## Štruktúra

| Súbor | Obsah |
|---|---|
| `app/src/main/java/sk/meniny/NameDays.kt` | kalendár mien |
| `app/src/main/java/sk/meniny/MainActivity.kt` | obrazovka aplikácie (Jetpack Compose) |
| `app/src/main/java/sk/meniny/NameDayWidget.kt` | widget |
| `app/src/main/java/sk/meniny/Favorites.kt` | obľúbené mená |
| `app/src/main/res/layout/widget_name_day*.xml` | vzhľad widgetu |
