# MachineStock

Aplicación Android desarrollada en Kotlin. Es una aplicación de administración de stock para uso interno en una empresa de compra-venta de maquinarias. 
(Algunos archivos del repositorio no son los originales por motivos de privacidad.)

# Tecnologías:
- Arquitectura MVVM
- Corrutinas
- Room
- Dagger Hilt
- Navigation Component
- Fragments
- Recycler View
- Glide
- Constraint Layout
- Firebase (Auth, Firestorage, Firestore)

# Funcionalidades:
- Menu principal muestra un listado de listas diferentes:
  - Según tipos de máquinas
  - Según estado de las máquinas
  - Según fecha de cambio de la ficha de la maquina
  - Según fecha de ingreso de la maquina
  
- Pantalla de busqueda:
  - Se hace una consulta por entrada de texto
  - Se filtra la vista por: tipo, estado y tecnología.
  
- Pantalla de detalle: 
  - Se accede a la ficha técnica de la maquina.
  - Se edita el estado de la maquina.
  - Se comparte la ficha técnica con fotos y detalles de la maquina hacia otras aplicaciones. (Whatsapp, mail, etc)
  - Se accede a la galería para agregar una foto.
  - Se accede a la cámara para agregar una foto.
  - Se navega a la pantalla de edición de la ficha.
  
- Pantalla de edición:
  - Se editan los datos de las maquinas.
  - Validación del tipo de datos según el tipo de maquina.

- Pantalla de nuevo ingreso:
  - Se ingresa una nueva máquina.
  - Validación del tipo de datos según el tipo de maquina.
