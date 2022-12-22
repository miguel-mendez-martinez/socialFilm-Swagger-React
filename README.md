# socialFilm-Swagger&React

Se trata de un proyecto hecho durante el curso 2022/2023 para la asignatura de Ingeniería de Servicios de la Universidad de Santiago de compostela.

Para iniciarla, debemos usar un IDE de java, yo recomiendo intelliJ y ahi podrmeos iniciar el .jar o simplemente ejecutar el .jar desde la consola de windows.

Una vez tenemos eso, nuestro backend utiliza mongoDB como base de datos, en esta versión la base de datos es local por lo que se debe crear con 
el compass o alguna aplicación similiar una copia de la base de datos. En futuras versiones se tiene planeado utilizar una versión de la base de datos en mongo Atlas 
de manera que se comparta la información entre los clientes.

Para el front-end. Al tratarse de una app de react, debemos posicionarnos con una ventana de la consola en el directorio donde se encuentra nuestro proyecto.
Se ejecuta un npm install de manera que se instalan las dependencias necesarias y se ejecuta por último un npm start de manera que se inicia el cliente. Tras eso se
debe esperar hasta que se abra una ventana en el buscador y ya podremos usar nuestra APP.

Se ha utilizado react con la implementación de funciones y hooks, de distinta manera que en proyectos anteriores donde se utilizaba la implementación por componentes.
Para el estilo se ha usado Tailwind css, y se usa fetch para las llamadas a la API ubicada en otro puerto del ordenador.
