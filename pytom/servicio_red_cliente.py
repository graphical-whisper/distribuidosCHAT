import socket
import threading

class ServicioRedCliente:
    def __init__(self, ip, puerto, nombre_usuario, listener):
        self.ip = ip
        self.puerto = puerto
        self.nombre_usuario = nombre_usuario
        self.listener = listener
        self.socket_cliente = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.activo = True

    def conectar(self):
        try:
            self.socket_cliente.connect((self.ip, self.puerto))
            # Registro inicial
            mensaje_registro = f"CONNECT;{self.nombre_usuario}\n"
            self.socket_cliente.sendall(mensaje_registro.encode('utf-8'))

            # Inicia el hilo en segundo plano
            hilo_recepcion = threading.Thread(target=self._escuchar_servidor, daemon=True)
            hilo_recepcion.start()
        except Exception:
            self.listener.on_desconexion()

    def enviar_mensaje_global(self, contenido):
        self._enviar(f"BROADCAST;{self.nombre_usuario};{contenido}\n")

    def enviar_mensaje_privado(self, destinatario, contenido):
        self._enviar(f"PRIVATE;{self.nombre_usuario};{destinatario};{contenido}\n")

    def _enviar(self, mensaje):
        try:
            if self.activo:
                self.socket_cliente.sendall(mensaje.encode('utf-8'))
        except Exception:
            pass # Si falla el envío, el hilo de escucha detectará la caída pronto

    def _escuchar_servidor(self):
        try:
            while self.activo:
                data = self.socket_cliente.recv(1024).decode('utf-8')
                if not data:
                    break
                
                mensajes = data.strip().split('\n')
                for mensaje in mensajes:
                    if mensaje:
                        self._procesar_linea_servidor(mensaje)
        except Exception:
            self.listener.on_desconexion()
        finally:
            self.cerrar_conexion()

    def _procesar_linea_servidor(self, linea):
        partes = linea.split(';', 2)
        if not partes: return
        comando = partes[0]

        if comando == "USERS":
            usuarios = linea.split(';')[1:]
            usuarios_limpios = [u for u in usuarios if u and u != self.nombre_usuario]
            self.listener.on_lista_usuarios_actualizada(usuarios_limpios)
        elif comando == "MSG" and len(partes) >= 3:
            self.listener.on_mensaje_recibido(partes[1], partes[2])
        elif comando == "PRIVMSG" and len(partes) >= 3:
            self.listener.on_mensaje_privado_recibido(partes[1], partes[2])
        elif comando == "ERROR" and len(partes) >= 2:
            self.listener.on_error_recibido(partes[1])

    def cerrar_conexion(self):
        self.activo = False
        try:
            self.socket_cliente.close()
        except Exception:
            pass