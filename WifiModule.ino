//Librerias
#include <ESP8266WiFi.h>
#include <LiquidCrystal_I2C.h>
#include <Wire.h>
#include <Servo.h>

//Constantes
#define servoPin D3
#define salida_velocidad1 D6
#define salida_velocidad2 D7
#define salida_cancion D4
#define salida_direcciony D5
#define salida_volumen1 D0
#define salida_volumen2 D8

//Credenciales del WiFi
const char* ssid = "carrito";
const char* password = "carrito96";

//Variables
boolean cambiar_cancion;
WiFiServer server(80);
LiquidCrystal_I2C lcd(0x27, 16, 2);
Servo servo;
int indice;
String aux;

//Funciones
void girar(int grados);
void imprimir_cadena(int x, int y, String mensaje);
void imprimir_numero(int x, int y, int numero);

//Configuracion
void setup() {
  //Configurar salidas
  pinMode(salida_cancion, OUTPUT);
  pinMode(salida_direcciony, OUTPUT);
  pinMode(salida_velocidad1, OUTPUT);
  pinMode(salida_velocidad2, OUTPUT);
  pinMode(salida_volumen1, OUTPUT);
  pinMode(salida_volumen2, OUTPUT);
  pinMode(servoPin, OUTPUT);
  //Parar motor
  digitalWrite(salida_velocidad1, LOW);
  digitalWrite(salida_velocidad2, LOW);
  //Subir volumen al maximo
  digitalWrite(salida_volumen1, LOW);
  digitalWrite(salida_volumen2, LOW);
  //Inicio de la pantalla
  Wire.begin(D2, D1);
  lcd.begin();
  lcd.home();
  WiFi.mode(WIFI_AP);
  WiFi.softAP(ssid, password);
  // Inicializo el servidor
  server.begin();
  //Imprimir datos a la pantalla
  imprimir_cadena(0, 1, "adelante");
  imprimir_numero(10, 1, 0);
  imprimir_cadena(0, 0, WiFi.softAPIP().toString());
  //Prepara servo
  servo.attach(servoPin);
  girar(85);
  cambiar_cancion = false;
}

void loop() {
  //Compruebo si hay un cliente disponible (una petición)
  WiFiClient client = server.available();
  if (!client) {
    return; // En caso de no haber un cliente, no hago nada
  }
  //Espero hasta que el cliente realice una petición
  while (!client.available()) {
    delay(1);
  }
  //Leo la primera linea de la petición del cliente
  String request = client.readStringUntil('\r'); //Leo hasta retorno de carro
  client.flush(); //Limpio el buffer
  //Interpreto lo que he recibido
  indice = request.indexOf("/v=");
  int numero = request.substring(indice + 3).toInt();
  //Cambio de cancion
  if (numero >= 100) {
    switch (numero % 10) {
      case 0:
        digitalWrite(salida_volumen1, LOW);
        digitalWrite(salida_volumen2, LOW);
        break;
      case 1:
        digitalWrite(salida_volumen1, HIGH);
        digitalWrite(salida_volumen2, LOW);
        break;
      case 2:
        digitalWrite(salida_volumen1, LOW);
        digitalWrite(salida_volumen2, HIGH);
        break;
      case 3:
        digitalWrite(salida_volumen1, HIGH);
        digitalWrite(salida_volumen2, HIGH);
        break;
    }
    cambiar_cancion = !cambiar_cancion;
    digitalWrite(salida_cancion, cambiar_cancion);
    numero /= 10;
  }
  //Velocidad y direccion
  switch (numero % 10) {
    case 1:
      digitalWrite(salida_velocidad1, LOW);
      digitalWrite(salida_velocidad2, LOW);
      digitalWrite(salida_direcciony, LOW);
      imprimir_cadena(0, 1, "adelante  0");
      break;
    case 2:
      digitalWrite(salida_velocidad1, LOW);
      digitalWrite(salida_velocidad2, HIGH);
      digitalWrite(salida_direcciony, LOW);
      imprimir_cadena(0, 1, "adelante  1");
      break;
    case 3:
      digitalWrite(salida_velocidad1, HIGH);
      digitalWrite(salida_velocidad2, LOW);
      digitalWrite(salida_direcciony, LOW);
      imprimir_cadena(0, 1, "adelante  2");
      break;
    case 4:
      digitalWrite(salida_velocidad1, HIGH);
      digitalWrite(salida_velocidad2, HIGH);
      digitalWrite(salida_direcciony, LOW);
      imprimir_cadena(0, 1, "adelante  3");
      break;
    case 5:
      digitalWrite(salida_velocidad1, LOW);
      digitalWrite(salida_velocidad2, HIGH);
      digitalWrite(salida_direcciony, HIGH);
      imprimir_cadena(0, 1, "reversa   1");
      break;
  }
  //Angulo de servo
  switch (numero /= 10) {
    case 1:
      girar(55);
      break;
    case 2:
      girar(85);
      break;
    case 3:
      girar(115);
      break;
  }
  client.println("HTTP/1.1 200 OK");
  client.println("");                                     //No olvidar esta línea de separación
  client.println("<!DOCTYPE HTML>");
  client.println("<meta charset='UTF-8'>");
  client.println("<html>");
  client.println("Presiona <a href='/LED=ON'>AQUÍ</a> para encender el LED<br>");
  client.println("Presiona <a href='/LED=OFF'>AQUÍ</a> para apagar el LED<br><br>");
  client.println("</html>");
  delay(1);
}

void imprimir_cadena(int x, int y, String mensaje) {
  lcd.setCursor(x, y);
  lcd.print(mensaje);
}

void imprimir_numero(int x, int y, int numero) {
  lcd.setCursor(x, y);
  lcd.print(numero);
}

void girar(int grados) {
  if (grados > 115) {
    servo.write(115);
  } else if (grados < 55) {
    servo.write(55);
  } else {
    servo.write(grados);
  }
}
