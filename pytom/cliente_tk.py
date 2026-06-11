import tkinter as tk
from tkinter import simpledialog, scrolledtext
from cliente_listener import ClienteListener
from servicio_red_cliente import ServicioRedCliente

class ClienteMensajeriaTK(ClienteListener):
    def __init__(self, master, ip_servidor, puerto, nombre_usuario):
        self.master = master
        self.nombre_usuario = nombre_usuario
        self.master.title(f"Chat - {nombre_usuario}")
        self.master.geometry("600x400")

        self.configurar_interfaz()

        # Inyección de dependencias
        self.servicio_red = ServicioRedCliente(ip_servidor, puerto, nombre_usuario, self)
        self.servicio_red.conectar()

        # Capturar evento de cierre de ventana
        self.master.protocol("WM_DELETE_WINDOW", self.al_cerrar)

    def configurar_interfaz(self):
        main_frame = tk.Frame(self.master)
        main_frame.pack(fill=tk.BOTH, expand=True)

        chat_frame = tk.Frame(main_frame)
        chat_frame.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        
        self.area_chat = scrolledtext.ScrolledText(chat_frame, state='disabled')
        self.area_chat.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)

        users_frame = tk.Frame(main_frame, width=150)
        users_frame.pack(side=tk.RIGHT, fill=tk.Y, padx=5, pady=5)
        users_frame.pack_propagate(False)
        
        tk.Label(users_frame, text="Usuarios").pack()
        self.lista_usuarios = tk.Listbox(users_frame, exportselection=False)
        self.lista_usuarios.pack(fill=tk.BOTH, expand=True)

        bottom_frame = tk.Frame(self.master)
        bottom_frame.pack(side=tk.BOTTOM, fill=tk.X, padx=5, pady=5)
        
        self.campo_mensaje = tk.Entry(bottom_frame)
        self.campo_mensaje.pack(side=tk.LEFT, fill=tk.X, expand=True)
        self.campo_mensaje.bind("<Return>", lambda event: self.gestionar_envio())
        
        btn_enviar = tk.Button(bottom_frame, text="Send", command=self.gestionar_envio)
        btn_enviar.pack(side=tk.RIGHT, padx=5)

    def gestionar_envio(self):
        texto = self.campo_mensaje.get().strip()
        if not texto: return

        seleccion = self.lista_usuarios.curselection()
        if seleccion:
            destinatario = self.lista_usuarios.get(seleccion[0])
            self.servicio_red.enviar_mensaje_privado(destinatario, texto)
            self.lista_usuarios.selection_clear(0, tk.END)
        else:
            self.servicio_red.enviar_mensaje_global(texto)
            
        self.campo_mensaje.delete(0, tk.END)

    # --- Contrato ClienteListener (Thread-Safe mediante after) ---
    def on_mensaje_recibido(self, emisor, contenido):
        self.master.after(0, self._escribir_en_chat, f"{emisor}: {contenido}")

    def on_mensaje_privado_recibido(self, emisor, contenido):
        self.master.after(0, self._escribir_en_chat, f"[Privado de {emisor}]: {contenido}")

    def on_lista_usuarios_actualizada(self, usuarios):
        self.master.after(0, self._actualizar_lista, usuarios)

    def on_error_recibido(self, mensaje_error):
        self.master.after(0, self._escribir_en_chat, f"SISTEMA: {mensaje_error}")

    def on_desconexion(self):
        self.master.after(0, self._escribir_en_chat, "[-] Desconectado del servidor.")

    # --- Utilidades Internas ---
    def _escribir_en_chat(self, texto):
        self.area_chat.config(state='normal')
        self.area_chat.insert(tk.END, texto + "\n")
        self.area_chat.config(state='disabled')
        self.area_chat.yview(tk.END)

    def _actualizar_lista(self, usuarios):
        self.lista_usuarios.delete(0, tk.END)
        for usr in usuarios:
            self.lista_usuarios.insert(tk.END, usr)

    def al_cerrar(self):
        self.servicio_red.cerrar_conexion()
        self.master.destroy()

if __name__ == "__main__":
    root = tk.Tk()
    root.withdraw() 

    ip_servidor = simpledialog.askstring("Servidor", "Ingrese la IP del servidor:", initialvalue="10.10.1.129")
    if ip_servidor:
        usuario = simpledialog.askstring("Usuario", "Ingrese su nombre de usuario:")
        if usuario:
            root.deiconify() 
            app = ClienteMensajeriaTK(root, ip_servidor, 5000, usuario)
            root.mainloop()