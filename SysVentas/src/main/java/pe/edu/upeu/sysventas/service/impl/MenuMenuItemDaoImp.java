package pe.edu.upeu.sysventas.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.upeu.sysventas.dto.MenuMenuItenTO;
import pe.edu.upeu.sysventas.service.IMenuMenuItemDao;

import java.util.*;

@Service
public class MenuMenuItemDaoImp implements IMenuMenuItemDao {
    @Override
    public List<MenuMenuItenTO> listaAccesos(String perfil, Properties idioma) {

        List<MenuMenuItenTO> lista = new ArrayList<>();

        // Menú Principal
        lista.add(new MenuMenuItenTO("miprincipal", "/view/login.fxml",
                "Principal", "Salir",
                "Salir", "S"));

        // Menú Producto
        lista.add(new MenuMenuItenTO("miproducto", "/view/main_producto.fxml",
                "Producto", "Gestión Productos",
                "Gestión Productos", "T"));

        // Menú Mantenimiento
        lista.add(new MenuMenuItenTO("mimarca", "/view/main_marca.fxml",
                "Mantenimiento", "Reg. Marca",
                "Gestión Marcas", "T"));

        lista.add(new MenuMenuItenTO("micategoria", "/view/main_categoria.fxml",
                "Mantenimiento", "Reg. Categoría",
                "Gestión Categorías", "T"));

        lista.add(new MenuMenuItenTO("miunidadmedida", "/view/main_unidad_medida.fxml",
                "Mantenimiento", "Reg. Unidad Medida",
                "Gestión Unidades", "T"));

        // Menú Compras
        lista.add(new MenuMenuItenTO("miproveedor", "/view/main_proveedor.fxml",
                "Compras", "Reg. Proveedor",
                "Gestión Proveedores", "T"));

        // Menú Venta
        lista.add(new MenuMenuItenTO("micliente", "/view/main_cliente.fxml",
                "Venta", "Reg. Cliente",
                "Gestionar Cliente", "T"));

        lista.add(new MenuMenuItenTO("miventa", "/view/main_venta.fxml",
                "Venta", "Reg. Venta",
                "Gestionar Ventas", "T"));

        // Menú Cambiar Estilo
        lista.add(new MenuMenuItenTO("miestilo", "",
                "Cambiar Estilo", "",
                "", "N"));

        // Menú Idioma
        lista.add(new MenuMenuItenTO("miidioma", "",
                "Idioma", "",
                "", "N"));

        List<MenuMenuItenTO> accesoReal = new ArrayList<>();

        // Siempre agregar el menú principal
        accesoReal.add(lista.get(0));

        switch (perfil) {
            case "Administrador":
                accesoReal.add(lista.get(6)); // Cliente
                accesoReal.add(lista.get(7)); // Venta
                accesoReal.add(lista.get(8)); // Estilo
                accesoReal.add(lista.get(9)); // Idioma
                break;
            case "Root":
                accesoReal = lista;
                break;
            case "Reporte":
                accesoReal.add(lista.get(1)); // Producto
                accesoReal.add(lista.get(2)); // Marca
                accesoReal.add(lista.get(3)); // Categoría
                accesoReal.add(lista.get(4)); // Unidad Medida
                accesoReal.add(lista.get(5)); // Proveedor
                accesoReal.add(lista.get(6)); // Cliente
                accesoReal.add(lista.get(8)); // Estilo
                accesoReal.add(lista.get(9)); // Idioma
                break;
            default:
                throw new AssertionError();
        }
        return accesoReal;
    }

    @Override
    public Map<String, String[]> accesosAutorizados(List<MenuMenuItenTO> accesos) {

        Map<String, String[]> menuConfig = new HashMap<>();

        for (MenuMenuItenTO menu : accesos) {
            menuConfig.put("mi"+menu.getIdNombreObj(), new String[]{menu.getRutaFile(), menu.getNombreTab(),menu.getTipoTab()});
        }

        return menuConfig;
    }

}
