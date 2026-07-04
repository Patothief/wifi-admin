# Pokretanje i testiranje

Ovaj dokument opisuje kako lokalno pokrenuti backend aplikaciju, SOAP mock, testove i ručne REST provjere.

## Preduvjeti

- Java 21 ili novija verzija;
- Maven;
- Docker Desktop, ako želite ručno testirati komunikaciju prema Mockoon SOAP mocku.

## Pokretanje SOAP mocka

```bash
docker compose up -d
```

Mock je dostupan na:

```text
http://localhost:8080/platform
```

Zaustavljanje mocka:

```bash
docker compose down
```

## Pokretanje backend aplikacije

Prvo pokrenite SOAP mock:

```bash
docker compose up -d
```

Zatim pokrenite backend aplikaciju:

```bash
mvn spring-boot:run
```

Backend je dostupan na:

```text
http://localhost:8081
```

SOAP platforma u lokalnom profilu konfigurirana je na:

```text
http://127.0.0.1:8080/platform
```

Za pokretanje s eksplicitnim lokalnim profilom:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Zaustavljanje backend aplikacije: `Ctrl+C` u terminalu u kojem je aplikacija pokrenuta.

## Lokalna baza podataka

Backend koristi file-based H2 bazu:

```text
./data/wifi-admin
```

Shema se inicijalizira iz:

```text
src/main/resources/schema.sql
```

Tablica `wifi_configuration` sprema zadnju poznatu WiFi konfiguraciju po `cpeId`.

Vazno ponasanje nakon dodavanja DB sloja:

- `GET /wifi-parameter/{cpeId}` cita iskljucivo iz baze i ne poziva SOAP platformu;
- ako zapis jos ne postoji u bazi, `GET` vraca `404 NOT_FOUND`;
- `PUT /wifi-parameter` prvo azurira SOAP platformu, zatim sprema konfiguraciju koju platforma vrati u bazu;
- nakon uspjesnog `PUT` poziva, `GET` za isti `cpeId` vraca spremljeni zapis iz baze.

Za reset lokalne baze zaustavite backend aplikaciju i obrisite H2 datoteke:

```powershell
Remove-Item -Force .\data\wifi-admin.*
```

## Pokretanje testova

```bash
mvn test
```

Testovi ne zahtijevaju pokrenut Docker/Mockoon jer koriste mockirane i testne SOAP endpointove.

## Primjeri REST poziva (curl)

Na Windows PowerShellu koristite `curl.exe`. Samo `curl` je PowerShell alias za `Invoke-WebRequest` i ne prihvaća iste parametre kao curl.
Primjeri su napisani u jednom retku kako bi se mogli direktno kopirati u PowerShell.

**Dohvat WiFi parametara prije sinkronizacije u bazu:**

```bash
curl.exe -s "http://localhost:8081/wifi-parameter/CPE_001"
```

Ako zapis jos nije spremljen lokalno, ocekivani odgovor je `404 NOT_FOUND`.

**Azuriranje WiFi parametara i spremanje u bazu:**

```bash
curl.exe -s -X PUT "http://localhost:8081/wifi-parameter" -H "Content-Type: application/json" -H "Accept: application/json" -d '{\"cpeId\":\"CPE_001\",\"wifiBand\":\"BAND_2_4_GHZ\",\"ssid\":\"Office-2G-Updated\",\"encryptionType\":\"WPA2_PSK\",\"password\":\"new-wifi-password\"}'
```

Ovaj poziv mora imati dostupan SOAP mock jer backend salje promjenu na platformu prije spremanja u bazu.

**Dohvat WiFi parametara iz baze nakon uspjesnog azuriranja:**

```bash
curl.exe -s "http://localhost:8081/wifi-parameter/CPE_001"
```

Ocekivani odgovor sadrzi vrijednosti spremljene nakon `PUT` poziva, npr. `Office-2G-Updated`.

**Primjer neispravnog zahtjeva:**

```bash
curl.exe -s -X PUT "http://localhost:8081/wifi-parameter" -H "Content-Type: application/json" -H "Accept: application/json" -d '{\"cpeId\":\"CPE_001\",\"wifiBand\":\"BAND_2_4_GHZ\",\"ssid\":\"Office-2G\",\"encryptionType\":\"WPA2_PSK\"}'
```

Očekivani odgovor je `400 BAD_REQUEST` jer šifrirana mreža zahtijeva lozinku.

## Postman kolekcija

Postman kolekcija nalazi se u:

```text
postman/wifi-admin.postman_collection.json
```

U Postmanu odaberite **Import** i uvezite tu datoteku. Kolekcija sadrži varijable:

- `baseUrl`: `http://localhost:8081`
- `soapPlatformUrl`: `http://localhost:8080/platform`
- `cpeId`: `CPE_001`
