package com.rmi.news.shared;

import com.rmi.news.shared.dto.NoticiaDTO;
import com.rmi.news.shared.dto.UsuarioDTO;
import com.rmi.news.shared.exception.PermisoDenegadoException;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IServicioNoticias extends Remote {
    
    List<NoticiaDTO> buscarNoticias(String criterio) throws RemoteException;
    
    NoticiaDTO obtenerNoticia(String nombreUnico) throws RemoteException;

    NoticiaDTO crearNoticia(NoticiaDTO noticia, UsuarioDTO usuarioAccion) throws RemoteException, PermisoDenegadoException;
    
    NoticiaDTO modificarNoticia(NoticiaDTO noticiaActualizada, UsuarioDTO usuarioAccion) throws RemoteException, PermisoDenegadoException;
    
    boolean eliminarNoticia(String nombreUnico, UsuarioDTO usuarioAccion) throws RemoteException, PermisoDenegadoException;
}