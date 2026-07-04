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

## Pokretanje testova

```bash
mvn test
```

Testovi ne zahtijevaju pokrenut Docker/Mockoon jer koriste mockirane i testne SOAP endpointove.

## Primjeri REST poziva (curl)

Na Windows PowerShellu koristite `curl.exe`. Samo `curl` je PowerShell alias za `Invoke-WebRequest` i ne prihvaća iste parametre kao curl.
Primjeri su napisani u jednom retku kako bi se mogli direktno kopirati u PowerShell.

**Dohvat WiFi parametara:**

```bash
curl.exe -s "http://localhost:8081/wifi-parameter/CPE_001"
```

**Ažuriranje WiFi parametara:**

```bash
curl.exe -s -X PUT "http://localhost:8081/wifi-parameter" -H "Content-Type: application/json" -H "Accept: application/json" -d '{\"cpeId\":\"CPE_001\",\"wifiBand\":\"BAND_2_4_GHZ\",\"ssid\":\"Office-2G-Updated\",\"encryptionType\":\"WPA2_PSK\",\"password\":\"new-wifi-password\"}'
```

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
