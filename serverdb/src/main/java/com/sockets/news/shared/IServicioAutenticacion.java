package com.rmi.news.shared;

import com.rmi.news.shared.dto.UsuarioDTO;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServicioAutenticacion extends Remote {
    UsuarioDTO iniciarSesion(String credencial, String password) throws RemoteException;
}