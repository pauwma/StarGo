# StarGo

#Avances
## Implementación de un sistema de chat en tiempo real con Firestore - 30/04

### Estructura de la base de datos en Firestore

La estructura de la base de datos propuesta es la siguiente:
1- chats
  2- chatId
     3- users
         4- userId1
         4- userId2 
     3 - messages (una subcólección dentro del documento de cada chat)
         4- messageId
            5- content
            5 - autor
            5 - timestamp

### Pasos para crear la estructura de datos en Firestore

1. Accede a la consola web de Firebase y selecciona tu proyecto.
2. Ve a "Firestore Database" en el panel de navegación lateral y crea una nueva base de datos de Firestore si aún no lo has hecho.
3. Crea una colección llamada "chats" en la página principal de Firestore en la consola web.
4. Crea un documento de ejemplo en la colección "chats". Este documento representará un chat individual.
5. Agrega los siguientes campos al documento de chat:
   - `participantes`: Tipo de dato "map". Almacena los ID de los usuarios que participan en el chat.
6. Crea una subcolección llamada "mensajes" dentro del documento de chat.
7. Crea un documento de ejemplo en la subcolección "mensajes". Este documento representará un mensaje individual en el chat.
8. Agrega los siguientes campos al documento de mensaje:
   - `contenido`: Tipo de dato "string". Almacena el texto del mensaje.
   - `autor`: Tipo de dato "string". Almacena el ID del autor del mensaje.
   - `timestamp`: Tipo de dato "timestamp". Almacena la fecha y hora en que se envió el mensaje.


Figma: https://www.figma.com/file/Imvsi0Qx6A3rEepnSdoddn/StarGo---MockUp?t=YbVS0gzbEXKxDMfR-0
![Portada1](https://user-images.githubusercontent.com/114858315/222991070-0336ada2-260a-47d3-80e0-b7ea3b7622e4.png)
![Poster_Simple](https://user-images.githubusercontent.com/114858315/222991031-d97b8abb-e43a-407c-85f8-954da6870bc3.png)
