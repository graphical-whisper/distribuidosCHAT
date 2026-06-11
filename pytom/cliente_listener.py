class ClienteListener:
    def on_mensaje_recibido(self, emisor, contenido):
        pass

    def on_mensaje_privado_recibido(self, emisor, contenido):
        pass

    def on_lista_usuarios_actualizada(self, usuarios):
        pass

    def on_error_recibido(self, mensaje_error):
        pass

    def on_desconexion(self):
        pass
