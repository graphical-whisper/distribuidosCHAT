package com.rmi.news.server.service;

import com.rmi.news.server.dao.NoticiaSocketDAO;
import com.rmi.news.shared.IServicioNoticias;
import com.rmi.news.shared.dto.NoticiaDTO;
import com.rmi.news.shared.dto.UsuarioDTO;
import com.rmi.news.shared.exception.PermisoDenegadoException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ServicioNoticiasImpl extends UnicastRemoteObject implements IServicioNoticias {

    private final NoticiaSocketDAO noticiaDAO;

    public ServicioNoticiasImpl() throws RemoteException {
        super(); // Exporta el objeto en un puerto efímero o parametrizado
        this.noticiaDAO = new NoticiaSocketDAO();
    }

    @Override
    public List<NoticiaDTO> buscarNoticias(String criterio) throws RemoteException {
        // La lectura no requiere validación de permisos estricta
        return noticiaDAO.obtenerTodas(criterio);
    }

    @Override
    public NoticiaDTO obtenerNoticia(String nombreUnico) throws RemoteException {
        return noticiaDAO.obtenerPorNombreUnico(nombreUnico);
    }

    @Override
    public NoticiaDTO crearNoticia(NoticiaDTO noticia, UsuarioDTO usuarioAccion) throws RemoteException, PermisoDenegadoException {
        if (usuarioAccion == null) {
            throw new PermisoDenegadoException("Usuario no autenticado.");
        }

        noticia.setId(UUID.randomUUID().toString());
        noticia.setAutorId(usuarioAccion.getId());
        noticia.setFechaCreacion(LocalDateTime.now());
        noticia.setUltimaFechaActualizacion(LocalDateTime.now());

        return noticiaDAO.guardar(noticia);
    }

    @Override
    public NoticiaDTO modificarNoticia(NoticiaDTO noticiaActualizada, UsuarioDTO usuarioAccion) throws RemoteException, PermisoDenegadoException {
        NoticiaDTO noticiaOriginal = noticiaDAO.obtenerPorNombreUnico(noticiaActualizada.getNombreUnico());
        
        if (noticiaOriginal == null) {
            throw new RemoteException("La noticia especificada no existe.");
        }

        validarPermisosModificacion(noticiaOriginal, usuarioAccion);

        noticiaActualizada.setUltimaFechaActualizacion(LocalDateTime.now());
        // Se preservan datos inmutables
        noticiaActualizada.setFechaCreacion(noticiaOriginal.getFechaCreacion());
        noticiaActualizada.setAutorId(noticiaOriginal.getAutorId());

        return noticiaDAO.actualizar(noticiaActualizada);
    }

    @Override
    public boolean eliminarNoticia(String nombreUnico, UsuarioDTO usuarioAccion) throws RemoteException, PermisoDenegadoException {
        NoticiaDTO noticiaOriginal = noticiaDAO.obtenerPorNombreUnico(nombreUnico);
        if (noticiaOriginal == null) return false;

        validarPermisosModificacion(noticiaOriginal, usuarioAccion);

        return noticiaDAO.eliminar(nombreUnico);
    }

    private void validarPermisosModificacion(NoticiaDTO noticia, UsuarioDTO usuario) throws PermisoDenegadoException {
        boolean esAdmin = "ADMINISTRADOR".equals(usuario.getRol());
        boolean esAutor = usuario.getId().equals(noticia.getAutorId());

        if (!esAdmin && !esAutor) {
            throw new PermisoDenegadoException("No tiene permisos para modificar o eliminar esta noticia. No es el autor original.");
        }
    }
}