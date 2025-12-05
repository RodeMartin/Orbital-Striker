# OrbitalStriker
  
Modern webalapú futball-stratégiai játék, valós idejű fizikai szimulációval, WebSocket kommunikációval és interaktív felhasználói felülettel.  
A projekt frontendje Vite + React + MUI, a backend pedig Spring Boot + H2 adatbázisra épül.

---

## Tartalomjegyzék
1. [Projekt áttekintés](#1-projekt-áttekintés)
2. [Fő funkciók](#2-fő-funkciók)
3. [Rendszerarchitektúra](#3-rendszerarchitektúra)
4. [Telepítés és futtatás](#4-telepítés-és-futtatás)
   - [Backend indítása](#backend-indítása)
   - [Frontend indítása](#frontend-indítása)
   - [Adatbázis előkészítése (H2--csv-import)](#adatbázis-előkészítése-h2--csv-import)
5. [Fejlesztői környezet követelményei](#5-fejlesztői-környezet-követelményei)
6. [Hardverkövetelmények](#6-hardverkövetelmények)
7. [Mappa-struktúra](#7-mappa-struktúra)
8. [Demo képernyőképek](#8-demo-képernyőképek)
9. [Licenc](#9-licenc)

---

## 1. Projekt áttekintés

Az OrbitalStriker egy asztali böngészőre optimalizált, valós idejű fizikai gombfocit megvalósító játék. (Soccer Stars - Miniclip)
A backend valós időben számolja a mozgásokat és kezeli a teljes játékmenetet (korong-fizika, ütközések, gól-detektálás, AI).  
A frontend dinamikus, vászon alapú megjelenítést használ animációkhoz, célzáshoz és részecske-effektekhez.

---

## 2. Fő funkciók

- Regisztráció és bejelentkezés (email)
- Liga és csapatválasztás (H2 DB-ből töltve)
- Logó-előnézetek körbevágott PNG-kkel
- Valós idejű szimuláció WebSocket / STOMP kapcsolattal
- "Particle" animáció gól esetén
- Felhasználói statisztikák (ESPK, meccsek, gólok)
- AI ellenfél több nehézségi fokozattal
- Adaptív canvas és responsiveness
- H2 adatbázis automata feltöltése CSV adatokból
- Battle Pass és coin-szintű rendszer előkészítve (Folyamatban)

---

## 3. Rendszerarchitektúra

Backend:  
- Spring Boot  
- REST API + WebSocket (STOMP)  
- Fizikai motor (korongok mozgása, ütközések, gól logika)  
- AI döntéshozatal  
- H2 adatbázis, league/team/player táblákkal  
- DbInit modul (CSV → XML → DB import)  

Frontend:  
- React 18 (Vite)  
- Material UI  
- Custom canvas alapú renderer  
- State kezelés React hookokkal  
- WebSocket kliens  

---

## 4. Telepítés és futtatás

### 4.1 Backend indítása
```
cd backend
mvn spring-boot:run
```

H2 konzol:  
http://localhost:8080/phpmyadmin 
JDBC URL: `jdbc:h2:mem:testdb`
---
(Alapértelmezett:h2-console, ez csak a saját dolgom megkönnyítésére.) 

### 4.2 Frontend indítása
```
cd frontend
npm install
npm run dev
```
Frontend elérhető:  
http://localhost:5173

---

### 4.3 H2 adatbázis előkészítése CSV-ből  
A projekt tartalmaz egy `generate_leagues_xml.py` scriptet, ami a CSV állományokat XML-lé alakítja (Spring importhoz).

Futtatás:
```
python generate_leagues_xml.py
```

Az így generált adatok bekerülnek a backend `resources` alá, a `Data Initializer` modul pedig automatikusan betölti indításkor.

---

## 5. Fejlesztői környezet követelményei
- Node.js 18 vagy újabb  
- Java 17 vagy újabb  
- Maven 3.8+  
- Python 3 (csak a CSV → XML generáláshoz)  
- Böngésző

---

## 6. Hardverkövetelmények

### Minimum:
- CPU: 2 magos processzor (2.0 GHz)  
- RAM: 4 GB  
- GPU: Integrált grafika (Intel HD 4000 vagy jobb)  
- Képernyőfelbontás: 1366×768  

### Ajánlott:
- CPU: 4 magos processzor, 3.0 GHz+  
- RAM: 8 GB  
- GPU: Dedikált vagy modern integrált (UHD 620+)  
- 1080p felbontás vagy nagyobb  

---

## 7. Mappa-struktúra

```
OrbitalStriker/
│
├── backend/
│   ├── src/main/java/com/orbitalstriker/backend/
│   │   ├── modules/     # Auth, Users, Teams, Game, game-engine
│   │   └── config/      # WebSocket, CORS, H2
│   ├── resources/
│   │   ├── db/          # CSV → XML import
│   │   └── application.yml
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── views/
│   │   └── App.jsx
│   └── package.json
│
└── README.md
└── .gitignore
```

---

## 8. Demo képernyőképek

### Login képernyő
![Login](backend/resources/demo_login.png)

### Csapatválasztó
![Selector](backend/resources/demo_selection.png)

### Játék (canvas + scoreboard)
![Gameplay](backend/resources/demo_ingame.png)

# 👤 Szerző
**[Ródé Martin]**
* Egyetemi hallgató - [Tokaj-Hegyalja Egyetem - PTI]
* Neptun-kód: **DRPPXL**
* GitHub: [@RodeMartin](https://github.com/RodeMartin)

## 9. Licenc

Ez a projekt az MIT License alatt érhető el.

